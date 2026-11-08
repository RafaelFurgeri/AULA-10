# Estágio 1: Build com Maven e Eclipse Temurin JDK 17
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
COPY config ./config
COPY src ./src

RUN mvn clean package -DskipTests -B

# Estágio 2: Runtime leve e seguro com Eclipse Temurin JRE 17 Alpine
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S iqsgroup && adduser -S iqsuser -G iqsgroup
COPY --from=build /app/target/aula10-ci-cd-1.0.0.jar app.jar

USER iqsuser
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD wget -qO- http://localhost:8080/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
