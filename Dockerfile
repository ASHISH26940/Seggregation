# Stage 1: Build the application
FROM maven:3.9.0-eclipse-temurin-17 AS build

# Set the working directory
WORKDIR /app

# Copy the pom.xml to cache dependencies
COPY pom.xml .

# Download dependencies without building
RUN mvn dependency:go-offline

# Copy the entire source code
COPY src ./src

# Package the application with dependencies (shaded JAR)
RUN mvn clean package -DskipTests

# Stage 2: Create a minimal runtime image
FROM eclipse-temurin:17-jdk

# Set the working directory
WORKDIR /app

# Set Java options for increased heap size (adjust as needed)
ENV JAVA_OPTS="-Xms512m -Xmx4g"

# Copy the fat JAR from the builder stage
COPY --from=build /app/target/myproject-1.0-SNAPSHOT.jar app.jar

# Set the input and output as runtime arguments
ENTRYPOINT ["java", "-jar", "app.jar"]
