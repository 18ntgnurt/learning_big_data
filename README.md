# 🎓 Big Data Platform - Learning Data Engineering

A comprehensive, production-ready big data processing platform demonstrating modern data engineering and ML practices through a complete fraud detection system.

## 📋 Documentation

- **[🏗️ REFACTORED_ARCHITECTURE.md](REFACTORED_ARCHITECTURE.md)** - Complete refactored system architecture
- **[📊 Infrastructure Monitoring](infrastructure/monitoring/README.md)** - Unified monitoring stack documentation

## 🎯 What You'll Learn

This project demonstrates enterprise-grade patterns and practices:

- **Modern ETL Pipelines**: Java-based data ingestion with Kafka Streams
- **Real-time ML Services**: Python fraud detection with MLflow integration
- **Event-Driven Architecture**: Kafka as the central message backbone
- **Feature Engineering**: Real-time feature stores with Redis and PostgreSQL
- **ML Operations**: Complete MLOps pipeline with model registry and monitoring
- **Comprehensive Monitoring**: Prometheus, Grafana, and custom metrics
- **Container Orchestration**: Docker Compose with proper networking and dependencies
- **Data Quality**: Validation, monitoring, and drift detection
- **Production Patterns**: Health checks, error handling, and observability

## 🏗️ Refactored Project Structure

```
learning_big_data/
├── core/                          # Java ETL engine and streaming
│   ├── etl-engine/               # Data ingestion and processing
│   └── kafka-streams/            # Real-time stream processing
├── ml-services/                  # Python ML platform
│   └── fraud-detection/          # Complete fraud detection service
├── infrastructure/               # Infrastructure as code
│   └── monitoring/              # Unified monitoring stack
├── shared/                       # Shared configurations and schemas
│   ├── kafka/                   # Kafka topic and schema definitions
│   └── schemas/                 # JSON schemas for data contracts
├── deployment/                   # Docker orchestration
│   └── docker-compose/          # Environment-specific compose files
├── notebooks/                    # Jupyter notebooks for exploration
├── data/                         # Data storage and processing
├── init-scripts/                 # Database initialization
└── [support files]              # Build configs, validation scripts
```

## 🚀 Quick Start

### Prerequisites

- **Java 11+** - For ETL engine and stream processing
- **Python 3.9+** - For ML services
- **Docker & Docker Compose** - For infrastructure
- **Maven 3.6+** - For Java builds
- **8GB+ RAM** - For running all services

### 1. Start the Complete Platform

```bash
# Navigate to project directory
cd learning_big_data

# Start infrastructure services (databases, Kafka, etc.)
cd deployment/docker-compose
docker-compose -f docker-compose.refactored.yml up -d

# Start monitoring stack (optional but recommended)
cd ../../infrastructure/monitoring
docker-compose -f docker-compose.monitoring.yml up -d

# Initialize databases
cd ../../
./run-all-sql.sh
```

### 2. Verify Platform Health

```bash
# Check all services are running
docker ps

# Validate architecture
./validate-architecture.sh

# Test database connections
./test-database-connections.sh
```

### 3. Access the Platform

| Service | URL | Purpose |
|---------|-----|---------|
| **Fraud Detection API** | http://localhost:5001 | ML fraud detection service |
| **MLflow UI** | http://localhost:5000 | ML experiment tracking |
| **Kafka UI** | http://localhost:9090 | Kafka cluster management |
| **Grafana Dashboards** | http://localhost:3000 | Monitoring and analytics |
| **Prometheus Metrics** | http://localhost:9090 | Metrics collection |

**Default Credentials:**
- Grafana: `admin/admin123`

## 🎮 Using the Platform

### Real-time Fraud Detection

```bash
# Send a test transaction for fraud detection
curl -X POST http://localhost:5001/api/v1/predict \
  -H "Content-Type: application/json" \
  -d '{
    "transaction_id": "test-123",
    "customer_id": "customer-456",
    "amount": 1500.00,
    "timestamp": "2024-01-15T10:30:00Z",
    "merchant_id": "merchant-789"
  }'
```

### Batch Processing

```bash
# Process multiple transactions
curl -X POST http://localhost:5001/api/v1/predict/batch \
  -H "Content-Type: application/json" \
  -d '[
    {"transaction_id": "txn-1", "customer_id": "cust-1", "amount": 100.00, "timestamp": "2024-01-15T10:30:00Z"},
    {"transaction_id": "txn-2", "customer_id": "cust-2", "amount": 5000.00, "timestamp": "2024-01-15T10:31:00Z"}
  ]'
```

### Health Monitoring

```bash
# Check service health
curl http://localhost:5001/health

# Get metrics
curl http://localhost:5001/metrics

# Check model information
curl http://localhost:5001/api/v1/model
```

