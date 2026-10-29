-- Crear únicamente si NO se utiliza el servicio PostgreSQL de docker-compose.
-- Abrir Query Tool conectado a la base "postgres" con una cuenta administradora.
-- REEMPLAZAR la contraseña de ejemplo por una generada para Blenkir antes de ejecutar.
-- Este script se ejecuta UNA vez; no es una migración idempotente.

CREATE ROLE blenkir_app LOGIN PASSWORD 'blenkir_dev_2026';
CREATE DATABASE blenkir_ssacb OWNER blenkir_app ENCODING 'UTF8';

-- Configurar DB_PASSWORD en el entorno Spring Boot con esa misma contraseña.
