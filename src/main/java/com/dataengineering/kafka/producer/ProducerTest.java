package com.dataengineering.kafka.producer;

import com.dataengineering.model.SalesRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * ProducerTest - Simple test class for testing Kafka producer functionality
 * 
 * This class demonstrates how to:
 * 1. Create test sales records
 * 2. Test synchronous sending
 * 3. Test asynchronous sending
 * 4. Test batch sending
 * 5. Monitor results
 */
public class ProducerTest {
    
    private static final Logger logger = LoggerFactory.getLogger(ProducerTest.class);
    private static final Random random = new Random();
    
    public static void main(String[] args) {
        logger.info("🧪 Starting Kafka Producer Test...");
        
        try {
            // Test 1: Single synchronous send
            testSynchronousSend();
            
            // Test 2: Single asynchronous send
            testAsynchronousSend();
            
            // Test 3: Batch send
            testBatchSend();
            
            logger.info("✅ All producer tests completed!");
            
        } catch (Exception e) {
            logger.error("❌ Producer test failed", e);
        }
    }
    
    /**
     * Test 1: Synchronous sending - wait for confirmation
     */
    private static void testSynchronousSend() {
        logger.info("🔄 Test 1: Synchronous Send");
        
        SalesEventProducer producer = new SalesEventProducer();
        
        try {
            // Create a test sales record
            SalesRecord testRecord = createTestSalesRecord("SYNC_TEST_001");
            
            // Send synchronously
            boolean success = producer.sendSalesEvent(testRecord);
            
            if (success) {
                logger.info("✅ Synchronous send successful!");
            } else {
                logger.error("❌ Synchronous send failed!");
            }
            
        } catch (Exception e) {
            logger.error("❌ Error in synchronous test", e);
        } finally {
            producer.close();
        }
        
        logger.info("📊 Synchronous test completed\n");
    }
    
    /**
     * Test 2: Asynchronous sending - fire and forget
     */
    private static void testAsynchronousSend() {
        logger.info("🔄 Test 2: Asynchronous Send");
        
        SalesEventProducer producer = new SalesEventProducer();
        
        try {
            // Create test sales records
            for (int i = 1; i <= 5; i++) {
                SalesRecord testRecord = createTestSalesRecord("ASYNC_TEST_" + String.format("%03d", i));
                producer.sendSalesEventAsync(testRecord);
                logger.info("📤 Sent async message {}/5", i);
            }
            
            // Wait a bit for async processing
            logger.info("⏳ Waiting for async processing...");
            Thread.sleep(3000);
            
        } catch (Exception e) {
            logger.error("❌ Error in asynchronous test", e);
        } finally {
            producer.close();
        }
        
        logger.info("📊 Asynchronous test completed\n");
    }
    
    /**
     * Test 3: Batch sending - send multiple records efficiently
     */
    private static void testBatchSend() {
        logger.info("🔄 Test 3: Batch Send");
        
        SalesEventProducer producer = new SalesEventProducer();
        
        try {
            // Create batch of test records
            List<SalesRecord> batchRecords = new ArrayList<>();
            for (int i = 1; i <= 10; i++) {
                SalesRecord record = createTestSalesRecord("BATCH_TEST_" + String.format("%03d", i));
                batchRecords.add(record);
            }
            
            // Send batch
            long startTime = System.currentTimeMillis();
            int successCount = producer.sendBatch(batchRecords);
            long endTime = System.currentTimeMillis();
            
            logger.info("📊 Batch Results:");
            logger.info("   - Records sent: {}/{}", successCount, batchRecords.size());
            logger.info("   - Time taken: {} ms", (endTime - startTime));
            logger.info("   - Throughput: {} records/second", 
                       (successCount * 1000.0) / (endTime - startTime));
            
        } catch (Exception e) {
            logger.error("❌ Error in batch test", e);
        } finally {
            producer.close();
        }
        
        logger.info("📊 Batch test completed\n");
    }
    
    /**
     * Helper method to create test sales records with realistic data
     */
    private static SalesRecord createTestSalesRecord(String transactionId) {
        String[] customers = {"CUST001", "CUST002", "CUST003", "CUST004", "CUST005"};
        String[] products = {"Laptop", "Mouse", "Keyboard", "Monitor", "Webcam"};
        String[] categories = {"Electronics", "Accessories", "Hardware", "Peripherals"};
        String[] locations = {"Store North", "Store South", "Store East", "Store West", "Store Central"};
        String[] salesPeople = {"John Doe", "Jane Smith", "Bob Johnson", "Alice Brown", "Charlie Wilson"};
        
        SalesRecord record = new SalesRecord();
        record.setTransactionId(transactionId);
        record.setCustomerId(customers[random.nextInt(customers.length)]);
        record.setProductName(products[random.nextInt(products.length)]);
        record.setProductCategory(categories[random.nextInt(categories.length)]);
        record.setQuantity(random.nextInt(5) + 1);
        record.setUnitPrice(BigDecimal.valueOf(random.nextDouble() * 1000 + 50)); // $50-$1050
        record.setStoreLocation(locations[random.nextInt(locations.length)]);
        record.setSalesPerson(salesPeople[random.nextInt(salesPeople.length)]);
        record.setSaleDate(LocalDateTime.now());
        
        return record;
    }
} 