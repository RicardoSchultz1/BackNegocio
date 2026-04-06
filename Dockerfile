# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:resolve

# Copy source code
COPY src ./src
COPY mvnw mvnw.cmd ./

# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy JAR from builder stage
COPY --from=builder /build/target/*.jar app.jar

# Expose port (Render dynamic port)
EXPOSE 8081

# Set environment variables (can be overridden in Render)
ENV SERVER_PORT=8081

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
