package com.dataengineering.ingestion;

import com.dataengineering.model.SalesRecord;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JsonDataIngestion demonstrates how to ingest data from JSON files.
 * JSON is another common format in modern data engineering pipelines.
 * 
 * Key Learning Points:
 * - JSON parsing and serialization
 * - Object mapping with Jackson
 * - Handling different JSON structures
 * - Error handling for malformed JSON
 * - Type safety in data ingestion
 */
public class JsonDataIngestion {
    
    private static final Logger logger = LoggerFactory.getLogger(JsonDataIngestion.class);
    private final ObjectMapper objectMapper;
    
    public JsonDataIngestion() {
        // Configure ObjectMapper for proper JSON handling
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule()); // For LocalDateTime support
    }
    
    /**
     * Reads sales records from a JSON file.
     * 
     * @param filePath Path to the JSON file
     * @return List of SalesRecord objects
     * @throws IOException If file reading or JSON parsing fails
     */
    public List<SalesRecord> ingestFromJson(String filePath) throws IOException {
        logger.info("🚀 Starting JSON ingestion from file: {}", filePath);
        
        File jsonFile = new File(filePath);
        if (!jsonFile.exists()) {
            throw new IOException("JSON file not found: " + filePath);
        }
        
        List<SalesRecord> records = new ArrayList<>();
        
        try {
            // Read JSON file and parse into list of SalesRecord objects
            List<SalesRecord> parsedRecords = objectMapper.readValue(
                jsonFile, 
                new TypeReference<List<SalesRecord>>() {}
            );
            
            long validRecords = 0;
            long invalidRecords = 0;
            
            // Validate each record
            for (SalesRecord record : parsedRecords) {
                if (record.isValid()) {
                    records.add(record);
                    validRecords++;
                } else {
                    invalidRecords++;
                    logger.warn("❌ Invalid record: {} - Errors: {}", 
                              record.getTransactionId(), record.getValidationErrors());
                }
            }
            
            logger.info("✅ JSON ingestion completed!");
            logger.info("📈 Total records parsed: {}", parsedRecords.size());
            logger.info("✅ Valid records: {}", validRecords);
            logger.info("❌ Invalid records: {}", invalidRecords);
            
        } catch (IOException e) {
            logger.error("❌ Error reading JSON file: {}", e.getMessage());
            throw e;
        }
        
        return records;
    }
    
    /**
     * Reads a single JSON object from a file.
     * Useful for reading configuration files or single record files.
     * 
     * @param filePath Path to the JSON file
     * @return A single SalesRecord object
     * @throws IOException If file reading or JSON parsing fails
     */
    public SalesRecord ingestSingleRecordFromJson(String filePath) throws IOException {
        logger.info("🚀 Reading single JSON record from file: {}", filePath);
        
        File jsonFile = new File(filePath);
        if (!jsonFile.exists()) {
            throw new IOException("JSON file not found: " + filePath);
        }
        
        try {
            SalesRecord record = objectMapper.readValue(jsonFile, SalesRecord.class);
            
            if (record.isValid()) {
                logger.info("✅ Valid record loaded: {}", record.getTransactionId());
            } else {
                logger.warn("❌ Invalid record loaded: {}", record.getValidationErrors());
            }
            
            return record;
            
        } catch (IOException e) {
            logger.error("❌ Error reading JSON file: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Writes sales records to a JSON file.
     * This demonstrates data export capabilities.
     * 
     * @param records List of SalesRecord objects to write
     * @param filePath Output file path
     * @throws IOException If file writing fails
     */
    public void exportToJson(List<SalesRecord> records, String filePath) throws IOException {
        logger.info("📤 Exporting {} records to JSON file: {}", records.size(), filePath);
        
        try {
            objectMapper.writerWithDefaultPrettyPrinter()
                       .writeValue(new File(filePath), records);
            
            logger.info("✅ JSON export completed successfully!");
            
        } catch (IOException e) {
            logger.error("❌ Error writing JSON file: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Creates a sample JSON file for testing purposes.
     * This method demonstrates how to generate test data in JSON format.
     * 
     * @param filePath Path where to create the sample JSON file
     * @throws IOException If file creation fails
     */
    public void createSampleJsonFile(String filePath) throws IOException {
        logger.info("📝 Creating sample JSON file at: {}", filePath);
        
        // Create sample data
        List<SalesRecord> sampleRecords = new ArrayList<>();
        
        sampleRecords.add(new SalesRecord(
            "TXN001", "CUST001", "Laptop Pro", "Electronics", 
            1, new BigDecimal("1299.99"), 
            LocalDateTime.of(2023, 10, 1, 10, 30), 
            "New York", "John Smith"
        ));
        
        sampleRecords.add(new SalesRecord(
            "TXN002", "CUST002", "Coffee Mug", "Home & Garden", 
            2, new BigDecimal("15.50"), 
            LocalDateTime.of(2023, 10, 1, 11, 15), 
            "Los Angeles", "Jane Doe"
        ));
        
        sampleRecords.add(new SalesRecord(
            "TXN003", "CUST003", "Running Shoes", "Sports", 
            1, new BigDecimal("89.99"), 
            LocalDateTime.of(2023, 10, 1, 14, 20), 
            "Chicago", "Mike Johnson"
        ));
        
        sampleRecords.add(new SalesRecord(
            "TXN004", "CUST001", "Wireless Mouse", "Electronics", 
            3, new BigDecimal("25.99"), 
            LocalDateTime.of(2023, 10, 1, 16, 45), 
            "New York", "John Smith"
        ));
        
        try {
            exportToJson(sampleRecords, filePath);
            logger.info("✅ Sample JSON file created successfully!");
        } catch (IOException e) {
            logger.error("❌ Error creating sample JSON file: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Processes JSON data from a string.
     * Useful for processing JSON data received from APIs or message queues.
     * 
     * @param jsonString JSON string containing sales records
     * @return List of SalesRecord objects
     * @throws IOException If JSON parsing fails
     */
    public List<SalesRecord> processJsonString(String jsonString) throws IOException {
        logger.info("🔄 Processing JSON string data...");
        
        try {
            List<SalesRecord> records = objectMapper.readValue(
                jsonString, 
                new TypeReference<List<SalesRecord>>() {}
            );
            
            logger.info("✅ Processed {} records from JSON string", records.size());
            return records;
            
        } catch (IOException e) {
            logger.error("❌ Error processing JSON string: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Validates and pretty-prints JSON content.
     * Useful for debugging and data quality checks.
     * 
     * @param filePath Path to the JSON file to validate
     * @return true if JSON is valid, false otherwise
     */
    public boolean validateJsonFile(String filePath) {
        logger.info("🔍 Validating JSON file: {}", filePath);
        
        try {
            String content = Files.readString(Paths.get(filePath));
            objectMapper.readTree(content);
            logger.info("✅ JSON file is valid");
            return true;
        } catch (Exception e) {
            logger.error("❌ JSON validation failed: {}", e.getMessage());
            return false;
        }
    }
} 