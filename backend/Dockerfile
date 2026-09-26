# =====================================================
# Stage 1: Build
# =====================================================
FROM maven:3.9-eclipse-temurin-17-alpine AS build

WORKDIR /app

# Copia pom primeiro pra cachear deps
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia source e builda
COPY src ./src
RUN mvn clean package -DskipTests -B

# =====================================================
# Stage 2: Runtime
# =====================================================
FROM eclipse-temurin:17-jre-alpine AS runtime

# Cria usuário não-root
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app

# Copia só o jar do stage de build
COPY --from=build /app/target/*.jar app.jar

# Health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8081/health || exit 1

EXPOSE 8081

# JVM otimizada pra containers
ENTRYPOINT ["java", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+UseContainerSupport", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]