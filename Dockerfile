# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
RUN apk add --no-cache libc6-compat gcompat
WORKDIR /app

# Copy the pom.xml and any other configuration files
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Download dependencies (this layer will be cached)
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

# Copy the source code and proto files
COPY src src

# Build the application
RUN ./mvnw clean package -Dmaven.test.skip=true

# Stage 2: Run the application
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the ports
EXPOSE 8080 9090

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]