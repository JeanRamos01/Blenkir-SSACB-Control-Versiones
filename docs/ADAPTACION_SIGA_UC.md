# Adaptación funcional: SIGA UC → Colegio Blenkir

## Criterios de integración

- **Identidad propia**: no se cambiaron `frontend/src/assets/logo.png`, `frontend/src/assets/colegio.jpg` ni los colores institucionales principales (azul marino y naranja). Se conserva `legacy-prototype/`.
- **Stack preservado**: Angular 18, Bootstrap 5, TypeScript, Java 17, Spring Boot 3.4.5, Spring Security, PostgreSQL 16 y JWT. El Express de SIGA UC es únicamente material de referencia y no se incorporó al proyecto.
- **Base de datos propia**: no se importa la base `siga_uc` ni personas/contraseñas de ese proyecto; las tablas nuevas se relacionan con `estudiantes`, `cursos` y `usuarios` de Blenkir.
- **Sin QR**: no se utilizan credenciales, cámaras, generación/lectura de QR ni marcación por QR. La asistencia manual original sigue disponible.

## Correspondencia de módulos

| SIGA UC | En Blenkir | Rutas frontend | API Spring Boot |
|---|---|---|---|
| Dashboard por rol | Panel principal, ampliado para Secretaría | `/dashboard` | `GET /api/dashboard` |
| Gestión de alumnos y padrón | Estudiantes + matrículas por curso | `/students`, `/enrollments` | `/api/students`, `/api/enrollments` |
| Carga curricular | Asignación de docentes a cursos | `/curriculum` | `/api/curriculum` |
| Horarios del estudiante | Horarios por curso y aula | `/schedules` | `/api/schedules` |
| Personal | Personal administrativo y cuentas Secretaría | `/staff` | `/api/staff`, `/api/secretariat-users` |
| Ficha del estudiante (docente) | Perfil, notas y asistencias de sus estudiantes | `/student-records` | `/api/students`, `/api/academic/*` |
| Calificaciones/competencias | Notas numéricas existentes + niveles AD/A/B/C | `/grades`, `/competencies` | `/api/academic`, `/api/competencies` |
| Asistencia QR | **No incorporada**; queda asistencia manual | `/attendance` | `/api/academic/attendance` |
| Tesorería/pensiones | Conceptos, cargos, saldos, pagos parciales | `/treasury` | `/api/finance/concepts`, `/charges`, `/payments` |
| Caja | Libro de movimientos y balance acumulado | `/cash` | `/api/finance/cash` |
| Notificaciones | Comunicados internos por rol | `/notifications` | `/api/notifications` |
| Auditoría | Bitácora de las operaciones incorporadas | `/audit` | `/api/audit` |
| Reportes SIAGIE | CSV preparatorios con datos de Blenkir | `/siagie` | `/api/exports/siagie/*.csv` |
| Reportes académicos | Se mantiene el módulo de Blenkir | `/reports` | endpoints académicos de Blenkir |
| Acceso administrativo | Administrador, Secretaría, Docente, Estudiante, Apoderado | `/login` | `/api/auth` |

## Esquema PostgreSQL agregado

| Tabla | Relación principal |
|---|---|
| `personal_colegio` | Empleados no docentes; el usuario de Secretaría es independiente |
| `horarios_clases` | `curso_id → cursos.id` |
| `competencias_blenkir` | `curso_id → cursos.id` |
| `logros_competencias_blenkir` | `competencia_id`, `estudiante_id`, `periodo` únicos |
| `conceptos_cobro_blenkir` | Conceptos financieros de la institución |
| `cuentas_por_cobrar` | `estudiante_id`, `concepto_id` y `periodo`; pagos parciales |
| `pagos_blenkir` | Abonos vinculados con una cuenta por cobrar |
| `movimientos_caja_blenkir` | Entrada automática por pago o asiento manual autorizado |
| `comunicados_blenkir` | Destinatario por rol o `TODOS` |
| `auditoria_blenkir` | Usuario, acción, entidad, referencia y fecha |

El archivo `database/03-modulos-administrativos-blenkir.sql` define una migración idempotente de **creación de tablas** sobre un esquema Blenkir ya inicializado. No elimina ni cambia tablas existentes. No ejecutarlo sobre una base vacía sin antes crear las entidades originales de SSACB. Para entornos nuevos se puede iniciar en desarrollo con `JPA_DDL_AUTO=update`; para producción conviene aplicar migraciones versionadas y validar el esquema.

## Autorización resumida

