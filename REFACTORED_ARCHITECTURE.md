# 🏗️ Refactored Big Data Architecture

## 📋 Executive Summary

This refactored architecture provides a comprehensive, production-ready big data processing platform with a complete fraud detection system. The architecture demonstrates modern data engineering and ML practices through three core pillars:

1. **Java ETL & Streaming Pipeline** - Real-time data ingestion and stream processing with Kafka
2. **Python ML Services** - Complete fraud detection with integrated feature stores and model registry
3. **Unified Infrastructure** - Comprehensive monitoring, observability, and orchestration

## 🎯 Architecture Principles

- **Modularity**: Clear separation between ETL, ML, and infrastructure components
- **Scalability**: Containerized microservices that scale independently  
- **Observability**: Comprehensive monitoring, logging, and alerting with custom metrics
- **Event-Driven**: Kafka-based event streaming with standardized schemas
- **ML Operations**: Complete MLOps pipeline with feature stores, model registry, and monitoring
- **Production-Ready**: Health checks, error handling, graceful degradation, and fault tolerance

## 🏗️ System Architecture

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                         REFACTORED FRAUD DETECTION PLATFORM                     │
└─────────────────────────────────────────────────────────────────────────────────┘

┌──────────────────┐    ┌──────────────────┐    ┌──────────────────────────────────┐
│   DATA SOURCES   │────▶│  JAVA ETL LAYER  │────▶│        KAFKA BACKBONE           │
│                  │    │                  │    │                                  │
│ • Transaction    │    │ • DataIngestion  │    │ • 18 Standardized Topics        │
│   Files (CSV)    │    │   Service        │    │ • transactions-raw-v1           │
│ • JSON Data      │    │ • Validation &   │    │ • transactions-validated-v1     │
│ • Databases      │    │   Transformation │    │ • fraud-predictions-v1          │
│ • APIs           │    │ • Kafka Streams  │    │ • fraud-alerts-v1               │
└──────────────────┘    │   Processor      │    │ • analytics-aggregated-v1       │
                        └──────────────────┘    └──────────────────────────────────┘
                                                           │
           ┌────────────────────────────────────────────────┼────────────────────────┐
           │                                               │                        │
           ▼                                               ▼                        ▼
