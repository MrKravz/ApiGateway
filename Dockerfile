FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /build
COPY pom.xml .
RUN mvn -B -q -e -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -B package -DskipTests
FROM eclipse-temurin:21-jre-alpine
WORKDIR /java_course
COPY --from=builder /build/target/*.jar api_gateway.jar
ENTRYPOINT ["java", "-jar", "/java_course/api_gateway.jar"]