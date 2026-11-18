
# Etapa 1: Compilar el backend
FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /app

COPY backend/pom.xml .
COPY backend/src ./src

RUN mvn clean package -DskipTests

# Etapa 2: Ejecutar Spring Boot
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 10000

ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-10000} -jar app.jar"]

# Archivo Dockerfile para construir y ejecutar una aplicación Spring Boot con Maven. La primera etapa compila el backend y la segunda etapa ejecuta la aplicación en un contenedor con Java Runtime Environment (JRE).