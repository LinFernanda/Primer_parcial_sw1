# ==============================================================================
# FASE 13: DOCKERFILE MULTI-STAGE - BACKEND SPRING BOOT 3 (JAVA 21)
# Optimizado para Amazon Web Services (AWS ECS / Fargate / ECR / EC2)
# ==============================================================================

# STAGE 1: Compilación y Empaquetado con Maven y Eclipse Temurin 21
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# 1. Copiar descriptor Maven para cachear dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 2. Copiar código fuente y compilar artefacto JAR
COPY src ./src
RUN mvn clean package -DskipTests -B

# ==============================================================================
# STAGE 2: Imagen de Producción Ligera y Segura (Alpine JRE 21)
FROM eclipse-temurin:21-jre-alpine AS runner

LABEL maintainer="Equipo CASE Platform <dev@caseplatform.com>"
LABEL version="1.0.0"
LABEL description="Backend Spring Boot 3 con Java 21 para Plataforma CASE en AWS"

WORKDIR /app

# Instalar curl para healthchecks de AWS ALB / Docker
RUN apk add --no-cache curl tzdata

# Configurar zona horaria estándar UTC
ENV TZ=UTC

# Crear usuario y grupo sin privilegios de root (Hardening de Seguridad)
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Crear directorios de datos y logs con permisos adecuados
RUN mkdir -p /app/data/storage /app/logs && chown -R appuser:appgroup /app

# Copiar el ejecutable JAR desde la etapa de construcción
COPY --from=builder --chown=appuser:appgroup /build/target/case-platform-backend-*.jar /app/app.jar

# Cambiar al usuario no-root
USER appuser

# Puerto expuesto para el contenedor
EXPOSE 8080

# Verificación de estado para AWS ALB / ECS
HEALTHCHECK --interval=30s --timeout=5s --start-period=45s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Opciones de la JVM adaptadas para límites de memoria en contenedores
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError -Djava.security.egd=file:/dev/./urandom"
ENV SPRING_PROFILES_ACTIVE="prod"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
