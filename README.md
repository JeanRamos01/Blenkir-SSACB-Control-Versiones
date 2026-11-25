# SSACB Blenkir — Plataforma escolar ampliada con nuevos modulos.

Version nueva de blenkir actualizada con ramas y commits.

## Tecnología solicitada

- Frontend: HTML, CSS, JavaScript/TypeScript, Angular y Bootstrap 5.
- Backend: Java 17 LTS + Spring Boot + Spring Security + API REST.
- Base de datos: PostgreSQL.
- Autenticación: JWT y control de acceso por roles.

## Roles incluidos

1. **Administrador**: estudiantes, docentes, cursos, apoderados, evaluación, calificaciones, asistencia y reportes.
2. **Secretaría**: padrón/matrículas, horarios de consulta, tesorería, caja y exportaciones preparatorias; no administra usuarios ni personal.
3. **Docente**: sus cursos, horarios, competencias, evaluaciones, calificaciones, asistencia y reportes.
4. **Estudiante**: consulta únicamente su propia información académica y horario.
5. **Apoderado**: consulta únicamente a estudiantes previamente vinculados y autorizados por la institución.

## Módulos incorporados desde la referencia SIGA UC

| Módulo | Funcionalidad en Blenkir |
| --- | --- |
| Matrícula y padrón | Alta/reactivación de matrícula por curso, listado de alumnos y estado |
| Carga curricular | Asignación de docente a cursos, grados y secciones |
| Horarios | Sesiones por día, aula y horas; consulta filtrada para docente/estudiante |
| Personal | Alta/edición de personal y administración de cuentas de Secretaría |
| Competencias | Escala AD/A/B/C y conclusión descriptiva por estudiante y periodo |
| Tesorería | Conceptos, cuentas pendientes, pagos parciales, vencimientos y medios de pago |
| Caja | Ingresos automáticos por pagos, egresos/ingresos manuales y saldo acumulado |
| Comunicados | Publicación por rol o a toda la comunidad educativa |
| Auditoría | Bitácora de nuevas operaciones, visible al administrador |
| Preparación SIAGIE | Descarga de CSV de padrón, matrículas y competencias (sin envío oficial) |

**Exclusión expresa:** no hay generación, lectura, credenciales ni control de asistencia por códigos QR. Se mantiene la asistencia manual existente.

### Regla de seguridad para DNI

El DNI **no es una contraseña**. En el portal del apoderado se puede escribir el DNI del estudiante para buscarlo, pero el backend solo devuelve la información cuando ese estudiante ya está vinculado a la cuenta autenticada del apoderado. Conocer el DNI de un alumno no permite acceder a sus notas.

## Inicio rápido con PostgreSQL usando Docker

1. Instalar Docker Desktop, Java 17+, Maven 3.9+, Node.js 20+ y npm.
2. Copiar `.env.example` a `.env`, asignar contraseñas y un JWT_SECRET en Base64 de al menos 32 bytes. El servidor necesita `DB_PASSWORD` y `JWT_SECRET` exportadas como variables de entorno o configuradas en el IDE; Spring Boot no lee `.env` automáticamente.
3. En esta carpeta ejecutar:

```bash
docker compose up -d postgres pgadmin
```

PostgreSQL quedará disponible en `localhost:5432`:

- Base: `blenkir_ssacb`
- Usuario: `blenkir_app`
- Clave: la indicada en `POSTGRES_PASSWORD` de tu archivo `.env` (debe coincidir con `DB_PASSWORD`).

PgAdmin quedará en `http://localhost:5050`.

## Backend

Requisitos: Java 17 LTS (o superior compatible) y Maven 3.9+.

```bash
cd backend
mvn spring-boot:run
```

El backend se conecta mediante `backend/src/main/resources/application.yml`.
Es obligatorio configurar las credenciales en el entorno; el código ya no trae una clave JWT o una contraseña de PostgreSQL funcional por defecto:

```text
DB_URL=jdbc:postgresql://localhost:5432/blenkir_ssacb
DB_USERNAME=blenkir_app
DB_PASSWORD=...
JWT_SECRET=...
BOOTSTRAP_ADMIN_USERNAME=admin
BOOTSTRAP_ADMIN_PASSWORD=<clave_segura_de_al_menos_12_caracteres>
DEMO_DATA_ENABLED=false
```

Hibernate valida el esquema (`ddl-auto: validate`); no ejecuta migraciones automáticamente. La ampliación administrativa existente se describe en `database/03-modulos-administrativos-blenkir.sql`. Las alertas académicas requieren la migración aditiva `database/04-alertas-academicas.sql`, que crea solo una tabla e índices nuevos. Antes de desplegar esta versión, debe aplicarse una vez a cada base conectada a esta versión (incluida la producción existente), porque JPA valida la nueva tabla al arrancar. Para producción, requiere revisión, respaldo y autorización; nunca se ejecuta automáticamente.

## Frontend Angular

Requisitos: Node.js 20+ y npm.

```bash
cd frontend
npm install
npm start
```

Abrir `http://localhost:4200`.

## Credenciales de acceso para pruebas

La inicialización DEMO está desactivada de forma predeterminada. Se recomienda ejecutarla en una base PostgreSQL separada. En producción, solo debe habilitarse con autorización explícita: agrega registros de prueba visibles junto con los datos reales y no los elimina automáticamente.

Configura las variables de entorno en el servicio de pruebas de Render:

