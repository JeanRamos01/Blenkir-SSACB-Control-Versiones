param(
    [ValidateSet("Plan", "Apply")]
    [string]$Mode = "Plan"
)

$ErrorActionPreference = "Stop"
$reviewSql = Join-Path $PSScriptRoot "review-academic-distribution.sql"
$repairSql = Join-Path $PSScriptRoot "repair-academic-distribution.sql"

$postgresBin = $null
$psqlCommand = Get-Command psql -ErrorAction SilentlyContinue
if ($psqlCommand) {
    $postgresBin = Split-Path $psqlCommand.Source -Parent
} else {
    $postgresRoot = Join-Path $env:ProgramFiles "PostgreSQL"
    if (Test-Path $postgresRoot) {
        $installations = Get-ChildItem $postgresRoot -Directory | Sort-Object Name -Descending
        foreach ($installation in $installations) {
            $candidate = Join-Path $installation.FullName "bin"
            if (Test-Path (Join-Path $candidate "psql.exe")) {
                $postgresBin = $candidate
                break
            }
        }
    }
}
if (-not $postgresBin) {
    throw "No se encontraron herramientas cliente de PostgreSQL en PATH ni en $env:ProgramFiles\PostgreSQL."
}

$psql = Join-Path $postgresBin "psql.exe"
$pgDump = Join-Path $postgresBin "pg_dump.exe"
$pgRestore = Join-Path $postgresBin "pg_restore.exe"
foreach ($toolPath in @($psql, $pgDump, $pgRestore)) {
    if (-not (Test-Path $toolPath)) {
        throw "Falta una herramienta PostgreSQL requerida: $toolPath"
    }
}

if ([string]::IsNullOrWhiteSpace($env:DB_URL)) {
    throw "Configura DB_URL con jdbc:postgresql://host:puerto/base antes de continuar."
}
if ([string]::IsNullOrWhiteSpace($env:DB_USERNAME) -or
    [string]::IsNullOrWhiteSpace($env:DB_PASSWORD)) {
    throw "Configura DB_USERNAME y DB_PASSWORD en el entorno."
}

if ($Mode -eq "Apply" -and $env:TEST_DATA_APPROVED -ne "YES") {
    throw "Apply requiere aprobación explícita: establece TEST_DATA_APPROVED=YES después de revisar el plan."
}

$urlPattern = '^(?:jdbc:)?postgresql://(?<host>\[[^\]]+\]|[^:/?]+)(?::(?<port>[0-9]+))?/(?<database>[^?]+)(?:\?(?<query>.*))?$'
if ($env:DB_URL -notmatch $urlPattern) {
    throw "DB_URL debe usar jdbc:postgresql://host[:puerto]/base[?opciones]."
}

$databaseHost = [string]$Matches.host
if ($databaseHost.StartsWith("[") -and $databaseHost.EndsWith("]")) {
    $databaseHost = $databaseHost.Substring(1, $databaseHost.Length - 2)
}
$databasePort = if ($Matches.port) { $Matches.port } else { "5432" }
$databaseName = [System.Uri]::UnescapeDataString($Matches.database)
if ($databaseName.Contains("/")) {
    throw "DB_URL debe identificar una sola base de datos."
}
if ($Matches.query -match '(?:^|&)sslmode=(?<sslmode>disable|allow|prefer|require|verify-ca|verify-full)(?:&|$)') {
    $env:PGSSLMODE = $Matches.sslmode
}

$previousPgPassword = $env:PGPASSWORD
$env:PGPASSWORD = $env:DB_PASSWORD
$connectionArgs = @(
    "--no-password",
    "--host=$databaseHost",
    "--port=$databasePort",
    "--username=$env:DB_USERNAME",
    "--dbname=$databaseName"
)

try {
    if ($Mode -eq "Plan") {
        Write-Host "Plan de solo lectura: no se crearán respaldos ni se escribirán datos."
        & $psql @connectionArgs --no-psqlrc --set ON_ERROR_STOP=1 --file $reviewSql
        if ($LASTEXITCODE -ne 0) {
            throw "La revisión de solo lectura terminó con código $LASTEXITCODE."
        }
        return
    }

    if ([string]::IsNullOrWhiteSpace($env:LOCALAPPDATA)) {
        throw "No se pudo determinar LOCALAPPDATA para guardar el respaldo privado."
    }
    $backupDirectory = Join-Path $env:LOCALAPPDATA "Blenkir\database-backups\academic-distribution"
    New-Item -ItemType Directory -Path $backupDirectory -Force | Out-Null
    $identity = [System.Security.Principal.WindowsIdentity]::GetCurrent().Name
    & icacls.exe $backupDirectory /inheritance:r /grant:r "${identity}:(OI)(CI)F" | Out-Null
    if ($LASTEXITCODE -ne 0) {
        throw "No se pudo restringir el acceso al directorio privado del respaldo."
    }

    $timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
    $backupFile = Join-Path $backupDirectory "academic-distribution-$timestamp.dump"
    if (Test-Path $backupFile) {
        throw "Ya existe un respaldo con este nombre; espera un segundo y vuelve a intentar."
    }
    Write-Host "Creando respaldo PostgreSQL previo a cualquier escritura: $backupFile"
    & $pgDump @connectionArgs --format=custom --file $backupFile
    if ($LASTEXITCODE -ne 0) {
        throw "pg_dump falló; no se ejecutará la corrección."
    }
    & $pgRestore --list $backupFile | Out-Null
    if ($LASTEXITCODE -ne 0) {
        throw "No se pudo validar el respaldo; no se ejecutará la corrección."
    }

    Write-Host "Respaldo validado. Ejecutando la corrección transaccional aprobada."
    & $psql @connectionArgs --no-psqlrc --set ON_ERROR_STOP=1 --set allow_write=on --file $repairSql
    if ($LASTEXITCODE -ne 0) {
        throw "La corrección terminó con código $LASTEXITCODE. PostgreSQL revierte la transacción al cerrar la conexión."
    }
    Write-Host "Corrección completada. Respaldo: $backupFile"
}
finally {
    $env:PGPASSWORD = $previousPgPassword
}
