param(
    [ValidateSet("plan", "apply", "clean")]
    [string]$Mode = "plan"
)

$ErrorActionPreference = "Stop"
$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
$maven = Get-Command mvn -ErrorAction SilentlyContinue
if (-not $maven) {
    throw "No se encontró Maven en PATH. Instala Maven 3.9 o superior."
}

if ([string]::IsNullOrWhiteSpace($env:DB_PASSWORD)) {
    throw "Configura DB_PASSWORD en el entorno antes de ejecutar el cargador."
}

if ($Mode -ne "plan" -and $env:TEST_DATA_APPROVED -ne "YES") {
    throw "Para $Mode se requiere revisar/aprobar el plan y establecer TEST_DATA_APPROVED=YES."
}

if ($Mode -eq "apply" -and
    ([string]::IsNullOrWhiteSpace($env:TEST_DATA_PASSWORD) -or $env:TEST_DATA_PASSWORD.Length -lt 12)) {
    throw "Para apply, configura TEST_DATA_PASSWORD con al menos 12 caracteres."
}

$mainClass = "pe.edu.blenkir.ssacb.TestDataLoader"
$arguments = "--mode=$Mode"
& $maven.Source `
    -f (Join-Path $projectRoot "backend\pom.xml") `
    "-Dspring-boot.run.main-class=$mainClass" `
    "-Dspring-boot.run.arguments=$arguments" `
    spring-boot:run

if ($LASTEXITCODE -ne 0) {
    throw "El cargador terminó con código $LASTEXITCODE."
}