┌──────────────────────┐                       ┌──────────────────────┐  ┌─────────────────┐
│   PYTHON ML          │                       │   DATA STORAGE       │  │   MONITORING    │
│   SERVICES           │                       │                      │  │   STACK         │
│                      │                       │ • PostgreSQL         │  │                 │
│ • Fraud API (Flask)  │                       │   (Feature Store)    │  │ • Prometheus    │
│ • Integrated Feature │                       │ • MySQL              │  │ • Grafana       │
│   Store (Redis +     │                       │   (Transactions)     │  │ • AlertManager  │
│   SQLite)            │                       │ • Redis              │  │ • Loki/Promtail │
│ • MLflow Model       │                       │   (Real-time Cache)  │  │ • 18 Exporters  │
│   Registry           │                       │ • MLflow Artifacts   │  │ • Custom Metrics│
│ • Ensemble Models    │                       │ • SQLite (Features)  │  │ • Team Alerts   │
│   (RF + IsolationF)  │                       │                      │  │                 │
└──────────────────────┘                       └──────────────────────┘  └─────────────────┘
```

## 📂 Actual Implementation Structure

```
learning_big_data/
├── core/                           # Java ETL engine and streaming
│   ├── etl-engine/                # Main data ingestion and processing
│   │   └── src/main/java/com/dataengineering/etl/
│   │       └── DataIngestionService.java
│   └── kafka-streams/             # Real-time stream processing  
│       └── src/main/java/com/dataengineering/streaming/
│           └── TransactionStreamProcessor.java
├── ml-services/                   # Python ML platform  
│   └── fraud-detection/           # Complete integrated fraud detection service
│       ├── src/
│       │   ├── fraud_detection_service.py    # Core service with feature store, model registry
│       │   └── api.py             # Flask API with Prometheus metrics
│       └── Dockerfile             # Container definition
├── infrastructure/               # Infrastructure and monitoring
│   └── monitoring/              # Unified monitoring stack
│       ├── docker-compose.monitoring.yml
│       ├── prometheus/
│       ├── grafana/
│       ├── alertmanager/
│       ├── loki/
│       └── promtail/
├── shared/                       # Shared schemas and configurations
│   ├── kafka/                   # Kafka topic and schema definitions
│   │   └── KafkaTopologyConfig.java
│   └── schemas/                 # JSON schemas for data contracts
│       └── transaction_schema.json
├── deployment/                   # Docker orchestration
│   └── docker-compose/          # Environment-specific compose files
│       └── docker-compose.refactored.yml
├── notebooks/                    # Jupyter exploration notebooks
│   └── fraud-detection/
├── data/                         # Data storage and processing
├── init-scripts/                 # Database initialization SQL
├── mlruns/                       # MLflow experiment tracking
├── mlflow-artifacts/             # MLflow model artifacts
├── target/                       # Java build artifacts
├── pom.xml                       # Maven build configuration
├── requirements.txt              # Python dependencies
├── validate-architecture.sh      # Architecture validation
├── test-database-connections.sh  # Database connectivity tests
├── run-all-sql.sh                # Database initialization
└── README.md                     # Platform documentation
```

## 🔧 Core Components Implementation

### 1. Java ETL Layer (`core/`)

#### ETL Engine (`core/etl-engine/`)
**DataIngestionService.java**
```java
com.dataengineering.etl.DataIngestionService
```
- **Unified Ingestion**: CSV and JSON processing with automatic format detection
- **Kafka Integration**: Batch streaming (1000 records) to `transactions-raw-v1`
- **Error Handling**: Dead letter queue support and retry mechanisms
- **Metrics Collection**: Real-time processing statistics with atomic counters
- **Asynchronous Processing**: CompletableFuture-based parallel processing

#### Kafka Streams (`core/kafka-streams/`)
**TransactionStreamProcessor.java**
```java
com.dataengineering.streaming.TransactionStreamProcessor
```
- **Stream Topology**: Multi-branch processing with validation, enrichment, and analytics
- **Real-time Detection**: High-value ($1000+) and suspicious ($5000+) transaction identification
- **Windowed Analytics**: 5-minute tumbling windows for aggregation
- **Topic Routing**: Intelligent routing to fraud detection and analytics pipelines
- **Performance Monitoring**: Built-in health checks and processing metrics

### 2. Python ML Services (`ml-services/fraud-detection/`)

#### Integrated Fraud Detection Service
**fraud_detection_service.py** - Unified ML orchestration service containing:

**Feature Store Implementation:**
- **FeatureStore class**: Hybrid Redis + SQLite storage for features
- **Real-time Features**: Redis caching with sub-millisecond retrieval
- **Persistent Features**: SQLite for historical feature storage
- **Feature Engineering**: 15+ engineered features including:
  - Transaction amount analysis and z-scores
  - Time-based patterns (night transactions, weekend patterns)
  - Customer behavior analysis (amount vs. customer average)
  - Merchant risk scoring
  - Geographic and device-based features

**Model Registry Implementation:**
- **ModelRegistry class**: MLflow integration with fallback ensemble
- **Primary Models**: MLflow-tracked production models
- **Fallback Ensemble**: RandomForestClassifier + IsolationForest
- **Model Versioning**: Automatic version tracking and deployment
- **Performance Monitoring**: Model accuracy and drift detection

**Data Quality Implementation:**
- **DataQualityChecker class**: Real-time data validation
- **Schema Validation**: JSON schema enforcement
- **Quality Metrics**: Completeness, accuracy, consistency tracking
- **Anomaly Detection**: Statistical and ML-based anomaly identification

**api.py** - Production Flask API with:
- **Prediction Endpoints**:
  - `POST /api/v1/predict` - Single transaction prediction
  - `POST /api/v1/predict/batch` - Batch processing (max 100 transactions)
- **Health & Monitoring**:
  - `GET /health` - Service health status with dependency checks
  - `GET /metrics` - Prometheus metrics endpoint
  - `GET /api/v1/metrics` - Detailed service metrics
  - `GET /api/v1/model` - Current model information
- **Production Features**:
  - Comprehensive error handling and validation
  - CORS support for web applications
  - Prometheus metrics collection
  - Background Kafka processing
  - Request/response logging and tracing

### 3. Infrastructure Layer (`infrastructure/monitoring/`)

#### Unified Monitoring Stack
**docker-compose.monitoring.yml** includes:
- **Core Services**: Prometheus (30-day retention), Grafana (admin/admin123), AlertManager
- **System Exporters**: Node, cAdvisor, Redis, Kafka, PostgreSQL, MySQL, Blackbox
- **Custom Exporters**: MLflow (9401), Fraud Detection (9402), Data Quality (9403), Kafka Lag (8000)
- **Log Management**: Loki (7-day retention) + Promtail with regex parsing

**Configuration Files:**
- **alertmanager.yml**: Multi-channel alerts (Email/Slack/PagerDuty) with severity routing
- **prometheus.yml**: 18 scrape targets with service discovery
- **datasources.yml**: 9 Grafana datasources (Prometheus, Loki, Redis, databases)
- **loki-config.yml**: Log retention, compaction, and performance tuning
- **promtail-config.yml**: Container log collection with service-specific parsing

### 4. Shared Resources (`shared/`)

#### Kafka Configuration (`shared/kafka/`)
**KafkaTopologyConfig.java**
- **18 Standardized Topics** across 4 categories:
  - **Data Ingestion**: transactions-raw-v1, transactions-validated-v1, transactions-enriched-v1
  - **Fraud Detection**: fraud-predictions-v1, fraud-alerts-v1, fraud-feedback-v1
  - **Analytics**: analytics-aggregated-v1, analytics-windowed-v1, analytics-realtime-v1
  - **Monitoring**: monitoring-metrics-v1, monitoring-logs-v1, monitoring-alerts-v1
- **Custom Configurations**: Partitions (3-12), replication (2), retention policies
- **Compression**: LZ4 compression with optimized segment sizes

#### Schema Definitions (`shared/schemas/`)
**transaction_schema.json**
- **JSON Schema Draft 2020-12** compliant
- **Required Fields**: transaction_id, customer_id, amount, timestamp
- **Optional Fields**: merchant details, location (GPS), payment method, device info
- **Risk Indicators**: Suspicious patterns and fraud scores
- **Processing Metadata**: Timestamps, service versions, processing flags

### 5. Deployment Orchestration (`deployment/docker-compose/`)

#### Main Docker Compose (`docker-compose.refactored.yml`)
**Infrastructure Layer:**
- Zookeeper (Kafka coordination)
- Kafka (JMX enabled for monitoring)
- Schema Registry (Confluent)
- Kafka UI (cluster management)
- Redis (feature caching)
- PostgreSQL (feature store, MLflow backend)
- MySQL (transaction data)

**ETL Layer:**
- ETL Engine (Spring Boot with metrics)
- Kafka Streams Processor (transaction processing)

**ML Layer:**
- MLflow Tracking Server (experiment tracking)
- Fraud Detection API (integrated ML service)

**Configuration:**
- Single `bigdata-network` with proper DNS resolution
- Comprehensive health checks for all services
- Persistent volumes for stateful services
- Environment-specific configurations

## 🔄 Data Flow Implementation

### 1. Ingestion Pipeline
```
CSV/JSON Files → DataIngestionService → Validation → transactions-raw-v1 → Stream Processing
```

### 2. Stream Processing Pipeline  
```
transactions-raw-v1 → TransactionStreamProcessor → Enrichment → Multiple Topic Routing:
├── transactions-validated-v1 (validated data)
├── transactions-enriched-v1 (feature-enriched)
├── fraud-candidates-v1 (suspicious transactions)
└── analytics-realtime-v1 (real-time analytics)
```

### 3. Fraud Detection Pipeline
```
transactions-enriched-v1 → Feature Store → ML Model → Risk Assessment → fraud-predictions-v1
```

### 4. Monitoring Pipeline
```
All Services → Prometheus → Grafana Dashboards + AlertManager → Team Notifications
```

## 🎯 Key Technical Achievements

### Integrated Architecture
- **Unified ML Service**: Single fraud-detection service containing feature store, model registry, and API
- **Simplified Deployment**: Reduced from multiple ML services to one comprehensive service
- **Streamlined Configuration**: Centralized configuration management
- **Efficient Resource Usage**: Optimized container resource allocation

### Performance Optimizations
- **Kafka LZ4 Compression**: 40-60% space savings
- **Batch Processing**: 1000-record batches for optimal throughput
- **Connection Pooling**: HikariCP with optimized pool sizes
- **Async Processing**: CompletableFuture-based parallel operations
- **Feature Caching**: Redis for sub-millisecond feature retrieval

### Reliability Features
- **Circuit Breakers**: Fail-fast with graceful degradation
- **Retry Mechanisms**: Exponential backoff with jitter
- **Dead Letter Queues**: Error isolation and reprocessing
- **Health Checks**: Multi-level health validation
- **Fallback Models**: Ensemble fallback when primary models fail

### Observability Implementation
- **18 Metric Exporters**: Comprehensive system and business metrics
- **Structured Logging**: JSON logs with correlation IDs
- **Distributed Tracing**: Request flow across microservices
- **Custom Dashboards**: 10 categories of Grafana dashboards
- **Smart Alerting**: Context-aware alerts with runbook links

## 🚀 Deployment Instructions

### Quick Start
```bash
# Start infrastructure and ML services
cd deployment/docker-compose
docker-compose -f docker-compose.refactored.yml up -d

