package com.dataengineering;

import com.dataengineering.config.DatabaseConfig;
import com.dataengineering.config.DatabaseConfig.DatabaseType;
import com.dataengineering.database.DatabaseOperations;
import com.dataengineering.ingestion.CsvDataIngestion;
import com.dataengineering.ingestion.JsonDataIngestion;
import com.dataengineering.model.SalesRecord;
import com.dataengineering.processing.DataProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * DataEngineeringApplication - Main application demonstrating a complete data engineering pipeline.
 * 
 * This application showcases:
 * - Data ingestion from multiple sources (CSV, JSON)
 * - Data validation and quality checks
 * - Data transformation and processing
 * - Database operations and persistence
 * - Data analysis and reporting
 * - Interactive pipeline execution
 * 
 * Learning Goals:
 * - Understanding end-to-end data pipelines
 * - Best practices in data engineering
 * - Error handling and logging
 * - Performance considerations
 * - Modular architecture design
 */
public class DataEngineeringApplication {
    
    private static final Logger logger = LoggerFactory.getLogger(DataEngineeringApplication.class);
    
    // Application components
    private final CsvDataIngestion csvIngestion;
    private final JsonDataIngestion jsonIngestion;
    private final DatabaseOperations dbOperations;
    private final DataProcessor dataProcessor;
    private final Scanner scanner;
    
    // Configuration
    private static final String CSV_FILE_PATH = "data/sales_data.csv";
    private static final String JSON_FILE_PATH = "data/sales_data.json";
    private static final DatabaseType DEFAULT_DB_TYPE = DatabaseType.H2;
    
    public DataEngineeringApplication() {
        this.csvIngestion = new CsvDataIngestion();
        this.jsonIngestion = new JsonDataIngestion();
        this.dbOperations = new DatabaseOperations();
        this.dataProcessor = new DataProcessor();
        this.scanner = new Scanner(System.in);
    }
    
    /**
     * Main entry point for the Data Engineering application.
     */
    public static void main(String[] args) {
        logger.info("🚀 Starting Data Engineering Learning Application");
        logger.info("================================================");
        
        DataEngineeringApplication app = new DataEngineeringApplication();
        
        try {
            app.run();
        } catch (Exception e) {
            logger.error("❌ Application failed: {}", e.getMessage(), e);
            System.exit(1);
        } finally {
            app.cleanup();
        }
        
        logger.info("✅ Application completed successfully");
        System.exit(0);
    }
    
    /**
     * Main application loop with interactive menu.
     */
    public void run() throws Exception {
        // Initialize the application
        initialize();
        
        boolean running = true;
        while (running) {
            displayMenu();
            int choice = getUserChoice();
            
            switch (choice) {
                case 1:
                    runCompleteETLPipeline();
                    break;
                case 2:
                    demonstrateDataIngestion();
                    break;
                case 3:
                    demonstrateDataProcessing();
                    break;
                case 4:
                    demonstrateDatabaseOperations();
                    break;
                case 5:
                    demonstrateDataAnalysis();
                    break;
                case 6:
                    testDatabaseConnections();
                    break;
                case 7:
                    generateSampleData();
                    break;
                case 8:
                    logger.info("👋 Goodbye! Thanks for learning Data Engineering with Java!");
                    running = false;
                    break;
                default:
                    logger.warn("⚠️ Invalid choice. Please try again.");
                    break;
            }
            
            if (running) {
                System.out.println("\nPress Enter to continue...");
                scanner.nextLine();
            }
        }
    }
    
    /**
     * Initialize the application and set up database schema.
     */
    private void initialize() throws Exception {
        logger.info("🔧 Initializing Data Engineering Application...");
        
        // Test database connection
        logger.info("🔗 Testing database connection...");
        dbOperations.createTable(DEFAULT_DB_TYPE);
        
        // Create data directory if it doesn't exist
        java.nio.file.Files.createDirectories(java.nio.file.Paths.get("data"));
        
        logger.info("✅ Application initialized successfully");
    }
    