## 📊 Architecture Components

### Core ETL Layer (Java)

- **ETL Engine** (`core/etl-engine/`)
  - Unified data ingestion (CSV, JSON, databases)
  - Data validation and transformation
  - Kafka streaming integration
  - Batch processing with error handling

- **Kafka Streams** (`core/kafka-streams/`)
  - Real-time transaction processing
  - Stream analytics and windowing
  - High-value transaction detection
  - Fraud preprocessing pipeline

### ML Services Layer (Python)

- **Fraud Detection Service** (`ml-services/fraud-detection/`)
  - Real-time fraud prediction API
  - Feature engineering and feature store
  - MLflow model registry integration
  - Ensemble models (RandomForest + IsolationForest)
  - Background Kafka processing

### Infrastructure Layer

- **Monitoring Stack** (`infrastructure/monitoring/`)
  - Prometheus metrics collection
  - Grafana visualization dashboards
  - AlertManager for notifications
  - Custom exporters for ML metrics
  - Log aggregation with Loki

- **Data Storage**
  - PostgreSQL for data warehouse and feature store
  - MySQL for transactional data
  - Redis for real-time feature caching
  - MLflow artifacts storage

### Event Backbone

- **Kafka Topics** (standardized naming)
  - `transactions-raw-v1` - Raw transaction data
  - `transactions-validated-v1` - Validated transactions
  - `transactions-enriched-v1` - Feature-enriched data
  - `fraud-predictions-v1` - ML predictions
  - `fraud-alerts-v1` - High-risk alerts

## 🔧 Development Workflow

### Building and Testing

```bash
# Build Java components
mvn clean compile

# Run ETL engine
mvn exec:java -Dexec.mainClass="com.dataengineering.etl.DataIngestionApplication"

# Run Kafka streams processor
mvn exec:java -Dexec.mainClass="com.dataengineering.streaming.TransactionStreamProcessor"
```

### Monitoring and Debugging

```bash
# View service logs
docker-compose -f deployment/docker-compose/docker-compose.refactored.yml logs -f fraud-api

# Monitor Kafka topics
docker exec -it bigdata-kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic fraud-predictions-v1

# Check Prometheus targets
curl http://localhost:9090/api/v1/targets
```

## 📈 Key Features Demonstrated

### 1. **Event-Driven Architecture**
- Kafka as central message bus
- Event sourcing patterns
- Loose coupling between services
- Scalable message processing

### 2. **Real-time ML Operations**
- Online feature stores
- Real-time model inference
- Model performance monitoring
- Drift detection and alerting

### 3. **Data Quality & Validation**
- Schema validation with JSON Schema
- Data quality metrics
- Anomaly detection
- Error handling and dead letter queues

### 4. **Observability**
- Distributed tracing concepts
- Custom business metrics
- Performance monitoring
- Health checks and alerting

### 5. **Scalability Patterns**
- Microservices architecture
- Container orchestration
- Independent service scaling
- Load balancing strategies

## 🛠️ Technology Stack

### Core Technologies
- **Java 11+** with Spring Boot - ETL engine and stream processing
- **Python 3.9+** with Flask - ML services and APIs
- **Apache Kafka** - Event streaming platform
- **MLflow** - ML lifecycle management
- **Redis** - Real-time feature caching
- **PostgreSQL/MySQL** - Data storage

### Monitoring & Operations
- **Prometheus** - Metrics collection
- **Grafana** - Visualization and dashboards
- **Loki** - Log aggregation
- **AlertManager** - Alert routing and notifications

### Development & Deployment
- **Docker & Docker Compose** - Containerization
- **Maven** - Java build management
- **pip** - Python dependency management

## 🎯 Learning Path

### Beginner (Getting Started)
1. Start the platform with Docker Compose
2. Explore the fraud detection API
3. Send test transactions and observe results
4. Check Grafana dashboards for metrics

### Intermediate (Understanding Components)
1. Examine the ETL engine source code
2. Study the Kafka streams processing
3. Explore the ML service implementation
4. Understand the feature store design

### Advanced (Customization & Extension)
1. Add new ML models to the model registry
2. Create custom Kafka streams processors
3. Build additional monitoring dashboards
4. Implement new fraud detection features

## 📚 Additional Resources

- **API Documentation**: Available at http://localhost:5001/docs (when running)
- **Kafka Topics**: Monitor at http://localhost:9090 (Kafka UI)
- **ML Experiments**: Track at http://localhost:5000 (MLflow)
- **System Metrics**: View at http://localhost:3000 (Grafana)

## 🤝 Contributing

This is a learning project. Feel free to:
- Experiment with the code
- Add new features
- Improve documentation
- Share your learning experience

## 📄 License

This project is for educational purposes. Use the patterns and practices in your own projects! 