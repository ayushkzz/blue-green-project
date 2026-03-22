FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY app/target/blue-green-app-1.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
