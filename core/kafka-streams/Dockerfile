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
RUN mvn clean package -pl core/kafka-streams -am -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre

LABEL maintainer="Big Data Learning Platform"
LABEL description="Kafka Streams processor for real-time transaction processing"

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy the built JAR
COPY --from=build /app/core/kafka-streams/target/*.jar ./app.jar

# Create state directory for Kafka Streams
RUN mkdir -p /tmp/kafka-streams

# Set environment variables
ENV JAVA_OPTS="-Xmx1g -Xms512m -XX:+UseG1GC"
ENV KAFKA_APPLICATION_ID=bigdata-streams-processor
ENV KAFKA_STATE_DIR=/tmp/kafka-streams

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Default command
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"] 