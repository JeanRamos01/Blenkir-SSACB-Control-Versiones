# Datos ficticios de prueba para Blenkir SIGA

Este cargador usa las 20 entidades/tablas del modelo JPA de `backend` y las restricciones definidas en esas entidades y en `database/03-modulos-administrativos-blenkir.sql`. No crea ni altera tablas. La base de datos debe estar previamente creada y su esquema debe coincidir; antes de procesar datos, el programa comprueba las tablas, columnas, claves primarias, unicidades y claves foráneas requeridas.

El modo predeterminado es `plan`: solo valida el esquema y muestra el volumen objetivo; no escribe. `apply` y `clean` son operaciones explícitas y exigen `TEST_DATA_APPROVED=YES`. La carga y la limpieza se ejecutan en una única transacción, con bloqueo de PostgreSQL para evitar dos ejecuciones simultáneas. Si una validación o una restricción falla, la transacción se revierte.

## Alcance planificado

| Tabla | Filas objetivo |
|---|---:|
| `usuarios` | 420 |
| `docentes` | 20 |
| `estudiantes` | 200 |
| `apoderados` | 200 |
| `apoderado_estudiante` | 200 |
| `personal_colegio` | 10 |
| `cursos` | 13 |
| `matriculas_curso` | 2,600 |
| `evaluaciones` | 13 |
| `calificaciones` | 2,600 |
| `asistencias` | 2,600 |
| `horarios_clases` | 13 |
| `competencias_blenkir` | 13 |
| `logros_competencias_blenkir` | 2,600 |
| `conceptos_cobro_blenkir` | 2 |
| `cuentas_por_cobrar` | 400 |
| `pagos_blenkir` | 100 |
| `movimientos_caja_blenkir` | 100 |
| `comunicados_blenkir` | 3 |
| `auditoria_blenkir` | 3 |

El plan totaliza **12,110 filas**. Los 200 estudiantes ficticios pertenecen al grado/sección `5to Secundaria / A`; los 13 cursos de ese grupo se asignan a docentes y tienen matrícula, evaluación, calificación, asistencia, horario y competencia asociada. Cada estudiante tiene una cuenta de apoderado y se generan dos conceptos/cuentas de cobro. La mitad de las cuentas de matrícula tiene un pago ficticio, con su movimiento de caja asociado.

No existe una tabla de períodos académicos en este modelo. Los valores de período usan las columnas de texto que sí existen en cursos, cobros y logros. El conjunto sintético se identifica mediante códigos/referencias con prefijo `TST26` y nombres de usuario `test_seed_26_`. Reserva esos prefijos y rangos DNI (`95`–`98` millones) para este cargador.

## Requisitos

- Java 17 y Maven 3.9 o superior.
- PostgreSQL accesible desde el proceso.
- `DB_PASSWORD` en el entorno. Opcionalmente define `DB_URL` y `DB_USERNAME`; si no, se usa el host/base y usuario de desarrollo configurados por el cargador.
- Para `apply`, `TEST_DATA_PASSWORD` con al menos 12 caracteres. Se utiliza únicamente en memoria y se hashea con `BCryptPasswordEncoder`, el mismo mecanismo de `SecurityConfig`; nunca se guarda en texto plano.

No habilites simultáneamente el seeder de demostración (`app.demo.enabled=true`) y este cargador.

## Validar y revisar el plan

En PowerShell, desde la raíz del proyecto:

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/blenkir_ssacb"
$env:DB_USERNAME = "blenkir_app"
$env:DB_PASSWORD = "<contraseña de PostgreSQL>"

