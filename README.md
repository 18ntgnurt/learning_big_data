# 🎓 Learning Data Engineering with Java

A comprehensive, hands-on project designed to teach data engineering concepts using Java. This project demonstrates real-world data engineering patterns, best practices, and technologies through practical examples.

## 📋 Documentation

- **[🏗️ ARCHITECTURE_README.md](ARCHITECTURE_README.md)** - Complete system architecture documentation
- **[🚀 KAFKA Implementation Guides](KAFKA_IMPLEMENTATION_GUIDE.md)** - Kafka streaming setup and usage
- **[🔧 Testing & Monitoring](KAFKA_MONITORING_GUIDE.md)** - System monitoring and testing approaches

## 🎯 Learning Objectives

By working through this project, you will learn:

- **Data Ingestion**: Reading data from various sources (CSV, JSON, databases)
- **Data Processing**: Transforming, validating, and enriching data
- **Real-time Streaming**: Apache Kafka for real-time data processing
- **Stream Analytics**: Kafka Streams for real-time analytics
- **Data Storage**: Multi-database operations and batch processing
- **Data Analysis**: Aggregations, grouping, and reporting
- **ETL Pipelines**: Complete Extract-Transform-Load workflows
- **Data Quality**: Validation, cleansing, and anomaly detection
- **Monitoring**: System health and performance monitoring
- **Best Practices**: Error handling, logging, performance optimization

## 🏗️ Project Structure

```
learning-data-engineering/
├── src/main/java/com/dataengineering/
│   ├── DataEngineeringApplication.java  # Main application entry point
│   ├── config/
│   │   ├── DatabaseConfig.java          # Database connection management
│   │   └── KafkaConfig.java            # Kafka configuration
│   ├── model/
│   │   └── SalesRecord.java             # Core data model with validation
│   ├── ingestion/
│   │   ├── CsvDataIngestion.java        # CSV file processing
│   │   └── JsonDataIngestion.java       # JSON file processing
│   ├── processing/
│   │   └── DataProcessor.java           # Data transformation & analysis
│   ├── database/
│   │   └── DatabaseOperations.java      # Database CRUD operations
│   ├── kafka/
│   │   ├── producer/SalesEventProducer.java  # Message publishing
│   │   ├── consumer/SalesEventConsumer.java  # Message processing
│   │   ├── streams/SalesStreamProcessor.java # Stream analytics
│   │   ├── testing/                     # Comprehensive test suites
│   │   └── monitoring/                  # Monitoring utilities
│   └── service/
│       └── KafkaIntegrationService.java # System orchestration
├── src/main/resources/
│   ├── logback.xml                      # Logging configuration
│   └── kafka/kafka.properties           # Kafka properties
├── init-scripts/                        # Database initialization SQL
├── data/                                # Generated sample data files
├── logs/                                # Application logs
├── docker-compose.yml                   # Database containers
├── docker-compose-kafka.yml             # Kafka ecosystem
├── *.sh                                 # Utility scripts
├── *.md                                 # Documentation files
├── pom.xml                              # Maven dependencies
└── README.md                            # This file
```

## 🚀 Getting Started

### Prerequisites

- **Java 11 or higher** - The project uses modern Java features
- **Maven 3.6+** - For dependency management and building
- **Docker** - For running databases and Kafka
- **IDE** (IntelliJ IDEA, Eclipse, or VS Code) - For code editing

### Quick Start

1. **Clone or download this project**
   ```bash
   cd learning_big_data
   ```

2. **Start the infrastructure**
   ```bash
   # Start databases
   docker-compose up -d mysql postgres adminer
   
   # Start Kafka ecosystem
   docker-compose -f docker-compose-kafka.yml up -d
   
   # Initialize databases
   ./run-all-sql.sh
   ```

3. **Verify setup**
   ```bash
   # Test all connections
   ./test-database-connections.sh
   
   # Validate architecture
   ./validate-architecture.sh
   ```

4. **Run the application**
   ```bash
   mvn compile exec:java -Dexec.mainClass="com.dataengineering.DataEngineeringApplication"
   ```

## 🎮 Using the Application

When you run the application, you'll see an interactive menu:

```
============================================================
🎓 DATA ENGINEERING LEARNING APPLICATION
============================================================
1. 🏭 Run Complete ETL Pipeline
2. 📥 Demonstrate Data Ingestion
3. 🔄 Demonstrate Data Processing
4. 🗄️ Demonstrate Database Operations
5. 📊 Demonstrate Data Analysis
6. 🔧 Test Database Connections
7. 📝 Generate Sample Data
8. 🚪 Exit
============================================================
```

### Recommended Learning Path

1. **Start with option 7** - Generate sample data to understand the data structure
2. **Try option 2** - Learn about data ingestion from different sources
3. **Explore option 3** - Understand data processing and transformations
4. **Use option 4** - Learn database operations and persistence
5. **Analyze with option 5** - Explore data analysis and reporting
6. **Test option 6** - Verify database connectivity
7. **Run option 1** - Execute the complete ETL pipeline

### Real-time Streaming Components

For Kafka streaming and real-time processing:

```bash
# Test Kafka producer-consumer
mvn exec:java -Dexec.mainClass="com.dataengineering.kafka.testing.ProducerConsumerTestSuite"

# Try stream processing
mvn exec:java -Dexec.mainClass="com.dataengineering.kafka.testing.StreamProcessorTestSuite"

# Interactive streaming demo
mvn exec:java -Dexec.mainClass="com.dataengineering.kafka.testing.SimpleStreamDemo"
```