    /**
     * Demonstrates a complete ETL (Extract, Transform, Load) pipeline.
     */
    private void runCompleteETLPipeline() throws Exception {
        logger.info("🏭 Running Complete ETL Pipeline");
        logger.info("==============================");
        
        // Console output for visibility
        System.out.println("\n🏭 Running Complete ETL Pipeline");
        System.out.println("==============================");
        
        // Step 1: Extract - Generate sample data if not exists
        if (!java.nio.file.Files.exists(java.nio.file.Paths.get(CSV_FILE_PATH))) {
            logger.info("📝 No sample data found. Generating sample CSV file...");
            System.out.println("📝 No sample data found. Generating sample CSV file...");
            csvIngestion.createSampleCsvFile(CSV_FILE_PATH);
        }
        
        // Step 2: Extract - Data Ingestion
        logger.info("📥 EXTRACT: Ingesting data from CSV file...");
        System.out.println("📥 EXTRACT: Ingesting data from CSV file...");
        List<SalesRecord> records = csvIngestion.ingestFromCsv(CSV_FILE_PATH);
        logger.info("✅ Extracted {} records", records.size());
        System.out.println("✅ Extracted " + records.size() + " records");
        
        // Step 3: Transform - Data Processing and Validation
        logger.info("🔄 TRANSFORM: Processing and validating data...");
        System.out.println("\n🔄 TRANSFORM: Processing and validating data...");
        
        // Data enrichment
        records = dataProcessor.enrichRecords(records);
        
        // Data filtering (example: only records from last 30 days with amount > $10)
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        List<SalesRecord> filteredRecords = dataProcessor.filterRecords(
            records, new BigDecimal("10.00"), thirtyDaysAgo, null, null);
        
        // Anomaly detection
        List<SalesRecord> anomalies = dataProcessor.detectAnomalies(filteredRecords);
        if (!anomalies.isEmpty()) {
            logger.warn("⚠️ Found {} anomalies in the data", anomalies.size());
            System.out.println("⚠️ Found " + anomalies.size() + " anomalies in the data");
        }
        
        logger.info("✅ Transformed {} records", filteredRecords.size());
        System.out.println("✅ Transformed " + filteredRecords.size() + " records");
        
        // Step 4: Load - Database Persistence
        logger.info("💾 LOAD: Storing data in database...");
        System.out.println("\n💾 LOAD: Storing data in database...");
        
        // Clear existing data
        dbOperations.deleteAllRecords(DEFAULT_DB_TYPE);
        
        // Batch insert for performance
        dbOperations.insertRecordsBatch(filteredRecords, DEFAULT_DB_TYPE, 100);
        
        // Verify data load
        long recordCount = dbOperations.getRecordCount(DEFAULT_DB_TYPE);
        logger.info("✅ Loaded {} records to database", recordCount);
        System.out.println("✅ Loaded " + recordCount + " records to database");
        
        // Step 5: Analysis and Reporting
        logger.info("📊 Generating analysis reports...");
        System.out.println("\n📊 Generating analysis reports...");
        generateAnalysisReports(filteredRecords);
        
        logger.info("🎉 ETL Pipeline completed successfully!");
        System.out.println("🎉 ETL Pipeline completed successfully!");
    }
    
    /**
     * Demonstrates different data ingestion methods.
     */
    private void demonstrateDataIngestion() throws Exception {
        logger.info("📥 Data Ingestion Demonstration");
        logger.info("==============================");
        
        // Console output for visibility
        System.out.println("\n📥 Data Ingestion Demonstration");
        System.out.println("==============================");
        
        // Generate sample files if they don't exist
        if (!java.nio.file.Files.exists(java.nio.file.Paths.get(CSV_FILE_PATH))) {
            System.out.println("📄 Creating sample CSV file...");
            csvIngestion.createSampleCsvFile(CSV_FILE_PATH);
        }
        
        if (!java.nio.file.Files.exists(java.nio.file.Paths.get(JSON_FILE_PATH))) {
            System.out.println("📄 Creating sample JSON file...");
            jsonIngestion.createSampleJsonFile(JSON_FILE_PATH);
        }
        
        // CSV Ingestion
        logger.info("📄 CSV Ingestion Example:");
        System.out.println("\n📄 CSV Ingestion Example:");
        List<SalesRecord> csvRecords = csvIngestion.ingestFromCsv(CSV_FILE_PATH);
        logger.info("✅ CSV: Ingested {} records", csvRecords.size());
        System.out.println("✅ CSV: Ingested " + csvRecords.size() + " records");
        
        // JSON Ingestion
        logger.info("📄 JSON Ingestion Example:");
        System.out.println("\n📄 JSON Ingestion Example:");
        List<SalesRecord> jsonRecords = jsonIngestion.ingestFromJson(JSON_FILE_PATH);
        logger.info("✅ JSON: Ingested {} records", jsonRecords.size());
        System.out.println("✅ JSON: Ingested " + jsonRecords.size() + " records");
        
        // Demonstrate validation
        logger.info("🔍 Data Validation Example:");
        System.out.println("\n🔍 Data Validation Example:");
        long validCsvRecords = csvRecords.stream().mapToLong(r -> r.isValid() ? 1 : 0).sum();
        long validJsonRecords = jsonRecords.stream().mapToLong(r -> r.isValid() ? 1 : 0).sum();
        
        logger.info("✅ CSV: {}/{} valid records", validCsvRecords, csvRecords.size());
        logger.info("✅ JSON: {}/{} valid records", validJsonRecords, jsonRecords.size());
        System.out.println("✅ CSV: " + validCsvRecords + "/" + csvRecords.size() + " valid records");
        System.out.println("✅ JSON: " + validJsonRecords + "/" + jsonRecords.size() + " valid records");
        
        System.out.println("\n✅ Data ingestion demonstration completed!\n");
    }
    
