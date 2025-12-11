FROM eclipse-temurin:17-jdk

WORKDIR /app

# Usa el nombre REAL del jar que te genera Maven
COPY target/usei-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
