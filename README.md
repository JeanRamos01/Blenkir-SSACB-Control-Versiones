# SSACB Blenkir — Plataforma escolar ampliada con módulos tipo SIGA UC

Esta edición **amplía Blenkir** tomando como referencia las funciones de SIGA UC, conservando el proyecto original, el logo, la fotografía del colegio, el azul marino y naranja y las tecnologías de Blenkir. El prototipo se conserva en `legacy-prototype/`. No contiene módulos QR ni código del backend Express de SIGA.

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

Durante desarrollo, Hibernate crea/actualiza las tablas con `JPA_DDL_AUTO=update`. La ampliación se describe también en `database/03-modulos-administrativos-blenkir.sql`; úsala para revisar/aplicar los cambios a una base Blenkir que ya tenga el esquema original. Para producción: aplicar migraciones verificadas en copia, usar `validate` y realizar un respaldo previo.

## Frontend Angular

Requisitos: Node.js 20+ y npm.

```bash
cd frontend
npm install
npm start
```

Abrir `http://localhost:4200`.

## Usuarios iniciales de demostración

**Solo si `DEMO_DATA_ENABLED=true`** y la base está vacía se crean estos datos de demostración:

- Administrador: `admin / admin123`
- Docente: `docente / docente123`
- Estudiante: `alumno / alumno123`
- Apoderado: `apoderado / apoderado123`
- Secretaría: `secretaria / secretaria123`

También crea un estudiante de prueba con DNI `72123456`, vinculado al apoderado de prueba. **El modo demostración está desactivado de forma predeterminada.** Para comenzar sin datos de prueba, proporciona `BOOTSTRAP_ADMIN_PASSWORD` (mínimo 12 caracteres) con la base vacía. No habilites ambos métodos de inicialización al mismo tiempo.

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
