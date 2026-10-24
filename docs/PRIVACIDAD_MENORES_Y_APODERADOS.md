# Privacidad, menores de edad y acceso a calificaciones

Este documento resume criterios de diseño; no sustituye asesoría legal para la puesta en producción.

## Decisión aplicada en el sistema

- El estudiante puede consultar **solo su propia información** con cuenta individual.
- El apoderado tiene una cuenta independiente y puede consultar a los estudiantes que la institución haya vinculado previamente a su identidad.
- El DNI del estudiante sirve como dato de búsqueda, **nunca como credencial ni como autorización suficiente**.
- Para menores, la institución debe registrar y verificar padre, madre, tutor o apoderado y mantener evidencia del vínculo/autorización.
- Para menores de 14 años se recomienda que la habilitación del portal del estudiante sea aprobada y gestionada junto con quien ejerza patria potestad o tutela.
- Para adolescentes de 14 a 17 años puede existir cuenta personal del estudiante, conservando el acceso separado del padre/madre/apoderado autorizado.
- Los usuarios deben recibir solo la información necesaria según su rol; docentes no deben acceder a estudiantes fuera de los cursos asignados.

## Base normativa y referencias oficiales consultadas

- Ley N.° 29733, Ley de Protección de Datos Personales, y su Reglamento aprobado por D.S. N.° 016-2024-JUS.
- Artículo 25 del nuevo Reglamento: protección del interés superior del niño en servicios digitales; distingue consentimiento para mayores de 14 y menores de 14 años.
- SIAGIE del MINEDU indica que familiares/apoderados pueden contar con usuario y clave y exige confidencialidad de credenciales.
- Material oficial de SIAGIE señala que padres y alumnos pueden consultar notas, mostrando que el acceso del estudiante a su propia información no está prohibido de forma general.
- La Constancia de Logros de Aprendizaje del MINEDU diferencia el trámite personal desde los 16 años y por representante cuando el estudiante es menor de 16; esa regla corresponde a dicho documento oficial y no significa que un portal escolar deba bloquear todas las notas a menores de 16.

## Principios que el diseño debe cumplir

1. **Finalidad**: usar datos escolares para gestión y seguimiento académico.
2. **Proporcionalidad/minimización**: mostrar únicamente datos necesarios.
3. **Seguridad**: contraseña segura, cifrado, control por roles, logs y copias de seguridad.
4. **Acceso restringido**: un apoderado solo ve a sus dependientes autorizados.
5. **Transparencia**: informar a estudiantes y apoderados qué datos se usan y para qué.
6. **Interés superior del niño**: evitar exposición pública o acceso por simple conocimiento del DNI.

## Recomendación antes de producción

La institución debería aprobar una política de privacidad, procedimiento de alta/baja de apoderados, recuperación de contraseña, registro de consentimientos cuando corresponda, plazo de conservación y procedimiento de atención de derechos sobre datos personales.
