# Dockerfile for Products Service (microproducts)
# Builds Helper module first, then builds microproducts
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Step 1: Build Helper module first
COPY helper/pom.xml ./helper/
COPY helper/src ./helper/src
WORKDIR /app/helper
RUN mvn clean install -DskipTests

# Step 2: Build microproducts (Helper is now in local Maven repo)
WORKDIR /app
COPY microproducts/pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY microproducts/src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the built JAR
COPY --from=build /app/target/*.jar app.jar

# Expose Products service port
EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]

