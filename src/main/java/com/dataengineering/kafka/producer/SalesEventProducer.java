package com.dataengineering.kafka.producer;

import com.dataengineering.kafka.config.KafkaConfig;
import com.dataengineering.model.SalesRecord;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.Future;

/**
 * SalesEventProducer sends sales records to Kafka topics.
 * 
 * Key Learning Points:
 * - Asynchronous message sending
 * - Error handling and retries
 * - Message serialization (JSON)
 * - Producer lifecycle management
 * - Performance optimization patterns
 * 
 * TODO: Implement the following methods:
 * 1. Constructor - Initialize Kafka producer
 * 2. sendSalesEvent() - Send single sales record
 * 3. sendSalesEventAsync() - Send with callback
 * 4. sendBatch() - Send multiple records efficiently
 * 5. close() - Clean shutdown
 */
public class SalesEventProducer {
    
    private static final Logger logger = LoggerFactory.getLogger(SalesEventProducer.class);
    
    // TODO: Declare Kafka producer instance
    // private KafkaProducer<String, String> producer;
    private KafkaProducer<String, String> producer;

    // TODO: Declare JSON object mapper for serialization
    // private final ObjectMapper objectMapper;
    private final ObjectMapper objectMapper;
    
    /**
     * TODO: Implement constructor
     * 
     * Steps to implement:
     * 1. Initialize ObjectMapper for JSON serialization
     * 2. Get producer properties from KafkaConfig
     * 3. Create KafkaProducer instance
     * 4. Add shutdown hook for graceful cleanup
     * 
     * Example:
     * this.objectMapper = new ObjectMapper();
     * Properties props = KafkaConfig.getProducerProperties();
     * this.producer = new KafkaProducer<>(props);
     */
    public SalesEventProducer() {
        // TODO: Initialize ObjectMapper
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules(); // For LocalDateTime support
        // TODO: Get producer configuration
        Properties props = KafkaConfig.getProducerProperties();
        this.producer = new KafkaProducer<>(props);
        // TODO: Create KafkaProducer instance
        Runtime.getRuntime().addShutdownHook(new Thread(this::close));
        
        logger.info("✅ SalesEventProducer initialized");
    }
    
    /**
     * TODO: Implement synchronous send method
     * 
     * Steps to implement:
     * 1. Convert SalesRecord to JSON string
     * 2. Create ProducerRecord with topic, key, and value
     * 3. Send message and wait for result
     * 4. Handle any exceptions
     * 5. Log success/failure
     * 
     * @param salesRecord The sales record to send
     * @return true if sent successfully, false otherwise
     */
    public boolean sendSalesEvent(SalesRecord salesRecord) {
        try {
            // TODO: Convert SalesRecord to JSON
            // String jsonValue = objectMapper.writeValueAsString(salesRecord);
            String jsonValue = objectMapper.writeValueAsString(salesRecord);
            
            // TODO: Create producer record
            // Use transaction ID as key for partitioning
            ProducerRecord<String, String> record = new ProducerRecord<>(
                KafkaConfig.SALES_EVENTS_TOPIC,
                salesRecord.getTransactionId(),
                jsonValue
            );
            
            // TODO: Send message synchronously
            Future<RecordMetadata> future = producer.send(record);
            RecordMetadata metadata = future.get(); // Wait for completion
            
            // TODO: Log success
            logger.info("✅ Sent sales event: {} to partition: {}, offset: {}", 
                       salesRecord.getTransactionId(), metadata.partition(), metadata.offset());
            
            return true;
            
        } catch (Exception e) {
            // TODO: Log error
            logger.error("❌ Failed to send sales event: {}", salesRecord.getTransactionId(), e);
            return false;
        }
    }
    
    /**
     * TODO: Implement asynchronous send method with callback
     * 
     * Steps to implement:
     * 1. Convert SalesRecord to JSON string
     * 2. Create ProducerRecord
     * 3. Send with callback for handling success/failure
     * 4. Don't wait for completion (async)
     * 
     * @param salesRecord The sales record to send
     */
    public void sendSalesEventAsync(SalesRecord salesRecord) {
        try {
            // TODO: Convert SalesRecord to JSON
            // String jsonValue = objectMapper.writeValueAsString(salesRecord);
            String jsonValue = objectMapper.writeValueAsString(salesRecord);
            
            // TODO: Create producer record
            ProducerRecord<String, String> record = new ProducerRecord<>(
                KafkaConfig.SALES_EVENTS_TOPIC,
                salesRecord.getTransactionId(),
                jsonValue
            );
            
            // TODO: Send with callback
            producer.send(record, (metadata, exception) -> {
                if (exception == null) {
                    logger.info("✅ Async sent: {} to partition: {}, offset: {}", 
                               salesRecord.getTransactionId(), metadata.partition(), metadata.offset());
                } else {
                    logger.error("❌ Async send failed: {}", salesRecord.getTransactionId(), exception);
                }
            });
            
        } catch (Exception e) {
            logger.error("❌ Failed to prepare async send: {}", salesRecord.getTransactionId(), e);
        }
    }
    
    /**
     * TODO: Implement batch sending method
     * 
     * Steps to implement:
     * 1. Iterate through all sales records
     * 2. Send each record asynchronously
     * 3. Use producer.flush() to ensure all messages are sent
     * 4. Return count of successfully sent messages
     * 
     * @param salesRecords List of sales records to send
     * @return Number of successfully sent records
     */
    public int sendBatch(java.util.List<SalesRecord> salesRecords) {
        int successCount = 0;
        
        logger.info("📦 Sending batch of {} sales events", salesRecords.size());
        
        // TODO: Send all records asynchronously
        for (SalesRecord record : salesRecords) {
            sendSalesEventAsync(record);
            successCount++; // In real implementation, track actual success
        }
        
        // TODO: Flush to ensure all messages are sent
        producer.flush();
        
        logger.info("✅ Batch send completed: {}/{} successful", successCount, salesRecords.size());
        return successCount;
    }
    
    /**
     * Flush any pending messages
     */
    public void flush() {
        if (producer != null) {
            producer.flush();
        }
    }

    /**
     * Close producer and release resources
     */
    public void close() {
        // TODO: Flush any pending messages
        producer.flush();
        
        // TODO: Close producer
        producer.close();
        
        logger.info("✅ SalesEventProducer closed");
    }
    
    /**
     * TODO: Implement helper method for JSON serialization
     * 
     * This method should:
     * 1. Take any object as input
     * 2. Convert it to JSON string using ObjectMapper
     * 3. Handle JsonProcessingException
     * 4. Return JSON string or null on error
     * 
     * @param object Object to serialize
     * @return JSON string or null if error
     */
    private String toJson(Object object) {
        try {
            // TODO: Convert object to JSON
            // return objectMapper.writeValueAsString(object);
            return null;
        } catch (Exception e) {
            logger.error("❌ JSON serialization failed", e);
            return null;
        }
    }
} 