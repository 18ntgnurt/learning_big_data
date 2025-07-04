FROM eclipse-temurin:17-jdk AS build

# Install Maven
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy the entire project structure for proper Maven build
COPY pom.xml ./pom.xml
COPY shared/ ./shared/
COPY core/ ./core/

# Build the application using the main POM
RUN mvn clean package -pl core/etl-engine -am -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre

LABEL maintainer="Big Data Learning Platform"
LABEL description="ETL Engine for data ingestion and processing"

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy the built JAR
COPY --from=build /app/core/etl-engine/target/*.jar ./app.jar

# Create necessary directories
RUN mkdir -p /app/data /app/logs

# Set environment variables
ENV JAVA_OPTS="-Xmx2g -Xms1g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
ENV SPRING_PROFILES_ACTIVE=docker

# Expose ports
EXPOSE 8080 8081

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Default command
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"] 