    /**
     * Demonstrates data processing and transformation operations.
     */
    private void demonstrateDataProcessing() throws Exception {
        logger.info("🔄 Data Processing Demonstration");
        logger.info("===============================");
        
        // Console output for visibility
        System.out.println("\n🔄 Data Processing Demonstration");
        System.out.println("===============================");
        
        // Load sample data
        if (!java.nio.file.Files.exists(java.nio.file.Paths.get(CSV_FILE_PATH))) {
            System.out.println("📄 Creating sample CSV file...");
            csvIngestion.createSampleCsvFile(CSV_FILE_PATH);
        }
        
        List<SalesRecord> records = csvIngestion.ingestFromCsv(CSV_FILE_PATH);
        logger.info("📊 Processing {} sample records", records.size());
        System.out.println("📊 Processing " + records.size() + " sample records");
        
        // Filtering example
        logger.info("🔍 Filtering Example (Electronics only, > $50):");
        System.out.println("\n🔍 Filtering Example (Electronics only, > $50):");
        List<SalesRecord> filteredRecords = dataProcessor.filterRecords(
            records, new BigDecimal("50.00"), null, null, "Electronics");
        logger.info("✅ Filtered to {} records", filteredRecords.size());
        System.out.println("✅ Filtered to " + filteredRecords.size() + " records");
        
        // Grouping and aggregation examples
        logger.info("📊 Grouping by Category:");
        System.out.println("\n📊 Grouping by Category:");
        Map<String, DataProcessor.CategorySummary> categoryStats = dataProcessor.groupByCategory(records);
        System.out.println("✅ Found " + categoryStats.size() + " categories");
        
        logger.info("👥 Grouping by Customer:");
        System.out.println("\n👥 Grouping by Customer:");
        Map<String, DataProcessor.CustomerSummary> customerStats = dataProcessor.groupByCustomer(records);
        System.out.println("✅ Analyzed " + customerStats.size() + " customers");
        
        logger.info("📅 Grouping by Date:");
        System.out.println("\n📅 Grouping by Date:");
        Map<LocalDate, DataProcessor.DailySummary> dailyStats = dataProcessor.groupByDate(records);
        System.out.println("✅ Aggregated data for " + dailyStats.size() + " days");
        
        // Top products example
        logger.info("🏆 Top Products by Revenue:");
        System.out.println("\n🏆 Top Products by Revenue:");
        List<DataProcessor.ProductSummary> topProducts = dataProcessor.getTopProducts(records, 3, true);
        for (int i = 0; i < topProducts.size(); i++) {
            DataProcessor.ProductSummary product = topProducts.get(i);
            System.out.println("  " + (i + 1) + ". " + product.getProductName() + ": $" + product.getTotalRevenue());
        }
        
        // Anomaly detection
        logger.info("🔍 Anomaly Detection:");
        System.out.println("\n🔍 Anomaly Detection:");
        List<SalesRecord> anomalies = dataProcessor.detectAnomalies(records);
        System.out.println("✅ Checked for anomalies - found " + anomalies.size() + " potential issues");
        
        System.out.println("\n✅ Data processing demonstration completed!\n");
    }
    
