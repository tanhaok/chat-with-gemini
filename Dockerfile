FROM maven:3.8.8 AS builder
WORKDIR /workdir/server
COPY pom.xml /workdir/server/pom.xml
RUN mvn dependency:go-offline

COPY src /workdir/server/src
RUN mvn install -DskipTests



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
ARG USER_ID
ARG SECOND_USER_ID
ARG TELEGRAM_API_KEY

ENV API_KEY=${API_KEY}
ENV TELEGRAM_API_KEY=${TELEGRAM_API_KEY}
ENV USER_ID=${USER_ID}
ENV SECOND_USER_ID=${SECOND_USER_ID}

# Run the application using a specific entry point
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
