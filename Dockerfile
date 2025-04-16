FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY StudentDistributionBot/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