    /**
     * Demonstrates database operations.
     */
    private void demonstrateDatabaseOperations() throws Exception {
        logger.info("🗄️ Database Operations Demonstration");
        logger.info("===================================");
        
        // Console output for visibility
        System.out.println("\n🗄️ Database Operations Demonstration");
        System.out.println("===================================");
        
        // Load sample data
        if (!java.nio.file.Files.exists(java.nio.file.Paths.get(CSV_FILE_PATH))) {
            System.out.println("📄 Creating sample CSV file...");
            csvIngestion.createSampleCsvFile(CSV_FILE_PATH);
        }
        
        List<SalesRecord> records = csvIngestion.ingestFromCsv(CSV_FILE_PATH);
        
        // Clear and insert data
        logger.info("💾 Inserting {} records into database...", records.size());
        System.out.println("💾 Inserting " + records.size() + " records into database...");
        dbOperations.deleteAllRecords(DEFAULT_DB_TYPE);
        dbOperations.insertRecordsBatch(records, DEFAULT_DB_TYPE, 50);
        
        // Query operations
        logger.info("🔍 Querying all records:");
        System.out.println("\n🔍 Querying all records:");
        List<SalesRecord> allRecords = dbOperations.getAllRecords(DEFAULT_DB_TYPE);
        logger.info("✅ Retrieved {} records from database", allRecords.size());
        System.out.println("✅ Retrieved " + allRecords.size() + " records from database");
        
        // Customer-specific queries
        if (!allRecords.isEmpty()) {
            String sampleCustomerId = allRecords.get(0).getCustomerId();
            logger.info("🔍 Querying records for customer: {}", sampleCustomerId);
            System.out.println("\n🔍 Querying records for customer: " + sampleCustomerId);
            List<SalesRecord> customerRecords = dbOperations.getRecordsByCustomer(sampleCustomerId, DEFAULT_DB_TYPE);
            logger.info("✅ Found {} records for customer {}", customerRecords.size(), sampleCustomerId);
            System.out.println("✅ Found " + customerRecords.size() + " records for customer " + sampleCustomerId);
        }
        
        // Record count
        long totalCount = dbOperations.getRecordCount(DEFAULT_DB_TYPE);
        logger.info("📊 Total records in database: {}", totalCount);
        System.out.println("\n📊 Total records in database: " + totalCount);
        
        System.out.println("\n✅ Database operations demonstration completed!\n");
    }
    
    /**
     * Demonstrates data analysis and reporting.
     */
    private void demonstrateDataAnalysis() throws Exception {
        logger.info("📊 Data Analysis Demonstration");
        logger.info("=============================");
        
        // Also print to console for visibility
        System.out.println("\n📊 Data Analysis Demonstration");
        System.out.println("=============================");
        
        // Load data from database
        System.out.println("🔍 Loading data from database...");
        List<SalesRecord> records = dbOperations.getAllRecords(DEFAULT_DB_TYPE);
        
        if (records.isEmpty()) {
            logger.info("📝 No data in database. Loading sample data...");
            System.out.println("📝 No data in database. Loading sample data...");
            
            if (!java.nio.file.Files.exists(java.nio.file.Paths.get(CSV_FILE_PATH))) {
                System.out.println("📄 Creating sample CSV file...");
                csvIngestion.createSampleCsvFile(CSV_FILE_PATH);
            }
            
            System.out.println("📥 Reading sample data from CSV...");
            records = csvIngestion.ingestFromCsv(CSV_FILE_PATH);
            
            System.out.println("💾 Inserting " + records.size() + " records into database...");
            dbOperations.insertRecordsBatch(records, DEFAULT_DB_TYPE, 100);
        } else {
            System.out.println("✅ Found " + records.size() + " records in database");
        }
        
        generateAnalysisReports(records);
    }
    
    /**
     * Tests database connections for all supported database types.
     */
    private void testDatabaseConnections() {
        logger.info("🔧 Database Connection Testing");
        logger.info("=============================");
        
        DatabaseConfig.testConnections();
    }
    
    /**
     * Generates sample data files for testing.
     */
    private void generateSampleData() throws Exception {
        logger.info("📝 Sample Data Generation");
        logger.info("========================");
        
        // Console output for visibility
        System.out.println("\n📝 Sample Data Generation");
        System.out.println("========================");
        
        logger.info("📄 Generating sample CSV file...");
        System.out.println("📄 Generating sample CSV file...");
        csvIngestion.createSampleCsvFile(CSV_FILE_PATH);
        
        logger.info("📄 Generating sample JSON file...");
        System.out.println("📄 Generating sample JSON file...");
        jsonIngestion.createSampleJsonFile(JSON_FILE_PATH);
        
        logger.info("✅ Sample data files created successfully!");
        logger.info("📂 CSV file: {}", CSV_FILE_PATH);
        logger.info("📂 JSON file: {}", JSON_FILE_PATH);
        
        System.out.println("✅ Sample data files created successfully!");
        System.out.println("📂 CSV file: " + CSV_FILE_PATH);
        System.out.println("📂 JSON file: " + JSON_FILE_PATH);
        System.out.println("\n✅ Sample data generation completed!\n");
    }
    
