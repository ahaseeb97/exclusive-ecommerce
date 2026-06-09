# Dockerfile for Helper Module
# This builds the Helper module and installs it to Maven repository
# Note: In docker-compose.yml, helper-builder service uses Maven image directly
# This Dockerfile is provided for standalone builds if needed

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml first for dependency caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build and install to local Maven repository
RUN mvn clean install -DskipTests

# The artifact is installed to /root/.m2/repository
# This will be used by microproducts service via shared volume