# Start monitoring stack
cd ../../infrastructure/monitoring  
docker-compose -f docker-compose.monitoring.yml up -d

# Initialize databases
cd ../../
./run-all-sql.sh
```

### Service Access Points
| Service | URL | Purpose |
|---------|-----|---------|
| Fraud Detection API | http://localhost:5001 | ML inference endpoint |
| MLflow UI | http://localhost:5000 | Experiment tracking |
| Kafka UI | http://localhost:9090 | Cluster management |
| Grafana | http://localhost:3000 | Dashboards (admin/admin123) |
| Prometheus | http://localhost:9090 | Metrics collection |

### Health Validation
```bash
# Check all services
docker ps

# Validate architecture
./validate-architecture.sh

# Test fraud detection
curl -X POST http://localhost:5001/api/v1/predict \
  -H "Content-Type: application/json" \
  -d '{"transaction_id":"test-123","customer_id":"cust-456","amount":1500.00,"timestamp":"2024-01-15T10:30:00Z"}'
```

## 📈 Monitoring & Observability

### Key Metrics Tracked
- **Business KPIs**: Fraud detection rate, false positives, model accuracy
- **Performance**: API latency, throughput, processing times
- **Infrastructure**: CPU, memory, disk, network usage
- **Data Quality**: Schema violations, missing data, anomalies
- **Kafka**: Topic lag, partition distribution, message throughput

### Alert Strategy
- **P1 (Critical)**: Model failures, API downtime → PagerDuty + Slack
- **P2 (High)**: High error rates, data quality issues → Slack + Email
- **P3 (Medium)**: Performance degradation → Email
- **P4 (Low)**: Capacity warnings → Daily digest

## 🔮 Production Readiness

### Scalability Features
- **Horizontal Scaling**: Kafka partitioning aligned with consumer instances
- **Resource Optimization**: Container resource limits and requests
- **Load Balancing**: Multiple fraud-detection service instances
- **Caching Strategy**: Multi-tier caching (Redis + application)

### Security Considerations
- **Network Isolation**: Docker networks with proper firewall rules
- **Secrets Management**: Environment variable injection
- **API Security**: Input validation and rate limiting
- **Database Security**: Connection encryption and user permissions

### Maintenance Operations
- **Model Updates**: MLflow model deployment pipeline
- **Configuration Changes**: Environment variable updates
- **Scaling Operations**: Docker Compose scale commands
- **Backup Procedures**: Database and artifact backup strategies

This refactored architecture demonstrates a complete, production-ready big data platform with integrated fraud detection capabilities, comprehensive monitoring, and modern DevOps practices. 