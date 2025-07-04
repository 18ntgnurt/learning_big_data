package com.dataengineering.etl;

import com.dataengineering.model.Transaction;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Unified Data Ingestion Service
 * 
 * Consolidates CSV and JSON ingestion with:
 * - Automatic format detection
 * - Data validation and cleansing
 * - Real-time metrics collection
 * - Kafka streaming integration
 * - Error handling and recovery
 */
@Service
public class DataIngestionService {
    
    private static final Logger logger = LoggerFactory.getLogger(DataIngestionService.class);
    
    private final ObjectMapper objectMapper;
    private final CsvMapper csvMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    // Metrics
    private final AtomicLong totalProcessed = new AtomicLong(0);
    private final AtomicLong totalErrors = new AtomicLong(0);
    private final AtomicLong csvRecordsProcessed = new AtomicLong(0);
    private final AtomicLong jsonRecordsProcessed = new AtomicLong(0);
    
    // Configuration
    private static final String TRANSACTIONS_TOPIC = "transactions-raw-v1";
    private static final int BATCH_SIZE = 1000;
    
    public DataIngestionService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules();
        this.csvMapper = new CsvMapper();
        
        logger.info("Data Ingestion Service initialized");
    }
    
    /**
     * Ingest data from file with automatic format detection
     */
    public CompletableFuture<IngestionResult> ingestFromFile(String filePath) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                File file = new File(filePath);
                if (!file.exists()) {
                    throw new IllegalArgumentException("File not found: " + filePath);
                }
                
                // Detect format
                DataFormat format = detectFormat(file);
                logger.info("Detected format: {} for file: {}", format, filePath);
                
                // Process based on format
                switch (format) {
                    case CSV:
                        return processCsvFile(file);
                    case JSON:
                        return processJsonFile(file);
                    case JSON_LINES:
                        return processJsonLinesFile(file);
                    default:
                        throw new UnsupportedOperationException("Unsupported format: " + format);
                }
                
            } catch (Exception e) {
                logger.error("Error ingesting file: {}", filePath, e);
                return IngestionResult.error(filePath, e.getMessage());
            }
        });
    }
    
    /**
     * Process CSV file with dynamic schema detection
     */
    private IngestionResult processCsvFile(File file) throws IOException {
        logger.info("Processing CSV file: {}", file.getName());
        
        long startTime = System.currentTimeMillis();
        List<Transaction> transactions = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        // Build CSV schema from header
        CsvSchema schema = CsvSchema.emptySchema().withHeader();
        
        try {
            List<JsonNode> records = new ArrayList<>();
            try (MappingIterator<JsonNode> iterator = csvMapper.readerFor(JsonNode.class)
                    .with(schema)
                    .readValues(file)) {
                while (iterator.hasNext()) {
                    records.add(iterator.next());
                }
            }
            
            for (JsonNode record : records) {
                try {
                    Transaction transaction = convertToTransaction(record, DataFormat.CSV);
                    if (validateTransaction(transaction)) {
                        transactions.add(transaction);
                        
                        // Stream to Kafka in batches
                        if (transactions.size() >= BATCH_SIZE) {
                            streamBatch(transactions);
                            transactions.clear();
                        }
                    }
                } catch (Exception e) {
                    errors.add("Row error: " + e.getMessage());
                    totalErrors.incrementAndGet();
                }
            }
            
            // Process remaining transactions
            if (!transactions.isEmpty()) {
                streamBatch(transactions);
            }
            
            csvRecordsProcessed.addAndGet(records.size());
            
        } catch (Exception e) {
            logger.error("Error processing CSV file: {}", file.getName(), e);
            throw e;
        }
        
        long processingTime = System.currentTimeMillis() - startTime;
        return IngestionResult.success(file.getAbsolutePath(), DataFormat.CSV, 
                                     transactions.size(), errors, processingTime);
    }
    
    /**
     * Process JSON file (single object or array)
     */
    private IngestionResult processJsonFile(File file) throws IOException {
        logger.info("Processing JSON file: {}", file.getName());
        
        long startTime = System.currentTimeMillis();
        List<Transaction> transactions = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        try {
            JsonNode rootNode = objectMapper.readTree(file);
            
            if (rootNode.isArray()) {
                // Array of transactions
                for (JsonNode node : rootNode) {
                    processJsonNode(node, transactions, errors);
                }
            } else {
                // Single transaction
                processJsonNode(rootNode, transactions, errors);
            }
            
            // Stream all transactions
            streamBatch(transactions);
            jsonRecordsProcessed.addAndGet(transactions.size());
            
        } catch (Exception e) {
            logger.error("Error processing JSON file: {}", file.getName(), e);
            throw e;
        }
        
        long processingTime = System.currentTimeMillis() - startTime;
        return IngestionResult.success(file.getAbsolutePath(), DataFormat.JSON, 
                                     transactions.size(), errors, processingTime);
    }
    
    /**
     * Process JSON Lines file (one JSON object per line)
     */
    private IngestionResult processJsonLinesFile(File file) throws IOException {
        logger.info("Processing JSON Lines file: {}", file.getName());
        
        long startTime = System.currentTimeMillis();
        List<Transaction> transactions = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        try {
            List<String> lines = java.nio.file.Files.readAllLines(file.toPath());
            
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                
                try {
                    JsonNode node = objectMapper.readTree(line);
                    processJsonNode(node, transactions, errors);
                    
                    // Stream in batches
                    if (transactions.size() >= BATCH_SIZE) {
                        streamBatch(transactions);
                        transactions.clear();
                    }
                } catch (Exception e) {
                    errors.add("Line error: " + e.getMessage());
                    totalErrors.incrementAndGet();
                }
            }
            
            // Process remaining transactions
            if (!transactions.isEmpty()) {
                streamBatch(transactions);
            }
            
            jsonRecordsProcessed.addAndGet(lines.size());
            
        } catch (Exception e) {
            logger.error("Error processing JSON Lines file: {}", file.getName(), e);
            throw e;
        }
        
        long processingTime = System.currentTimeMillis() - startTime;
        return IngestionResult.success(file.getAbsolutePath(), DataFormat.JSON_LINES, 
                                     transactions.size(), errors, processingTime);
    }
    
    /**
     * Process individual JSON node
     */
    private void processJsonNode(JsonNode node, List<Transaction> transactions, List<String> errors) {
        try {
            Transaction transaction = convertToTransaction(node, DataFormat.JSON);
            if (validateTransaction(transaction)) {
                transactions.add(transaction);
            }
        } catch (Exception e) {
            errors.add("JSON node error: " + e.getMessage());
            totalErrors.incrementAndGet();
        }
    }
    
    /**
     * Convert JsonNode to Transaction object
     */
    private Transaction convertToTransaction(JsonNode node, DataFormat format) {
        Transaction.Builder builder = Transaction.builder();
        
        // Map fields with flexible naming
        builder.transactionId(getStringValue(node, "transaction_id", "transactionId", "id"))
               .customerId(getStringValue(node, "customer_id", "customerId", "customer"))
               .merchantId(getStringValue(node, "merchant_id", "merchantId", "merchant"))
               .amount(getBigDecimalValue(node, "amount", "total_amount", "totalAmount"))
               .description(getStringValue(node, "description", "product_name", "productName"))
               .category(getStringValue(node, "category", "product_category", "productCategory"))
               .location(getStringValue(node, "location", "store_location", "storeLocation"))
               .transactionDate(getDateTimeValue(node, "transaction_date", "transactionDate", "timestamp"))
               .metadata(node.toString()); // Store original for debugging
        
        return builder.build();
    }
    
    /**
     * Validate transaction data
     */
    private boolean validateTransaction(Transaction transaction) {
        if (transaction.getTransactionId() == null || transaction.getTransactionId().trim().isEmpty()) {
            logger.warn("Invalid transaction: missing transaction ID");
            return false;
        }
        
        if (transaction.getAmount() == null || transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            logger.warn("Invalid transaction: invalid amount for {}", transaction.getTransactionId());
            return false;
        }
        
        if (transaction.getCustomerId() == null || transaction.getCustomerId().trim().isEmpty()) {
            logger.warn("Invalid transaction: missing customer ID for {}", transaction.getTransactionId());
            return false;
        }
        
        return true;
    }
    
    /**
     * Stream batch of transactions to Kafka
     */
    private void streamBatch(List<Transaction> transactions) {
        for (Transaction transaction : transactions) {
            kafkaTemplate.send(TRANSACTIONS_TOPIC, transaction.getTransactionId(), transaction)
                    .whenComplete((result, failure) -> {
                        if (failure != null) {
                            logger.error("Failed to send transaction: {}", 
                                       transaction.getTransactionId(), failure);
                            totalErrors.incrementAndGet();
                        } else {
                            totalProcessed.incrementAndGet();
                        }
                    });
        }
        
        logger.debug("Streamed batch of {} transactions", transactions.size());
    }
    
    /**
     * Detect file format based on content and extension
     */
    private DataFormat detectFormat(File file) throws IOException {
        String fileName = file.getName().toLowerCase();
        
        if (fileName.endsWith(".csv")) {
            return DataFormat.CSV;
        }
        
        if (fileName.endsWith(".json")) {
            // Check if it's JSON Lines
            List<String> lines = java.nio.file.Files.readAllLines(file.toPath());
            if (lines.size() > 1 && lines.stream().allMatch(this::isValidJsonLine)) {
                return DataFormat.JSON_LINES;
            }
            return DataFormat.JSON;
        }
        
        if (fileName.endsWith(".jsonl") || fileName.endsWith(".ndjson")) {
            return DataFormat.JSON_LINES;
        }
        
        // Try to detect from content
        String firstLine = java.nio.file.Files.readAllLines(file.toPath()).get(0);
        if (firstLine.startsWith("{") || firstLine.startsWith("[")) {
            return DataFormat.JSON;
        }
        
        // Default to CSV
        return DataFormat.CSV;
    }
    
    /**
     * Check if line is valid JSON
     */
    private boolean isValidJsonLine(String line) {
        try {
            objectMapper.readTree(line);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    // Helper methods for flexible field extraction
    private String getStringValue(JsonNode node, String... fieldNames) {
        for (String fieldName : fieldNames) {
            JsonNode field = node.get(fieldName);
            if (field != null && !field.isNull()) {
                return field.asText();
            }
        }
        return null;
    }
    
    private BigDecimal getBigDecimalValue(JsonNode node, String... fieldNames) {
        for (String fieldName : fieldNames) {
            JsonNode field = node.get(fieldName);
            if (field != null && !field.isNull()) {
                return new BigDecimal(field.asText());
            }
        }
        return null;
    }
    
    private LocalDateTime getDateTimeValue(JsonNode node, String... fieldNames) {
        for (String fieldName : fieldNames) {
            JsonNode field = node.get(fieldName);
            if (field != null && !field.isNull()) {
                try {
                    // Try different date formats
                    String dateStr = field.asText();
                    return LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                } catch (Exception e) {
                    // Try other formats or use current time
                    logger.warn("Could not parse date: {}", field.asText());
                }
            }
        }
        return LocalDateTime.now();
    }
    
    /**
     * Get ingestion metrics
     */
    public IngestionMetrics getMetrics() {
        return new IngestionMetrics(
                totalProcessed.get(),
                totalErrors.get(),
                csvRecordsProcessed.get(),
                jsonRecordsProcessed.get()
        );
    }
    
    /**
     * Reset metrics
     */
    public void resetMetrics() {
        totalProcessed.set(0);
        totalErrors.set(0);
        csvRecordsProcessed.set(0);
        jsonRecordsProcessed.set(0);
    }
    
    // Supporting classes
    public enum DataFormat {
        CSV, JSON, JSON_LINES
    }
    
    public static class IngestionResult {
        private final String filePath;
        private final DataFormat format;
        private final boolean success;
        private final int recordsProcessed;
        private final List<String> errors;
        private final long processingTimeMs;
        private final String errorMessage;
        
        private IngestionResult(String filePath, DataFormat format, boolean success,
                              int recordsProcessed, List<String> errors, 
                              long processingTimeMs, String errorMessage) {
            this.filePath = filePath;
            this.format = format;
            this.success = success;
            this.recordsProcessed = recordsProcessed;
            this.errors = errors != null ? errors : new ArrayList<>();
            this.processingTimeMs = processingTimeMs;
            this.errorMessage = errorMessage;
        }
        
        public static IngestionResult success(String filePath, DataFormat format,
                                            int recordsProcessed, List<String> errors, 
                                            long processingTimeMs) {
            return new IngestionResult(filePath, format, true, recordsProcessed, 
                                     errors, processingTimeMs, null);
        }
        
        public static IngestionResult error(String filePath, String errorMessage) {
            return new IngestionResult(filePath, null, false, 0, 
                                     new ArrayList<>(), 0, errorMessage);
        }
        
        // Getters
        public String getFilePath() { return filePath; }
        public DataFormat getFormat() { return format; }
        public boolean isSuccess() { return success; }
        public int getRecordsProcessed() { return recordsProcessed; }
        public List<String> getErrors() { return errors; }
        public long getProcessingTimeMs() { return processingTimeMs; }
        public String getErrorMessage() { return errorMessage; }
    }
    
    public static class IngestionMetrics {
        private final long totalProcessed;
        private final long totalErrors;
        private final long csvRecordsProcessed;
        private final long jsonRecordsProcessed;
        
        public IngestionMetrics(long totalProcessed, long totalErrors,
                              long csvRecordsProcessed, long jsonRecordsProcessed) {
            this.totalProcessed = totalProcessed;
            this.totalErrors = totalErrors;
            this.csvRecordsProcessed = csvRecordsProcessed;
            this.jsonRecordsProcessed = jsonRecordsProcessed;
        }
        
        // Getters
        public long getTotalProcessed() { return totalProcessed; }
        public long getTotalErrors() { return totalErrors; }
        public long getCsvRecordsProcessed() { return csvRecordsProcessed; }
        public long getJsonRecordsProcessed() { return jsonRecordsProcessed; }
        public double getErrorRate() { 
            return totalProcessed > 0 ? (double) totalErrors / totalProcessed : 0.0; 
        }
    }
} 