# ------------ STAGE 1: COMPILAR MAVEN ----------------

FROM maven:3.9.5-eclipse-temurin-17 AS builder
WORKDIR /app

# Copiamos el pom.xml que está dentro de Backend/
COPY Backend/pom.xml .

# Descargar dependencias
RUN mvn dependency:go-offline

# Copiar el código fuente
COPY Backend/src ./src

# Compilar el proyecto
RUN mvn clean package -DskipTests



# ------------ STAGE 2: EJECUTAR SPRING BOOT ----------------

FROM eclipse-temurin:17-jdk
WORKDIR /app

# Copiar el jar compilado desde el stage 1
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
