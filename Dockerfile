# 🏗 Stage 1: Build Application using Maven
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy the source code and pom.xml
COPY . .

# Build the application with dependencies (optimized with cache)
RUN --mount=type=cache,target=/root/.m2 mvn clean package -DskipTests

# 🌟 Stage 2: Minimal Runtime with Eclipse Temurin JDK 17
FROM eclipse-temurin:17-jdk

WORKDIR /app

# Security best practice: Create a non-root user
RUN useradd -m appuser
USER appuser

# Copy the built JAR from the builder stage
COPY --from=builder /app/target/myproject-1.0-SNAPSHOT.jar app.jar

# Set entrypoint for better container execution
ENTRYPOINT ["java", "-jar", "app.jar"]