```text
DEMO_DATA_ENABLED=true
DEMO_ALLOW_EXISTING_DATA=true
DEMO_ADMIN_PASSWORD=<secreto de al menos 12 caracteres>
DEMO_SECRETARIA_PASSWORD=<secreto de al menos 12 caracteres>
DEMO_DOCENTE_PASSWORD=<secreto de al menos 12 caracteres>
DEMO_ESTUDIANTE_PASSWORD=<secreto de al menos 12 caracteres>
DEMO_APODERADO_PASSWORD=<secreto de al menos 12 caracteres>
ACADEMIC_RISK_THRESHOLD=11
```

Guarda los valores de contraseña únicamente como variables secretas del servicio; no los incluyas en el repositorio ni en logs. Los usuarios DEMO estables son `demo-admin`, `demo-secretaria`, `demo-docente`, `demo-estudiante-riesgo` y `demo-apoderado`. Las contraseñas las define quien administra el entorno DEMO y no se escriben en este README. Al habilitar DEMO, no configures `BOOTSTRAP_ADMIN_PASSWORD`; el administrador DEMO se crea desde `DEMO_ADMIN_PASSWORD`.

`DEMO_ALLOW_EXISTING_DATA` tiene valor predeterminado `false`. Para agregar este conjunto a una base poblada, habilita explícitamente ambas variables `DEMO_DATA_ENABLED=true` y `DEMO_ALLOW_EXISTING_DATA=true`. Antes de hacerlo, confirma que la migración de alertas ya se aplicó. La inicialización verifica colisiones con usuarios, DNI y códigos reservados; si encuentra alguna, o detecta una inicialización DEMO parcial, omite la inserción y no altera registros. Nunca se restablecen contraseñas ni se sobrescriben registros existentes.

Al inicializarse se crean esas cinco cuentas, un docente, un estudiante DEMO matriculado en Matemática, dos evaluaciones y calificaciones, asistencia y el vínculo autorizado con su apoderado. El promedio DEMO (9/20) queda por debajo del umbral configurado y genera una alerta. La inicialización es transaccional e idempotente: en reinicios conserva los datos y contraseñas ya creados. Si agregas el escenario DEMO a producción, esos registros de prueba permanecerán allí hasta que un administrador los gestione; el inicializador no los elimina.

El umbral se aplica al promedio aritmético de las calificaciones registradas del estudiante por curso y periodo. Se genera una alerta al guardar calificaciones que lo hagan caer por debajo del umbral. Las alertas no se envían por correo; se consultan desde las sesiones autorizadas del administrador/docente, el estudiante afectado y su apoderado vinculado. El esquema actual no mantiene sesiones revocables: después de restablecer una contraseña, los JWT emitidos anteriormente pueden seguir vigentes hasta expirar.

La tabla de alertas debe estar creada antes de desplegar esta versión porque JPA continúa validando el esquema. La migración no se ejecuta al arrancar: aplícala manualmente una sola vez a cada base que usará esta versión; hazlo primero en la base DEMO separada y verifica ahí el arranque. No despliegues la aplicación contra una base sin esa tabla. **No se ha verificado el despliegue real en Render desde este entorno.**

## Restablecimiento de contraseñas

El panel **Usuarios y claves** está disponible solo para ADMINISTRADOR. Permite buscar cuentas y establecer una contraseña nueva (mínimo 12 caracteres); se cifra con BCrypt y la auditoría registra quién hizo el cambio y a qué cuenta, nunca la contraseña. La contraseña nueva se muestra una sola vez en el navegador. Los JWT ya emitidos continúan vigentes hasta expirar; este cambio no modifica el flujo de autenticación ni revoca sesiones existentes.

## Cómo se conecta PostgreSQL

La conexión está centralizada en `application.yml`; no debe conectarse Angular directamente a PostgreSQL. La ruta correcta es:

**Angular → API REST Spring Boot → PostgreSQL**

Esto permite validar el rol del usuario antes de entregar datos académicos y evita exponer las credenciales de la base de datos en el navegador.

## Carpetas principales

- `frontend/`: nueva aplicación Angular.
- `backend/`: API REST Spring Boot.
- `database/`: scripts de creación y recomendaciones de PostgreSQL.
- `docs/`: decisiones de privacidad y uso por menores/apoderados.
- `legacy-prototype/`: avance HTML/CSS/JS original.
- `database/03-modulos-administrativos-blenkir.sql`: ampliación de PostgreSQL.
- `docs/ADAPTACION_SIGA_UC.md`: matriz de módulos, permisos y limitaciones conocidas.

## Comprobación de calidad

```bash
cd backend && mvn test
cd ../frontend && npm ci && npm run build
```

El nuevo `.gitignore` excluye dependencias, compilaciones, secretos, volcados de base de datos, exportaciones con datos personales y archivos temporales. Nunca incluyas datos reales de menores en el repositorio.

Rol	Usuario	Variable de Render	Clave temporal
Administrador	demo-admin	DEMO_ADMIN_PASSWORD	c680691d99d156fc0569774c
Secretaría	demo-secretaria	DEMO_SECRETARIA_PASSWORD	55926959078a9ca06269ccde
Docente	demo-docente	DEMO_DOCENTE_PASSWORD	bdfee9ae3f88ea0f7f5f333e
Estudiante	demo-estudiante-riesgo	DEMO_ESTUDIANTE_PASSWORD	03bdd6e47b7a1f92ed78b874
Apoderado	demo-apoderado	DEMO_APODERADO_PASSWORD	370195963856e288443c2c27