FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY target/usei-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