| Acción | Admin | Secretaría | Docente | Estudiante | Apoderado |
|---|:---:|:---:|:---:|:---:|:---:|
| Matrículas | ✓ | ✓ | – | – | – |
| Cambiar carga curricular / horarios | ✓ | Consulta | Consulta propia | Consulta propia | – |
| Personal y cuentas de Secretaría | ✓ | – | – | – | – |
| Tesorería y pagos | ✓ | ✓ | – | – | – |
| Caja | ✓ | Consulta | – | – | – |
| Movimientos manuales de caja | ✓ | – | – | – | – |
| Competencias y logros | ✓ | – | En cursos propios | – | – |
| Comunicados | Publicar/leer | Publicar/leer | Leer destinatarios | Leer destinatarios | Leer destinatarios |
| Bitácora | ✓ | – | – | – | – |
| CSV preparatorios | ✓ | ✓ | – | – | – |

Los permisos se comprueban en el servidor con Spring Security; el ocultamiento de enlaces en Angular es solo una ayuda de navegación. Docentes pueden consultar los estudiantes asociados a sus cursos. Apoderados conservan la validación de vínculo autorizado de Blenkir.

## Instalación desde una base Blenkir existente

1. Respaldar la base PostgreSQL original. No restaurar ni importar `siga_uc.sql`.
2. Revisar `.env.example` y crear `.env` con contraseñas distintas y un JWT secreto Base64 de mínimo 32 bytes. Establecer iguales `POSTGRES_PASSWORD` y `DB_PASSWORD`.
3. Ejecutar `docker compose up -d postgres pgadmin` si se usa Docker. Exportar `DB_PASSWORD`, `JWT_SECRET` y demás variables también al proceso Spring Boot; `docker compose` solo consume el archivo `.env` para sus propios servicios.
4. En el primer arranque de desarrollo, usar `JPA_DDL_AUTO=update`; alternativamente, ejecutar el SQL de ampliación una vez que las tablas originales existan.
5. En una base vacía, configurar `BOOTSTRAP_ADMIN_PASSWORD` (12+ caracteres) **o** activar temporalmente `DEMO_DATA_ENABLED=true`, nunca ambos.
6. `cd backend && mvn test && mvn spring-boot:run`.
7. `cd frontend && npm ci && npm start`, abrir `http://localhost:4200`.
8. Crear cursos, docentes y estudiantes; completar carga curricular, matrícula, horarios y conceptos de pago.

## Pruebas funcionales sugeridas

1. Como administrador, registrar concepto de S/ 100, generar una cuenta y abonar S/ 30; comprobar saldo S/ 70 y movimiento de ingreso de S/ 30.
2. Intentar un abono superior al saldo; la API debe responder 400 y no registrar caja.
3. Como Secretaría, listar matrículas, pagar una cuenta y consultar caja; comprobar que no puede administrar personal ni crear conceptos.
4. Como docente, comprobar que solo ve cursos/estudiantes propios y que no puede registrar competencias de otro curso.
5. Como estudiante, consultar horario y verificar que no aparecen clases de otros estudiantes de distinta sección.
6. Desactivar una cuenta Secretaría; un JWT ya emitido no debe seguir autorizando solicitudes.
7. Exportar los tres CSV, abrirlos con UTF-8 y comprobar datos y encabezados antes de cualquier tratamiento SIAGIE.
8. Verificar que ningún menú ni endpoint nuevo dependa de QR.

La URL del API utilizada por Angular se configura en `frontend/src/environments/environment.ts`; ajustarla antes de generar el paquete frontend para otro servidor. El navegador nunca debe conocer la contraseña PostgreSQL.

## Límites de esta primera integración

La referencia SIGA UC también contiene flujos especializados que **no se replican todavía 1:1**: cálculo final de competencias por bimestre, becas/descuentos, planes de pensiones automáticos, facturación electrónica, apertura y cierre formal de turnos de caja, correo SMTP, recuperación por correo, firma de actas y conectores oficiales de SIAGIE. Esta entrega brinda módulos funcionales de registro y consulta, no homologación normativa ni equivalencia exacta de cada subproceso.

La bitácora registra los cambios de los módulos incorporados; no instrumenta automáticamente todas las operaciones de los controladores originales. La caja muestra un saldo contable acumulado, no un arqueo físico por turno. Antes de utilizar datos reales de menores o aceptar dinero en producción, agregar pruebas de integración con PostgreSQL, políticas de respaldo, auditoría de accesos y validación contable y legal por la institución.
