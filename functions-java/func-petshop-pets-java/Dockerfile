# Dockerfile for Pets Function Microservice
# Multi-stage build

# Stage 1: Build all modules
FROM eclipse-temurin:21-jdk-alpine AS build

RUN apk add --no-cache maven

WORKDIR /app

# Copy parent pom first for dependency resolution
COPY pom.xml .

# Copy ALL modules (required by parent pom)
COPY petshop-functions-shared ./petshop-functions-shared
COPY func-petshop-auth-java ./func-petshop-auth-java
COPY func-petshop-catalog-java ./func-petshop-catalog-java
COPY func-petshop-customers-java ./func-petshop-customers-java
COPY func-petshop-orders-java ./func-petshop-orders-java
COPY func-petshop-pets-java ./func-petshop-pets-java
COPY func-petshop-scheduling-java ./func-petshop-scheduling-java

# Build shared module first, then this module with docker profile for executable JAR
RUN mvn clean install -pl petshop-functions-shared -DskipTests && \
    mvn clean package -pl func-petshop-pets-java -am -Pdocker -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="petshop@example.com"
LABEL description="Pet Shop Pets Function - Spring Boot Microservice"
LABEL version="1.0.0"

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/func-petshop-pets-java/target/*.jar app.jar

# Environment variables
ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=7084
ENV JAVA_OPTS="-Xms128m -Xmx256m"

# Database
ENV SPRING_DATASOURCE_URL=jdbc:h2:mem:petshop;DB_CLOSE_DELAY=-1
ENV SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.h2.Driver

EXPOSE 7084

HEALTHCHECK --interval=30s --timeout=3s --start-period=45s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:7084/api/pets/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=$SERVER_PORT -jar app.jar"]