## 📚 Key Concepts Demonstrated

### 1. Data Modeling (`SalesRecord.java`)
- **Object-oriented data representation**
- **Data validation and business rules**
- **JSON serialization/deserialization**
- **Proper equals/hashCode implementation**

### 2. Data Ingestion (`CsvDataIngestion.java`, `JsonDataIngestion.java`)
- **File parsing with error handling**
- **Data format conversion**
- **Batch processing patterns**
- **Progress monitoring and logging**

### 3. Data Processing (`DataProcessor.java`)
- **Stream API for data transformation**
- **Filtering and aggregation operations**
- **Grouping and statistical analysis**
- **Anomaly detection algorithms**
- **Data enrichment patterns**

### 4. Database Operations (`DatabaseOperations.java`)
- **JDBC connectivity and configuration**
- **Batch insert operations for performance**
- **Transaction management**
- **Parameterized queries for security**
- **Connection pooling concepts**

### 5. Configuration Management (`DatabaseConfig.java`)
- **Multi-database support (H2, MySQL, PostgreSQL)**
- **Connection management and cleanup**
- **Environment-specific configurations**

## 🛠️ Technologies Used

- **Java 11+** - Modern Java features and streams
- **Maven** - Dependency management and build tool
- **H2 Database** - In-memory database for learning
- **MySQL/PostgreSQL** - Production database examples
- **Jackson** - JSON processing
- **Apache Commons CSV** - CSV file processing
- **SLF4J + Logback** - Logging framework
- **JUnit 5** - Testing framework

## 📊 Sample Data

The application generates realistic sales data with the following structure:

```java
{
  "transaction_id": "TXN001",
  "customer_id": "CUST001", 
  "product_name": "Laptop Pro",
  "product_category": "Electronics",
  "quantity": 1,
  "unit_price": 1299.99,
  "total_amount": 1299.99,
  "sale_date": "2023-10-01 10:30:00",
  "store_location": "New York",
  "sales_person": "John Smith"
}
```

## 🔧 Configuration

### Database Configuration

The application supports multiple databases:

- **H2** (default) - No setup required, runs in-memory
- **MySQL** - Modify connection details in `DatabaseConfig.java`
- **PostgreSQL** - Modify connection details in `DatabaseConfig.java`

### Logging Configuration

Logs are configured in `src/main/resources/logback.xml`:
- Console output for immediate feedback
- File logging with daily rotation
- Configurable log levels

## 🎯 Hands-On Exercises

### Exercise 1: Data Validation
- Modify `SalesRecord.java` to add new validation rules
- Add a field for "discount_percentage" with validation
- Test with invalid data to see error handling

### Exercise 2: Custom Data Processing
- Add a new method in `DataProcessor.java` to find seasonal trends
- Implement customer segmentation based on purchase behavior
- Create a method to identify the best-selling day of the week

### Exercise 3: Database Schema Enhancement
- Add an index to the database table for better performance
- Create a new table for customer information
- Implement a join query to combine sales and customer data

### Exercise 4: New Data Sources
- Create a new ingestion class for XML files
- Add support for reading from a REST API
- Implement real-time data streaming simulation

### Exercise 5: Advanced Analytics
- Implement time-series analysis for sales trends
- Add forecasting capabilities
- Create data visualization export (CSV for external tools)

## 🐛 Troubleshooting

### Common Issues

1. **Java Version Error**
   ```
   Error: Java version not supported
   Solution: Ensure Java 11+ is installed and set as JAVA_HOME
   ```

2. **Maven Dependencies**
   ```
   Error: Could not resolve dependencies
   Solution: Run 'mvn clean install' to download dependencies
   ```

3. **Database Connection**
   ```
   Error: Could not connect to database
   Solution: Use H2 (default) or verify MySQL/PostgreSQL setup
   ```

4. **Memory Issues**
   ```
   Error: OutOfMemoryError
   Solution: Increase JVM heap size: -Xmx2g
   ```

## 📈 Performance Tips

1. **Batch Processing**: Use batch operations for large datasets
2. **Connection Pooling**: Implement connection pooling for production
3. **Stream Processing**: Use Java Streams for efficient data processing
4. **Indexing**: Add database indexes for frequently queried columns
5. **Memory Management**: Process data in chunks for large files

## 🚀 Next Steps

After mastering this project, consider exploring:

- **Apache Spark** for big data processing
- **Apache Kafka** for real-time streaming
- **Apache Airflow** for workflow orchestration
- **Docker** for containerization
- **Cloud platforms** (AWS, GCP, Azure) for scalable solutions

## 🤝 Contributing

Feel free to enhance this learning project:

1. Add new data sources (XML, Parquet, Avro)
2. Implement additional processing algorithms
3. Add more comprehensive testing
4. Create performance benchmarks
5. Add documentation and examples

## 📝 License

This is an educational project designed for learning purposes.

## 🎓 Learning Resources

- [Java Streams Documentation](https://docs.oracle.com/javase/8/docs/api/java/util/stream/package-summary.html)
- [JDBC Best Practices](https://docs.oracle.com/javase/tutorial/jdbc/)
- [Maven Getting Started](https://maven.apache.org/guides/getting-started/)
- [Data Engineering Fundamentals](https://www.oreilly.com/library/view/fundamentals-of-data/9781098108298/)

---

**Happy Learning! 🎉**

Start with the basics, experiment with the code, and gradually work your way up to more complex data engineering concepts. Remember, the best way to learn data engineering is by doing! 