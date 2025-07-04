# 🏗️ Data Engineering Architecture Documentation

## 📋 Table of Contents
1. [Project Overview](#project-overview)
2. [System Architecture](#system-architecture)
3. [Technology Stack](#technology-stack)
4. [Data Flow](#data-flow)
5. [Component Details](#component-details)
6. [Database Schema](#database-schema)
7. [Kafka Implementation](#kafka-implementation)
8. [Monitoring & Testing](#monitoring--testing)
9. [Development Environment](#development-environment)
10. [Next Steps: ML & Fraud Detection](#next-steps-ml--fraud-detection)

---

## 🎯 Project Overview

### Purpose
This project implements a **real-time data engineering pipeline** for processing sales transactions, with capabilities for:
- Real-time message streaming with Apache Kafka
- Multi-database persistence (MySQL, PostgreSQL, H2)
- Stream processing and analytics
- Foundation for ML-based fraud detection

### Business Use Case
**E-commerce Sales Processing System** that:
- Ingests sales transactions in real-time
- Processes and validates transaction data
- Stores data for analytics and reporting
- Provides foundation for fraud detection algorithms

---

## 🏗️ System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    DATA ENGINEERING ARCHITECTURE                │
└─────────────────────────────────────────────────────────────────┘

┌──────────────────┐    ┌───────────────────┐    ┌─────────────────┐
│   Data Sources   │    │   Message Queue   │    │   Processing    │
│                  │    │                   │    │                 │
│ • CSV Files      │───▶│   Apache Kafka    │───▶│ • Producers     │
│ • APIs           │    │ • Sales Events    │    │ • Consumers     │
│ • Real-time      │    │ • High Value      │    │ • Streams       │
│   Transactions   │    │   Alerts          │    │   Processing    │
└──────────────────┘    └───────────────────┘    └─────────────────┘
                                 │                         │
                                 │                         ▼
┌──────────────────┐    ┌───────────────────┐    ┌─────────────────┐
│   Monitoring     │    │     Storage       │    │   Analytics     │
│                  │    │                   │    │                 │
│ • Kafka UI       │    │ • MySQL DB        │    │ • ETL Pipeline  │
│ • Adminer        │    │ • PostgreSQL      │    │ • Reporting     │
│ • Java Monitor   │    │ • H2 (Testing)    │    │ • Data Analysis │
│ • Docker Stats   │    │ • Schema Design   │    │ • Visualization │
└──────────────────┘    └───────────────────┘    └─────────────────┘
```

---

## 🛠️ Technology Stack

### Core Technologies
| Component | Technology | Version | Purpose |
|-----------|------------|---------|---------|
| **Message Queue** | Apache Kafka | 2.8+ | Real-time data streaming |
| **Stream Processing** | Kafka Streams | 2.8+ | Real-time analytics |
| **Databases** | MySQL | 8.0 | Production data storage |
| | PostgreSQL | 15 | Analytics & reporting |
| | H2 | 2.1+ | Testing & development |
| **Application** | Java | 11+ | Core business logic |
| **Build Tool** | Maven | 3.6+ | Dependency management |
| **Containerization** | Docker | 20+ | Infrastructure management |

### Supporting Tools
| Tool | Purpose |
|------|---------|
| **Kafka UI** | Web-based Kafka monitoring |
| **Adminer** | Database administration |
| **Zookeeper** | Kafka coordination |
| **Schema Registry** | Schema management |

---

## 🔄 Data Flow

### 1. Data Ingestion Flow
```
CSV Files → Java ETL → SalesRecord Objects → Validation → Database
```

### 2. Real-time Streaming Flow
```
Sales Events → Kafka Producer → Kafka Topic → Kafka Consumer → Database
                     ↓
              Kafka Streams → Analytics → Enriched Topics
```

### 3. Processing Pipeline
```
Raw Data → Validation → Transformation → Enrichment → Storage → Analytics
```

---

## 🧩 Component Details

### 📊 Data Model
**SalesRecord** - Core data entity with:
- `transactionId` - Unique transaction identifier
- `customerId` - Customer reference
- `productName` - Product description
- `productCategory` - Product classification
- `quantity` - Number of items
- `unitPrice` - Price per item
- `totalAmount` - Total transaction value
- `saleDate` - Transaction timestamp
- `storeLocation` - Store identifier
- `salesPerson` - Sales representative

### 🏭 Java Application Components

#### Core Classes
```
src/main/java/com/dataengineering/
├── DataEngineeringApplication.java     # Main application entry point
├── model/
│   └── SalesRecord.java               # Core data model
├── config/
│   ├── DatabaseConfig.java           # Database connection management
│   └── KafkaConfig.java              # Kafka configuration
├── database/
│   └── DatabaseOperations.java       # Database CRUD operations
├── kafka/
│   ├── producer/SalesEventProducer.java   # Message publishing
│   ├── consumer/SalesEventConsumer.java   # Message processing
│   └── streams/SalesStreamProcessor.java  # Stream analytics
└── service/
    └── KafkaIntegrationService.java      # System orchestration
```

#### Key Features
- **ETL Pipeline**: Extract, Transform, Load operations
- **Data Validation**: Input validation and error handling
- **Batch Processing**: Efficient bulk data operations
- **Real-time Processing**: Stream-based analytics
- **Multi-database Support**: H2, MySQL, PostgreSQL

---

## 🗄️ Database Schema

### Sales Records Table
```sql
CREATE TABLE sales_records (
    transaction_id VARCHAR(50) PRIMARY KEY,
    customer_id VARCHAR(50) NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    product_category VARCHAR(100),
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    sale_date TIMESTAMP NOT NULL,
    store_location VARCHAR(100),
    sales_person VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Performance Indexes
- `idx_sales_customer_id` - Customer-based queries
- `idx_sales_date` - Time-based analytics
- `idx_sales_category` - Category analysis
- `idx_sales_location` - Location-based reporting

### Database Connections
| Database | Host | Port | Schema | User |
|----------|------|------|--------|------|
| MySQL | localhost | 3306 | data_engineering | dataeng |
| PostgreSQL | localhost | 5432 | data_engineering | dataeng |
| H2 | in-memory | - | testdb | sa |

---

## 🚀 Kafka Implementation

### Topic Structure
```
sales-events          # Main transaction stream
high-value-sales     # Transactions > $1000
enriched-sales       # Stream-processed data
category-aggregates  # Product category analytics
regional-analytics   # Location-based metrics
```

### Producer Configuration
- **Reliability**: `acks=all`, `retries=Integer.MAX_VALUE`
- **Performance**: Batch processing, async sending
- **Serialization**: JSON with Jackson ObjectMapper
- **Error Handling**: Retry logic and dead letter patterns

### Consumer Configuration
- **Consumer Group**: `sales-processor-group`
- **Offset Management**: Manual commits for reliability
- **Processing**: Business logic + database persistence
- **Error Handling**: Validation and error logging

### Stream Processing Features
- **Real-time Filtering**: High-value transaction detection
- **Aggregations**: Category and regional analytics
- **Windowing**: 5-minute tumbling windows
- **Data Enrichment**: Metadata addition
- **Branch Processing**: Multiple output streams

---

## 📊 Monitoring & Testing

### Available Monitoring Tools
1. **Kafka UI** (http://localhost:9090)
   - Topic management
   - Message browsing
   - Consumer group monitoring

2. **Database Adminer** (http://localhost:8080)
   - Database administration
   - Query execution
   - Schema management

3. **Java Monitoring**
   - `KafkaMonitor.java` - Programmatic monitoring
   - Live cluster information
   - Consumer group status

### Testing Suite
```
├── testing/
│   ├── ProducerTest.java              # Producer functionality
│   ├── ConsumerTest.java              # Consumer operations
│   ├── EndToEndTest.java              # Complete pipeline
│   ├── StreamProcessorTestSuite.java  # Stream processing
│   ├── ProducerConsumerTestSuite.java # Integration tests
│   └── SimpleStreamDemo.java          # Interactive demo
```

### Test Scenarios
- **Unit Tests**: Individual component testing
- **Integration Tests**: End-to-end pipeline validation
- **Load Tests**: Performance under volume
- **Interactive Tests**: Real-time user interaction
- **Monitoring Tests**: System health verification

---

## 🐳 Development Environment

### Docker Infrastructure
```yaml
# Database Services
- MySQL (data_engineering_mysql:3306)
- PostgreSQL (data_engineering_postgres:5432)  
- Adminer (data_engineering_adminer:8080)

# Kafka Services
- Zookeeper (data_engineering_zookeeper:2181)
- Kafka Broker (data_engineering_kafka:9092)
- Schema Registry (data_engineering_schema_registry:8081)
- Kafka UI (data_engineering_kafka_ui:9090)
```

### Quick Start Commands
```bash
# Start databases
docker-compose up -d mysql postgres adminer

# Start Kafka ecosystem  
docker-compose -f docker-compose-kafka.yml up -d

# Run all SQL setup
./run-all-sql.sh

# Test connections
./test-database-connections.sh

# Java application
mvn compile exec:java -Dexec.mainClass="com.dataengineering.DataEngineeringApplication"
```

### Project Structure
```
learning_big_data/
├── src/main/java/com/dataengineering/    # Java source code
├── src/main/resources/                   # Configuration files
├── init-scripts/                         # Database initialization
├── docker-compose.yml                    # Database containers
├── docker-compose-kafka.yml              # Kafka containers
├── pom.xml                               # Maven dependencies
├── *.sh                                  # Utility scripts
└── *.md                                  # Documentation
```

---

## 🎯 Current Capabilities

### ✅ Implemented Features
- [x] **Multi-database ETL Pipeline**
- [x] **Real-time Kafka Streaming**
- [x] **Stream Processing Analytics**
- [x] **Comprehensive Testing Suite**
- [x] **Monitoring & Administration**
- [x] **Docker Infrastructure**
- [x] **Performance Optimizations**
- [x] **Data Validation & Error Handling**

### 📊 Analytics Capabilities
- Revenue analysis and reporting
- Product category performance
- Customer behavior tracking
- Time-based trend analysis
- Geographic distribution analysis
- High-value transaction alerts

### 🔧 Operational Features
- Graceful shutdown handling
- Connection pooling ready
- Batch processing optimization
- Real-time health monitoring
- Automated testing suites
- Development environment automation

---

## 🚀 Next Steps: ML & Fraud Detection

### Planned ML Architecture Addition
```
Current Architecture + Apache Spark ML Pipeline
                          ↓
┌─────────────────────────────────────────────────────────────────┐
│                    ML-ENHANCED ARCHITECTURE                     │
└─────────────────────────────────────────────────────────────────┘

Kafka Streams ──→ Feature Engineering ──→ ML Model ──→ Fraud Alerts
     ↓                     ↓                   ↓            ↓
Historical Data ──→ Training Pipeline ──→ Model Storage ──→ Dashboard
```

### Star Schema for Fraud Detection
Will implement dimensional modeling with:
- **Fact Table**: `fraud_transactions` (transaction measures + risk scores)
- **Dimensions**: customers, products, stores, payment_methods, date, time
- **Features**: Amount anomalies, behavioral patterns, temporal patterns

### ML Pipeline Components
1. **Feature Engineering**: Real-time feature extraction from streams
2. **Model Training**: Spark MLlib for fraud detection models
3. **Real-time Scoring**: Live fraud probability calculation
4. **Alert System**: Automated fraud alert generation
5. **Model Management**: Version control and A/B testing

### Integration Points
- **Data Source**: Current Kafka streams + historical database
- **Feature Store**: Enriched features for ML training
- **Model Serving**: Real-time prediction API
- **Feedback Loop**: Model retraining based on outcomes

---

## 📝 Summary

This architecture provides a **solid foundation** for enterprise-grade data engineering:

### 🎯 **Strengths**
- **Scalable**: Kafka-based streaming handles high throughput
- **Reliable**: Multi-database redundancy and error handling
- **Testable**: Comprehensive testing and monitoring
- **Maintainable**: Clean code structure and documentation
- **Extensible**: Ready for ML and advanced analytics

### 🔧 **Ready for Enhancement**
- **ML Integration**: Foundation prepared for Spark ML pipeline
- **Fraud Detection**: Schema and data flow designed for ML features
- **Advanced Analytics**: Stream processing ready for complex algorithms
- **Production Deployment**: Container-ready infrastructure

**The current architecture serves as a robust platform for implementing machine learning-based fraud detection while maintaining all existing capabilities.**

---

*Documentation Version: 1.0*  
*Last Updated: July 2025*  
*Next Phase: Apache Spark ML Integration for Fraud Detection* 