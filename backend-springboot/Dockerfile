# Multi-stage build para otimizar tamanho da imagem

# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS build

# Instalar Maven
RUN apk add --no-cache maven

WORKDIR /app

# Copiar arquivos do projeto
COPY pom.xml .
COPY src ./src

# Build da aplicação (pula testes para build mais rápido)
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

# Metadata
LABEL maintainer="petshop@example.com"
LABEL description="Pet Shop Backend - Spring Boot"
LABEL version="1.0.0"

# Criar usuário não-root por segurança
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app

# Copiar apenas o JAR compilado do stage anterior
COPY --from=build /app/target/*.jar app.jar

# Variáveis de ambiente com valores padrão
ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=8080
ENV JAVA_OPTS="-Xms256m -Xmx512m"

# Expor porta
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Comando de execução
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
