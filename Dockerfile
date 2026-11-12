# ==============================================================================
# Dockerfile Multi-Stage para CRM Helpdesk (ServiClient)
# Java 21 + Spring Boot 3.3.3
# ==============================================================================

# ------------------------------------------------------------------------------
# Fase 1: Compilación y empaquetado (Builder)
# ------------------------------------------------------------------------------
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Copiar descriptor Maven y resolver dependencias para optimizar caché de capas
COPY pom.xml .
RUN mvn dependency:resolve -B

# Copiar código fuente y generar JAR ejecutable
COPY src ./src
RUN mvn clean package -DskipTests -B

# ------------------------------------------------------------------------------
# Fase 2: Entorno de ejecución ligero y seguro (Runtime)
# ------------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine AS runtime

LABEL maintainer="ServiClient CRM Helpdesk Team"
LABEL description="ServiClient CRM & Helpdesk Monolithic Web Application"

# Instalar dependencias para generación de reportes Excel con Apache POI (fuentes),
# curl para diagnósticos y tzdata para zona horaria
RUN apk add --no-cache fontconfig freetype ttf-dejavu curl tzdata

# Crear usuario y grupo de sistema sin privilegios de root por seguridad
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

RUN mkdir -p /app/logs && \
  chown -R appuser:appgroup /app/logs

# Copiar el archivo JAR empaquetado desde la fase de construcción
COPY --from=builder --chown=appuser:appgroup /build/target/*.jar /app/app.jar

# Cambiar al usuario no privilegiado
USER appuser:appgroup

# Configuración de JVM para contenedores
ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"
ENV SERVER_PORT=8080

EXPOSE 8080

# Healthcheck interno del contenedor de la aplicación
HEALTHCHECK --interval=30s --timeout=5s --start-period=45s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

