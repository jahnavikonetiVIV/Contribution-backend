# -------- Stage 1: Build the Spring Boot application --------

FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# Copy Gradle wrapper and config files

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# Copy source code

COPY src src

# Give execution permission to gradlew

RUN chmod +x gradlew

# Build the Spring Boot JAR (skip tests)

RUN ./gradlew bootJar -x test

# -------- Stage 2: Create lightweight runtime image --------

FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy the built JAR from builder stage

COPY --from=builder /app/build/libs/*.jar app.jar

# Render provides PORT environment variable

ENV PORT=8080

EXPOSE 8080

# Run the Spring Boot application

ENTRYPOINT ["java","-jar","app.jar"]
