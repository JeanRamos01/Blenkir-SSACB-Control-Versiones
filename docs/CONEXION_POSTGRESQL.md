# Conexión correcta de PostgreSQL con Spring Boot

No conectes Angular directamente a PostgreSQL. La conexión segura es:

`Angular -> Spring Boot API -> PostgreSQL`

## 1. Crear base y usuario

```sql
CREATE USER blenkir_app WITH PASSWORD 'TU_CLAVE_SEGURA';
CREATE DATABASE blenkir_ssacb OWNER blenkir_app ENCODING 'UTF8';
```

## 2. Dependencia Maven

En `backend/pom.xml` ya está incluida:

```xml
<dependency>
  <groupId>org.postgresql</groupId>
  <artifactId>postgresql</artifactId>
  <scope>runtime</scope>
</dependency>
```

También se usa Spring Data JPA:

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```

## 3. Configuración Spring Boot

Archivo: `backend/src/main/resources/application.yml`

```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/blenkir_ssacb}
    username: ${DB_USERNAME:blenkir_app}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: ${JPA_DDL_AUTO:update}
```

Configura `DB_PASSWORD` y `JWT_SECRET` como variables de entorno, no en Git. Los valores ya son obligatorios. Para Docker, establece `POSTGRES_PASSWORD` en `.env` con la misma clave que `DB_PASSWORD`.

## 4. Ejemplo de entidad

```java
@Entity
@Table(name = "estudiantes")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String dni;
}
```

Spring Data JPA traduce la entidad a una tabla PostgreSQL y permite trabajar mediante repositorios.

## 5. Ejemplo de repositorio

```java
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByDni(String dni);
}
```

## 6. Ejecución

```bash
cd backend
mvn spring-boot:run
```

Si PostgreSQL está levantado y las variables son correctas, Spring Boot abrirá la conexión y creará las tablas del prototipo.

## 7. Producción

- Cambiar `ddl-auto=update` por `validate` y usar migraciones.
- Usar contraseña fuerte y secretos fuera del repositorio.
- No abrir PostgreSQL directamente a Internet.
- Usar TLS entre backend y base de datos cuando estén en servidores distintos.
- Hacer copias de seguridad periódicas.
- Registrar auditoría de accesos y cambios de calificaciones.
