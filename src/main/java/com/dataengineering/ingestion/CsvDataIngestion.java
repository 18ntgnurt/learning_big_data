package com.dataengineering.ingestion;

import com.dataengineering.model.SalesRecord;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * CsvDataIngestion demonstrates how to ingest data from CSV files.
 * This is one of the most common data ingestion patterns in data engineering.
 * 
 * Key Learning Points:
 * - File handling and parsing
 * - Error handling during data ingestion
 * - Data validation during ingestion
 * - Batch processing concepts
 * - Logging and monitoring
 */
public class CsvDataIngestion {
    
    private static final Logger logger = LoggerFactory.getLogger(CsvDataIngestion.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Reads sales records from a CSV file.
     * 
     * @param filePath Path to the CSV file
     * @return List of SalesRecord objects
     * @throws IOException If file reading fails
     */
    public List<SalesRecord> ingestFromCsv(String filePath) throws IOException {
        logger.info("🚀 Starting CSV ingestion from file: {}", filePath);
        
        List<SalesRecord> records = new ArrayList<>();
        Path path = Paths.get(filePath);
        
        if (!Files.exists(path)) {
            throw new FileNotFoundException("CSV file not found: " + filePath);
        }
        
        try (Reader reader = Files.newBufferedReader(path);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withIgnoreHeaderCase()
                     .withTrim())) {
            
            long recordCount = 0;
            long validRecords = 0;
            long invalidRecords = 0;
            
            for (CSVRecord csvRecord : csvParser) {
                recordCount++;
                try {
                    SalesRecord salesRecord = parseRecord(csvRecord);
                    
                    if (salesRecord.isValid()) {
                        records.add(salesRecord);
                        validRecords++;
                    } else {
                        invalidRecords++;
                        logger.warn("❌ Invalid record at line {}: {}", 
                                  recordCount, salesRecord.getValidationErrors());
                    }
                    
                    // Log progress for large files
                    if (recordCount % 1000 == 0) {
                        logger.info("📊 Processed {} records so far...", recordCount);
                    }
                    
                } catch (Exception e) {
                    invalidRecords++;
                    logger.error("❌ Error parsing record at line {}: {}", recordCount, e.getMessage());
                }
            }
            
            logger.info("✅ CSV ingestion completed!");
            logger.info("📈 Total records processed: {}", recordCount);
            logger.info("✅ Valid records: {}", validRecords);
            logger.info("❌ Invalid records: {}", invalidRecords);
            
        } catch (IOException e) {
            logger.error("❌ Error reading CSV file: {}", e.getMessage());
            throw e;
        }
        
        return records;
    }
    
    /**
     * Parses a single CSV record into a SalesRecord object.
     * This method demonstrates data transformation during ingestion.
     * 
     * @param csvRecord The CSV record to parse
     * @return A SalesRecord object
     */
    private SalesRecord parseRecord(CSVRecord csvRecord) {
        SalesRecord record = new SalesRecord();
        
        // Parse each field with proper error handling
        record.setTransactionId(getStringValue(csvRecord, "transaction_id"));
        record.setCustomerId(getStringValue(csvRecord, "customer_id"));
        record.setProductName(getStringValue(csvRecord, "product_name"));
        record.setProductCategory(getStringValue(csvRecord, "product_category"));
        record.setQuantity(getIntegerValue(csvRecord, "quantity"));
        record.setUnitPrice(getBigDecimalValue(csvRecord, "unit_price"));
        record.setSaleDate(getDateTimeValue(csvRecord, "sale_date"));
        record.setStoreLocation(getStringValue(csvRecord, "store_location"));
        record.setSalesPerson(getStringValue(csvRecord, "sales_person"));
        
        return record;
    }
    
    /**
     * Safely extracts a string value from CSV record.
     */
    private String getStringValue(CSVRecord record, String column) {
        try {
            String value = record.get(column);
            return (value != null && !value.trim().isEmpty()) ? value.trim() : null;
        } catch (IllegalArgumentException e) {
            logger.warn("⚠️ Column '{}' not found in CSV record", column);
            return null;
        }
    }
    
    /**
     * Safely extracts an integer value from CSV record.
     */
    private Integer getIntegerValue(CSVRecord record, String column) {
        try {
            String value = getStringValue(record, column);
            return (value != null) ? Integer.parseInt(value) : null;
        } catch (NumberFormatException e) {
            logger.warn("⚠️ Invalid integer value for column '{}': {}", column, record.get(column));
            return null;
        }
    }
    
    /**
     * Safely extracts a BigDecimal value from CSV record.
     */
    private BigDecimal getBigDecimalValue(CSVRecord record, String column) {
        try {
            String value = getStringValue(record, column);
            return (value != null) ? new BigDecimal(value) : null;
        } catch (NumberFormatException e) {
            logger.warn("⚠️ Invalid decimal value for column '{}': {}", column, record.get(column));
            return null;
        }
    }
    
    /**
     * Safely extracts a LocalDateTime value from CSV record.
     */
    private LocalDateTime getDateTimeValue(CSVRecord record, String column) {
        try {
            String value = getStringValue(record, column);
            return (value != null) ? LocalDateTime.parse(value, DATE_FORMATTER) : null;
        } catch (Exception e) {
            logger.warn("⚠️ Invalid date value for column '{}': {}", column, record.get(column));
            return null;
        }
    }
    
    /**
     * Creates a sample CSV file for testing purposes.
     * This method demonstrates how to generate test data for data engineering projects.
     * 
     * @param filePath Path where to create the sample CSV file
     * @throws IOException If file creation fails
     */
    public void createSampleCsvFile(String filePath) throws IOException {
        logger.info("📝 Creating sample CSV file at: {}", filePath);
        
        String csvContent = 
            "transaction_id,customer_id,product_name,product_category,quantity,unit_price,sale_date,store_location,sales_person\n" +
            "TXN001,CUST001,Laptop Pro,Electronics,1,1299.99,2023-10-01 10:30:00,New York,John Smith\n" +
            "TXN002,CUST002,Coffee Mug,Home & Garden,2,15.50,2023-10-01 11:15:00,Los Angeles,Jane Doe\n" +
            "TXN003,CUST003,Running Shoes,Sports,1,89.99,2023-10-01 14:20:00,Chicago,Mike Johnson\n" +
            "TXN004,CUST001,Wireless Mouse,Electronics,3,25.99,2023-10-01 16:45:00,New York,John Smith\n" +
            "TXN005,CUST004,Book: Data Engineering,Books,1,45.00,2023-10-02 09:30:00,Seattle,Sarah Wilson\n" +
            "TXN006,CUST005,Smartphone Case,Electronics,2,12.99,2023-10-02 12:00:00,Miami,Carlos Rodriguez\n" +
            "TXN007,CUST002,Garden Hose,Home & Garden,1,35.75,2023-10-02 15:30:00,Los Angeles,Jane Doe\n" +
            "TXN008,CUST006,Tennis Racket,Sports,1,125.00,2023-10-03 11:00:00,Denver,Lisa Brown\n";
        
        try {
            Files.write(Paths.get(filePath), csvContent.getBytes());
            logger.info("✅ Sample CSV file created successfully!");
        } catch (IOException e) {
            logger.error("❌ Error creating sample CSV file: {}", e.getMessage());
            throw e;
        }
    }
} 