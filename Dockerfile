# ----- STAGE 1: Build con Maven -----
FROM maven:3.9.4-eclipse-temurin-17 AS build

WORKDIR /app

# Copiamos pom.xml y descargamos dependencias primero
COPY pom.xml .
RUN mvn dependency:go-offline

# Copiamos todo el proyecto
COPY . .

# Generamos el jar
RUN mvn clean package -DskipTests

# ----- STAGE 2: Imagen final ligera -----
FROM eclipse-temurin:17-jdk

WORKDIR /app

# Copiar el JAR generado desde el stage anterior
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
