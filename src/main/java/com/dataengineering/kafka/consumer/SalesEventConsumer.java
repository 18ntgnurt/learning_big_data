package com.dataengineering.kafka.consumer;

import com.dataengineering.kafka.config.KafkaConfig;
import com.dataengineering.model.SalesRecord;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * SalesEventConsumer processes sales events from Kafka topics.
 * 
 * Key Learning Points:
 * - Consumer group management
 * - Offset management and commits
 * - Message deserialization
 * - Error handling and retry logic
 * - Graceful shutdown patterns
 * 
 * TODO: Implement the following methods:
 * 1. Constructor - Initialize Kafka consumer
 * 2. startConsuming() - Main consumption loop
 * 3. processRecord() - Process individual messages
 * 4. commitOffsets() - Manual offset management
 * 5. stop() - Graceful shutdown
 */
public class SalesEventConsumer {
    
    private static final Logger logger = LoggerFactory.getLogger(SalesEventConsumer.class);
    
    // Declare consumer instance
    private KafkaConsumer<String, String> consumer;
    
    // Declare JSON object mapper for deserialization
    private final ObjectMapper objectMapper;
    
    // Declare atomic boolean for shutdown control
    private final AtomicBoolean running = new AtomicBoolean(true);
    
    // Consumer configuration
    private static final String CONSUMER_GROUP_ID = "sales-processor-group";
    private static final int POLL_TIMEOUT_MS = 1000;
    
    /**
     * Constructor - Initialize Kafka consumer
     */
    public SalesEventConsumer() {
        // Initialize ObjectMapper
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules(); // For LocalDateTime support
        this.objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        // Get consumer configuration
        Properties props = KafkaConfig.getConsumerProperties(CONSUMER_GROUP_ID);
        this.consumer = new KafkaConsumer<>(props);
        
        // Subscribe to topics
        consumer.subscribe(Arrays.asList(KafkaConfig.SALES_EVENTS_TOPIC));
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
        
        logger.info("✅ SalesEventConsumer initialized for group: {}", CONSUMER_GROUP_ID);
        System.out.println("✅ SalesEventConsumer initialized for group: " + CONSUMER_GROUP_ID);
    }
    
    /**
     * Main consumption loop
     */
    public void startConsuming() {
        logger.info("🚀 Starting to consume sales events...");
        System.out.println("🚀 Starting to consume sales events...");
        
        try {
            // Main consumption loop
            while (running.get()) {
                try {
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(POLL_TIMEOUT_MS));
                    
                    if (!records.isEmpty()) {
                        logger.info("📥 Received {} records", records.count());
                        System.out.println("📥 Received " + records.count() + " records");
                        
                        for (ConsumerRecord<String, String> record : records) {
                            processRecord(record);
                        }
                        
                        // Manual commit for reliability
                        commitOffsets();
                    }
                } catch (Exception e) {
                    logger.error("❌ Error during consumption", e);
                    System.err.println("❌ Error during consumption: " + e.getMessage());
                    Thread.sleep(5000); // Back off on error
                }
            }
            
        } catch (Exception e) {
            logger.error("❌ Fatal error in consumer", e);
            System.err.println("❌ Fatal error in consumer: " + e.getMessage());
        } finally {
            // Close consumer
            close();
        }
    }
    
    /**
     * Process individual records
     */
    private void processRecord(ConsumerRecord<String, String> record) {
        try {
            // Log record details
            logger.info("🔄 Processing record: key={}, partition={}, offset={}", 
                       record.key(), record.partition(), record.offset());
            System.out.println("🔄 Processing record: key=" + record.key() + 
                             ", partition=" + record.partition() + ", offset=" + record.offset());
            
            // Deserialize JSON to SalesRecord
            SalesRecord salesRecord = objectMapper.readValue(record.value(), SalesRecord.class);
            
            // Validate sales record
            if (!salesRecord.isValid()) {
                logger.warn("⚠️ Invalid sales record: {}", record.key());
                System.out.println("⚠️ Invalid sales record: " + record.key());
                return;
            }
            
            // Process the record
            processSalesRecord(salesRecord);
            
            // Log successful processing
            logger.info("✅ Successfully processed sales record: {}", salesRecord.getTransactionId());
            System.out.println("✅ Successfully processed sales record: " + salesRecord.getTransactionId());
            
        } catch (Exception e) {
            // Error handling - log and continue for now
            logger.error("❌ Failed to process record: key={}, partition={}, offset={}", 
                        record.key(), record.partition(), record.offset(), e);
            System.err.println("❌ Failed to process record: key=" + record.key() + 
                             ", partition=" + record.partition() + ", offset=" + record.offset() + 
                             ", error=" + e.getMessage());
            
            // TODO: Implement error handling strategy
            // - Send to dead letter queue?
            // - Retry with exponential backoff?
            // - Skip and continue?
        }
    }
    
    /**
     * Business logic for processing sales records
     */
    private void processSalesRecord(SalesRecord salesRecord) {
        // Example business logic implementations:
        
        // 1. Log the sales record details
        System.out.println("💼 Processing: " + salesRecord.toString());
        
        // 2. Example analytics
        if (salesRecord.getTotalAmount().doubleValue() > 1000) {
            System.out.println("🚨 HIGH VALUE SALE: $" + salesRecord.getTotalAmount());
            logger.info("🚨 High value sale detected: ${}", salesRecord.getTotalAmount());
        }
        
        // 3. Example category analysis
        System.out.println("📊 Category: " + salesRecord.getProductCategory() + 
                         " | Location: " + salesRecord.getStoreLocation());
        
        // TODO: Add your business logic here:
        // - Save to database
        // - Update analytics
        // - Send notifications
        // - Update inventory
        
        logger.info("💼 Business logic processed for: {}", salesRecord.getTransactionId());
    }
    
    /**
     * Manual offset commit
     */
    private void commitOffsets() {
        try {
            // Commit offsets synchronously
            consumer.commitSync();
            logger.debug("✅ Offsets committed successfully");
            
        } catch (Exception e) {
            logger.error("❌ Failed to commit offsets", e);
            System.err.println("❌ Failed to commit offsets: " + e.getMessage());
            // TODO: Decide on error handling strategy
        }
    }
    
    /**
     * Graceful shutdown
     */
    public void stop() {
        logger.info("🛑 Stopping sales event consumer...");
        System.out.println("🛑 Stopping sales event consumer...");
        
        // Set running flag to false
        running.set(false);
        
        // Close consumer
        close();
    }
    
    /**
     * Consumer cleanup
     */
    private void close() {
        // Close consumer
        if (consumer != null) {
            try {
                consumer.close(Duration.ofSeconds(10));
                logger.info("✅ Consumer closed successfully");
                System.out.println("✅ Consumer closed successfully");
            } catch (Exception e) {
                logger.error("❌ Error closing consumer", e);
                System.err.println("❌ Error closing consumer: " + e.getMessage());
            }
        }
    }
    
    /**
     * Helper method for running consumer in separate thread
     */
    public Thread startInBackground() {
        Thread consumerThread = new Thread(this::startConsuming, "sales-event-consumer");
        consumerThread.setDaemon(false); // Keep JVM alive
        consumerThread.start();
        
        logger.info("🔄 Consumer started in background thread");
        System.out.println("🔄 Consumer started in background thread");
        return consumerThread;
    }
} 