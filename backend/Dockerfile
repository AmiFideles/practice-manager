FROM maven:3.8.8-eclipse-temurin-21-alpine AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn package -DskipTests

FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

# Копируем собранный jar
COPY --from=build /app/target/practice-manager-0.0.1-SNAPSHOT.jar ./

EXPOSE 8080

CMD ["java", "-jar", "practice-manager-0.0.1-SNAPSHOT.jar"]
