# -------- Stage 1: Build the Spring Boot JAR with Java 21 --------
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Set the working directory
WORKDIR /app

# Copy pom.xml and download dependencies (faster builds using cache)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the source code
COPY src ./src

# ✅ ADD THIS LINE to copy your properties file
COPY env.properties .

# Build the application (skip tests for speed)
RUN mvn clean package -DskipTests


# -------- Stage 2: Run the application with Java 21 --------
FROM openjdk:21-slim

# Set the working directory
WORKDIR /app

# Copy the built JAR file from the build stage
COPY --from=build /app/target/*.jar app.jar
# ✅ ADD THIS LINE to copy the properties file from the build stage
COPY --from=build /app/env.properties ./

# Expose port 8080
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]