.\database\test-data\run.ps1
```

El modo por defecto es `plan`, de solo lectura. Comprueba el esquema real y muestra el recuento objetivo por tabla. Si no puede conectarse, falta una tabla/columna o una restricción esperada, se detiene sin insertar. Revisa y aprueba el plan antes de ejecutar la carga.

## Aplicar después de aprobar

Conserva las variables de conexión de arriba y configura la contraseña ficticia de las cuentas:

```powershell
$env:TEST_DATA_PASSWORD = "<contraseña ficticia segura, mínimo 12 caracteres>"
$env:TEST_DATA_APPROVED = "YES"
.\database\test-data\run.ps1 -Mode apply
```

La aplicación omite conflictos UNIQUE y no actualiza filas existentes. Las tablas sin una restricción natural de unicidad se comprueban por su marcador estable antes de insertarse; el bloqueo transaccional evita duplicados por ejecuciones concurrentes de este cargador. Al final se verifican los recuentos en las 20 tablas antes del `COMMIT`.

La misma `TEST_DATA_PASSWORD` debe conservarse para las cuentas creadas en una ejecución anterior: por diseño, las credenciales de filas ya existentes no se reemplazan en una segunda ejecución.

## Verificar la carga inicial

Las consultas siguientes verifican los recuentos de la carga inicial (13 cursos). Si ya ejecutaste la redistribución descrita más abajo, usa la revisión académica de solo lectura para ver sus 130 cursos y las nuevas cohortes.

Conéctate a la misma base y ejecuta consultas de solo lectura:

```sql
SELECT 'usuarios' AS tabla, COUNT(*) AS filas
FROM usuarios WHERE username LIKE 'test_seed_26_%'
UNION ALL
SELECT 'docentes', COUNT(*) FROM docentes WHERE codigo LIKE 'TST26-D%'
UNION ALL
SELECT 'estudiantes', COUNT(*) FROM estudiantes WHERE codigo LIKE 'TST26-S%'
UNION ALL
SELECT 'apoderados', COUNT(*) FROM apoderados WHERE dni LIKE '96000%'
UNION ALL
SELECT 'apoderado_estudiante', COUNT(*)
FROM apoderado_estudiante l
JOIN estudiantes e ON e.id = l.estudiante_id
WHERE e.codigo LIKE 'TST26-S%'
UNION ALL
SELECT 'personal_colegio', COUNT(*) FROM personal_colegio WHERE dni LIKE '95000%'
UNION ALL
SELECT 'cursos', COUNT(*) FROM cursos WHERE codigo LIKE 'TST26-C%'
UNION ALL
SELECT 'matriculas_curso', COUNT(*)
FROM matriculas_curso m
JOIN estudiantes e ON e.id = m.estudiante_id
JOIN cursos c ON c.id = m.curso_id
WHERE e.codigo LIKE 'TST26-S%' AND c.codigo LIKE 'TST26-C%'
UNION ALL
SELECT 'evaluaciones', COUNT(*) FROM evaluaciones WHERE nombre LIKE 'TST26 Evaluación %'
UNION ALL
SELECT 'calificaciones', COUNT(*)
FROM calificaciones g
JOIN evaluaciones v ON v.id = g.evaluacion_id
WHERE v.nombre LIKE 'TST26 Evaluación %'
UNION ALL
SELECT 'asistencias', COUNT(*)
FROM asistencias a JOIN cursos c ON c.id = a.curso_id
WHERE c.codigo LIKE 'TST26-C%'
UNION ALL
SELECT 'horarios_clases', COUNT(*)
FROM horarios_clases h JOIN cursos c ON c.id = h.curso_id
WHERE c.codigo LIKE 'TST26-C%'
UNION ALL
SELECT 'competencias_blenkir', COUNT(*)
FROM competencias_blenkir WHERE nombre LIKE 'TST26 Competencia %'
UNION ALL
SELECT 'logros_competencias_blenkir', COUNT(*)
FROM logros_competencias_blenkir r
JOIN competencias_blenkir b ON b.id = r.competencia_id
WHERE b.nombre LIKE 'TST26 Competencia %'
UNION ALL
SELECT 'conceptos_cobro_blenkir', COUNT(*)
FROM conceptos_cobro_blenkir WHERE nombre LIKE 'TST26 %'
UNION ALL
SELECT 'cuentas_por_cobrar', COUNT(*)
FROM cuentas_por_cobrar a
JOIN estudiantes e ON e.id = a.estudiante_id
WHERE e.codigo LIKE 'TST26-S%' AND a.periodo = 'TST26-2026-II'
UNION ALL
SELECT 'pagos_blenkir', COUNT(*) FROM pagos_blenkir WHERE referencia LIKE 'TST26-PAY-%'
UNION ALL
SELECT 'movimientos_caja_blenkir', COUNT(*)
FROM movimientos_caja_blenkir m
JOIN pagos_blenkir p ON p.id = m.pago_id
WHERE p.referencia LIKE 'TST26-PAY-%'
UNION ALL
SELECT 'comunicados_blenkir', COUNT(*)
FROM comunicados_blenkir WHERE titulo LIKE 'TST26 Comunicado %'
UNION ALL
SELECT 'auditoria_blenkir', COUNT(*)
FROM auditoria_blenkir WHERE referencia LIKE 'TST26:%';
```

Para revisar rápidamente las demás tablas de la carga inicial, consulta los recuentos objetivos en la tabla de alcance y filtra por los marcadores indicados; los mismos recuentos se comprueban automáticamente dentro de la transacción de carga.

## Limpiar únicamente los datos del cargador

La limpieza también requiere aprobación explícita. Borra dependencias en orden y limita los candidatos a los códigos, referencias y períodos reservados por este cargador:

```powershell
$env:TEST_DATA_APPROVED = "YES"
.\database\test-data\run.ps1 -Mode clean
```

La limpieza no vacía tablas ni elimina datos sin marcadores. Las filas referenciadas por información ajena que impida la eliminación causarán un error y rollback, no se borrarán en cascada. Verifica los conteos de las consultas anteriores (deben quedar en cero) antes de retirar la aprobación.

## Revisar la distribución académica TST26

La carga inicial deja a los 200 estudiantes de prueba en `5to Secundaria / A`. El siguiente procedimiento redistribuye **solo** esos registros identificados por `TST26` en 5 grados y secciones A/B, asigna los 13 cursos a cada cohorte y recalcula sus relaciones académicas. No toca al alumno, curso ni registros de demostración sin el marcador.

### Plan de solo lectura

Configura `DB_URL`, `DB_USERNAME` y `DB_PASSWORD` como en los ejemplos anteriores. Ejecuta:

```powershell
.\database\test-data\repair-academic-distribution.ps1 -Mode Plan
```

El plan ejecuta [review-academic-distribution.sql](./review-academic-distribution.sql) dentro de una transacción `READ ONLY`. Muestra los recuentos actuales, las cohortes objetivo, los cursos faltantes y conflictos con cursos no marcados. No crea respaldos, tablas temporales ni datos.

### Alcance propuesto de la corrección

| Operación | Filas estimadas antes de aplicar |
|---|---:|
| Actualizar grado/sección de estudiantes `TST26` | 200 |
| Conservar y actualizar cursos actuales | 13 |
| Insertar cursos para las otras cohortes | 117 |
| Insertar evaluaciones | 117 |
| Actualizar horarios existentes | 13 |
| Insertar horarios | 117 |
| Insertar competencias | 117 |
| Reasignar matrículas | 2,600 |
| Reasignar calificaciones | 2,600 |
| Reasignar asistencias | 2,600 |
| Reasignar logros de competencias | 2,600 |

El resultado será de 20 estudiantes por cada combinación de cinco grados (`1ro` a `5to Secundaria`) y secciones A/B; cada uno tendrá 13 cursos. Se mantienen los 13 cursos originales y sus identificadores. Se crean 117 cursos adicionales, con evaluación, horario y competencia por curso. La distribución genera notas en escala 0–20, estados de asistencia admitidos por la aplicación y asignaciones docentes dentro de su especialidad. Se usa el período existente `2026-II`; el esquema no tiene una tabla de períodos.

La transacción se detiene si la base no coincide con los marcadores y cardinalidades esperados, aparece un curso no marcado que colisiona por materia/grado/sección/período, falta un docente especializado, falla alguna relación única o una validación de consistencia. No se eliminan filas ni se sobrescriben registros ajenos a `TST26`.

### Aplicar solo después de revisar y aprobar

La opción `Apply` requiere una aprobación explícita en el entorno. Antes de ejecutar cualquier modificación crea un respaldo PostgreSQL en formato custom bajo `%LOCALAPPDATA%\Blenkir\database-backups\academic-distribution`, restringe los permisos del directorio a la cuenta actual y valida que `pg_restore --list` pueda leer el respaldo. Si falla el respaldo, no comienza la corrección.

```powershell
$env:TEST_DATA_APPROVED = "YES"
.\database\test-data\repair-academic-distribution.ps1 -Mode Apply
```

La corrección se ejecuta en una única transacción `SERIALIZABLE`, protegida por un bloqueo de PostgreSQL, y verifica las relaciones antes de confirmar. Si hay un error, PostgreSQL revierte la transacción. Conserva la ruta del respaldo que informa el script. Al finalizar:

```powershell
Remove-Item Env:TEST_DATA_APPROVED
.\database\test-data\repair-academic-distribution.ps1 -Mode Plan
```

El segundo comando permite comprobar los 10 grupos y que el número de cursos faltantes y conflictos sea cero.

### Verificar manualmente y limpiar

La consulta `review-academic-distribution.sql` es de solo lectura. Para comprobar matrícula, notas, asistencia y resultados por estudiante/grupo, usa:

```sql
SELECT s.grado, s.seccion,
       COUNT(*) AS estudiantes,
       SUM((SELECT COUNT(*) FROM matriculas_curso m WHERE m.estudiante_id = s.id)) AS matriculas,
       SUM((SELECT COUNT(*) FROM calificaciones g WHERE g.estudiante_id = s.id)) AS calificaciones,
       SUM((SELECT COUNT(*) FROM asistencias a WHERE a.estudiante_id = s.id)) AS asistencias,
       SUM((SELECT COUNT(*) FROM logros_competencias_blenkir r WHERE r.estudiante_id = s.id)) AS resultados
FROM estudiantes s
WHERE s.codigo LIKE 'TST26-S%'
GROUP BY s.grado, s.seccion
ORDER BY s.grado, s.seccion;
```

Los conteos exactos de cada tabla también aparecen en `review-academic-distribution.sql` y en la verificación transaccional del script.

La limpieza disponible en `run.ps1 -Mode clean` borra exclusivamente el conjunto ficticio completo identificado por los marcadores reservados `TST26`, `test_seed_26_` y los rangos de DNI documentados. También reconoce los códigos de curso adicionales de esta distribución. Requiere `TEST_DATA_APPROVED=YES` y elimina las relaciones en orden; no uses esa limpieza si deseas conservar alguna parte del conjunto sintético:

```powershell
$env:TEST_DATA_APPROVED = "YES"
.\database\test-data\run.ps1 -Mode clean
Remove-Item Env:TEST_DATA_APPROVED
```
