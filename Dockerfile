# syntax=docker/dockerfile:1

# ---------- Etapa 1: compilar el proyecto ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B -DskipTests package

# ---------- Etapa 2: ejecutar la aplicación ----------
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --create-home spring
COPY --from=build /app/target/*.jar app.jar
USER spring
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]