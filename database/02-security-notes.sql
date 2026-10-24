-- Recomendaciones para producción (ejecutar según la política de infraestructura):
-- 1) Use una contraseña distinta a la de desarrollo.
-- 2) No exponga el puerto 5432 a Internet.
-- 3) Permita conexión solo desde el servidor backend.
-- 4) Habilite copias de seguridad y cifrado TLS en la conexión.
-- 5) El usuario de la aplicación NO debe ser superusuario.

ALTER ROLE blenkir_app NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT;
