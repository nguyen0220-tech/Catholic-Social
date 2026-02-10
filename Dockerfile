# Stage 1: Build Maven + JDK 21
FROM maven:3.9.10-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# Stage 2: Run với JRE nhẹ
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy đúng file jar build ra
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Xmx256m", "-Xms128m", "-jar", "app.jar"]
