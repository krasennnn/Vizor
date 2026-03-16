# Multi-stage Dockerfile for Spring Boot application
# Stage 1: Build the application
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Install necessary tools
RUN apk add --no-cache bash

# Copy Gradle wrapper files first (for better caching)
COPY gradle ./gradle
COPY gradlew ./
COPY gradlew.bat ./

# Make gradlew executable
RUN chmod +x ./gradlew

# Copy Gradle configuration files (dependencies are downloaded here for caching)
COPY build.gradle settings.gradle ./

# Download dependencies (this layer will be cached if build.gradle doesn't change)
RUN ./gradlew dependencies --no-daemon || true

# Copy source code (this changes most frequently, so it's last)
COPY src ./src

# Build the application (skip tests as they're already run in CI)
RUN ./gradlew clean bootJar -x test --no-daemon

# Stage 2: Runtime image
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create a non-root user
RUN addgroup -S spring && adduser -S spring -G spring

# Copy the JAR from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Change ownership
RUN chown spring:spring app.jar

# Switch to non-root user
USER spring:spring

# Expose the application port
EXPOSE 8080

# Health check (optional - remove if actuator is not configured)
# HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
#   CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run the application
# Clear any inherited ENTRYPOINT first
ENTRYPOINT []
CMD ["java", "-jar", "app.jar"]