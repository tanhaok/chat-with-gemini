FROM maven:3.8.5 AS builder
WORKDIR /workdir/server
COPY pom.xml /workdir/server/pom.xml
RUN mvn dependency:go-offline

COPY src /workdir/server/src
RUN mvn install


# Use a slim base image with Java 21
FROM eclipse-temurin:21-jdk-alpine

# Create a non-root user for security
RUN adduser -D appuser

# Set working directory
WORKDIR /app

# Copy the application JAR file
COPY --from=builder /workdir/server/target/*.jar /app/app.jar

# Change ownership of the JAR file to the non-root user
RUN chown appuser:appuser /app/app.jar

# Switch to the non-root user
USER appuser

# Expose port 8080 (typical for Spring Boot apps)
EXPOSE 8081
ARG API_KEY
ENV API_KEY=${API_KEY}

# Run the application using a specific entry point
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
