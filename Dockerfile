# Etapa 1: build
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B package -DskipTests

# Etapa 2: runtime
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --uid 1001 goerp
COPY --from=build /app/target/goerp-*.jar app.jar
USER goerp
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