    /**
     * Generates comprehensive analysis reports.
     */
    private void generateAnalysisReports(List<SalesRecord> records) {
        logger.info("📊 Generating Analysis Reports...");
        logger.info("=================================");
        
        // Also print to console to ensure visibility
        System.out.println("\n📊 Generating Analysis Reports...");
        System.out.println("=================================");
        
        // Basic statistics
        BigDecimal totalRevenue = records.stream()
            .map(SalesRecord::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        int totalTransactions = records.size();
        int uniqueCustomers = (int) records.stream()
            .map(SalesRecord::getCustomerId)
            .distinct()
            .count();
        
        logger.info("💰 Total Revenue: ${}", totalRevenue);
        logger.info("🧾 Total Transactions: {}", totalTransactions);
        logger.info("👥 Unique Customers: {}", uniqueCustomers);
        
        // Console output for visibility
        System.out.println("💰 Total Revenue: $" + totalRevenue);
        System.out.println("🧾 Total Transactions: " + totalTransactions);
        System.out.println("👥 Unique Customers: " + uniqueCustomers);
        
        // Category analysis
        Map<String, DataProcessor.CategorySummary> categoryStats = dataProcessor.groupByCategory(records);
        logger.info("📊 Category Performance (Top 3):");
        System.out.println("\n📊 Category Performance (Top 3):");
        
        categoryStats.entrySet().stream()
            .sorted(Map.Entry.<String, DataProcessor.CategorySummary>comparingByValue(
                (a, b) -> b.getTotalAmount().compareTo(a.getTotalAmount())))
            .limit(3)
            .forEach(entry -> {
                DataProcessor.CategorySummary summary = entry.getValue();
                String message = String.format("  🏷️  %s: $%s (%d transactions)", 
                           entry.getKey(), summary.getTotalAmount(), summary.getTransactionCount());
                logger.info(message);
                System.out.println(message);
            });
        
        // Top products
        List<DataProcessor.ProductSummary> topProducts = dataProcessor.getTopProducts(records, 3, true);
        logger.info("🏆 Top Products by Revenue:");
        System.out.println("\n🏆 Top Products by Revenue:");
        
        for (int i = 0; i < topProducts.size(); i++) {
            DataProcessor.ProductSummary product = topProducts.get(i);
            String message = String.format("  %d. %s: $%s (%d units)", 
                       i + 1, product.getProductName(), product.getTotalRevenue(), product.getTotalQuantity());
            logger.info(message);
            System.out.println(message);
        }
        
        // Daily performance
        Map<LocalDate, DataProcessor.DailySummary> dailyStats = dataProcessor.groupByDate(records);
        logger.info("📅 Daily Performance Summary:");
        System.out.println("\n📅 Daily Performance Summary:");
        
        dailyStats.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> {
                DataProcessor.DailySummary summary = entry.getValue();
                String message = String.format("  📆 %s: $%s (%d transactions, %d customers)", 
                           entry.getKey(), summary.getTotalRevenue(), 
                           summary.getTotalTransactions(), summary.getUniqueCustomers());
                logger.info(message);
                System.out.println(message);
            });
        
        System.out.println("\n✅ Analysis completed!\n");
    }
    
    /**
     * Displays the main menu options.
     */
    private void displayMenu() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🎓 DATA ENGINEERING LEARNING APPLICATION");
        System.out.println("=".repeat(60));
        System.out.println("1. 🏭 Run Complete ETL Pipeline");
        System.out.println("2. 📥 Demonstrate Data Ingestion");
        System.out.println("3. 🔄 Demonstrate Data Processing");
        System.out.println("4. 🗄️  Demonstrate Database Operations");
        System.out.println("5. 📊 Demonstrate Data Analysis");
        System.out.println("6. 🔧 Test Database Connections");
        System.out.println("7. 📝 Generate Sample Data");
        System.out.println("8. 🚪 Exit");
        System.out.println("=".repeat(60));
        System.out.print("Enter your choice (1-8): ");
    }
    
    /**
     * Gets user choice from the menu.
     */
    private int getUserChoice() {
        try {
            int choice = Integer.parseInt(scanner.nextLine());
            return choice;
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    
    /**
     * Cleanup resources before application exit.
     */
    private void cleanup() {
        if (scanner != null) {
            scanner.close();
        }
    }
} 