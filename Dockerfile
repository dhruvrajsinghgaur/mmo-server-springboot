# Stage 1: Build the jar file inside Docker safely
FROM eclipse-temurin:25-jdk-alpine AS builder
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Stage 2: Run the jar using a lightweight Java environment
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/mmo-server-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

