# Dockerfile for All Java Functions Microservices
# Multi-stage build - builds all JARs in one image

FROM eclipse-temurin:21-jdk-alpine AS build

RUN apk add --no-cache maven

WORKDIR /app

# Copy entire project
COPY pom.xml .
COPY petshop-functions-shared ./petshop-functions-shared
COPY func-petshop-auth-java ./func-petshop-auth-java
COPY func-petshop-catalog-java ./func-petshop-catalog-java
COPY func-petshop-customers-java ./func-petshop-customers-java
COPY func-petshop-pets-java ./func-petshop-pets-java
COPY func-petshop-scheduling-java ./func-petshop-scheduling-java
COPY func-petshop-orders-java ./func-petshop-orders-java

# Build all modules
RUN mvn clean package -DskipTests

# ============================================================
# Auth Service Runtime Image
# ============================================================
FROM eclipse-temurin:21-jre-alpine AS auth

LABEL description="Pet Shop Auth Function - Spring Boot Microservice"

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app
COPY --from=build /app/func-petshop-auth-java/target/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=7081
ENV JAVA_OPTS="-Xms128m -Xmx256m"

EXPOSE 7081

HEALTHCHECK --interval=30s --timeout=3s --start-period=45s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:7081/api/auth/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=$SERVER_PORT -jar app.jar"]

# ============================================================
# Catalog Service Runtime Image
# ============================================================
FROM eclipse-temurin:21-jre-alpine AS catalog

LABEL description="Pet Shop Catalog Function - Spring Boot Microservice"

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app
COPY --from=build /app/func-petshop-catalog-java/target/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=7082
ENV JAVA_OPTS="-Xms128m -Xmx256m"

EXPOSE 7082

HEALTHCHECK --interval=30s --timeout=3s --start-period=45s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:7082/api/categories/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=$SERVER_PORT -jar app.jar"]

# ============================================================
# Customers Service Runtime Image
# ============================================================
FROM eclipse-temurin:21-jre-alpine AS customers

LABEL description="Pet Shop Customers Function - Spring Boot Microservice"

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app
COPY --from=build /app/func-petshop-customers-java/target/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=7083
ENV JAVA_OPTS="-Xms128m -Xmx256m"

EXPOSE 7083

HEALTHCHECK --interval=30s --timeout=3s --start-period=45s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:7083/api/customers/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=$SERVER_PORT -jar app.jar"]

# ============================================================
# Pets Service Runtime Image
# ============================================================
FROM eclipse-temurin:21-jre-alpine AS pets

LABEL description="Pet Shop Pets Function - Spring Boot Microservice"

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app
COPY --from=build /app/func-petshop-pets-java/target/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=7084
ENV JAVA_OPTS="-Xms128m -Xmx256m"

EXPOSE 7084

HEALTHCHECK --interval=30s --timeout=3s --start-period=45s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:7084/api/pets/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=$SERVER_PORT -jar app.jar"]

# ============================================================
# Scheduling Service Runtime Image
# ============================================================
FROM eclipse-temurin:21-jre-alpine AS scheduling

LABEL description="Pet Shop Scheduling Function - Spring Boot Microservice"

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app
COPY --from=build /app/func-petshop-scheduling-java/target/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=7085
ENV JAVA_OPTS="-Xms128m -Xmx256m"

EXPOSE 7085

HEALTHCHECK --interval=30s --timeout=3s --start-period=45s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:7085/api/scheduling/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=$SERVER_PORT -jar app.jar"]

# ============================================================
# Orders Service Runtime Image
# ============================================================
FROM eclipse-temurin:21-jre-alpine AS orders

LABEL description="Pet Shop Orders Function - Spring Boot Microservice"

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app
COPY --from=build /app/func-petshop-orders-java/target/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=7086
ENV JAVA_OPTS="-Xms128m -Xmx256m"

EXPOSE 7086

HEALTHCHECK --interval=30s --timeout=3s --start-period=45s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:7086/api/orders/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=$SERVER_PORT -jar app.jar"]
