package com.dataengineering.kafka;

import com.dataengineering.kafka.consumer.SalesEventConsumer;
import com.dataengineering.kafka.producer.SalesEventProducer;
import com.dataengineering.model.SalesRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;

/**
 * EndToEndTest - Comprehensive test showing producer and consumer working together
 * 
 * This test demonstrates:
 * 1. Starting a consumer in background
 * 2. Sending messages with producer
 * 3. Seeing real-time consumption
 * 4. Graceful shutdown
 */
public class EndToEndTest {
    
    private static final Logger logger = LoggerFactory.getLogger(EndToEndTest.class);
    private static final Random random = new Random();
    
    public static void main(String[] args) {
        System.out.println("🚀 Starting End-to-End Kafka Test...");
        System.out.println("=====================================");
        
        SalesEventConsumer consumer = null;
        SalesEventProducer producer = null;
        Thread consumerThread = null;
        
        try {
            // Step 1: Start Consumer
            System.out.println("\n📥 Step 1: Starting Consumer...");
            consumer = new SalesEventConsumer();
            consumerThread = consumer.startInBackground();
            
            // Give consumer time to initialize
            Thread.sleep(2000);
            
            // Step 2: Create Producer
            System.out.println("\n📤 Step 2: Creating Producer...");
            producer = new SalesEventProducer();
            
            // Step 3: Send Messages
            System.out.println("\n📦 Step 3: Sending Messages...");
            for (int i = 1; i <= 5; i++) {
                SalesRecord record = createTestSalesRecord("E2E_TEST_" + String.format("%03d", i));
                
                System.out.println("\n📤 Sending message " + i + "/5:");
                System.out.println("   Transaction: " + record.getTransactionId());
                System.out.println("   Product: " + record.getProductName());
                System.out.println("   Amount: $" + record.getTotalAmount());
                
                boolean success = producer.sendSalesEvent(record);
                
                if (success) {
                    System.out.println("   ✅ Sent successfully!");
                } else {
                    System.out.println("   ❌ Send failed!");
                }
                
                // Wait a bit between messages to see processing
                Thread.sleep(1000);
            }
            
            // Step 4: Wait for processing
            System.out.println("\n⏳ Step 4: Waiting for processing...");
            Thread.sleep(5000);
            
            // Step 5: Send a high-value transaction to test analytics
            System.out.println("\n💰 Step 5: Sending high-value transaction...");
            SalesRecord highValueRecord = createHighValueSalesRecord();
            producer.sendSalesEvent(highValueRecord);
            
            // Wait to see the high-value processing
            Thread.sleep(3000);
            
            System.out.println("\n✅ All messages sent and processed!");
            
        } catch (Exception e) {
            System.err.println("❌ Test failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Cleanup
            System.out.println("\n🧹 Cleaning up...");
            
            if (producer != null) {
                producer.close();
                System.out.println("✅ Producer closed");
            }
            
            if (consumer != null) {
                consumer.stop();
                System.out.println("✅ Consumer stopped");
            }
            
            if (consumerThread != null) {
                try {
                    consumerThread.join(5000);
                    System.out.println("✅ Consumer thread terminated");
                } catch (InterruptedException e) {
                    System.err.println("⚠️ Consumer thread cleanup interrupted");
                }
            }
            
            System.out.println("\n🎉 End-to-End test completed!");
        }
    }
    
    /**
     * Helper method to create test sales records
     */
    private static SalesRecord createTestSalesRecord(String transactionId) {
        String[] customers = {"CUST001", "CUST002", "CUST003", "CUST004", "CUST005"};
        String[] products = {"Laptop", "Mouse", "Keyboard", "Monitor", "Webcam"};
        String[] categories = {"Electronics", "Accessories", "Hardware", "Peripherals"};
        String[] locations = {"Store North", "Store South", "Store East", "Store West"};
        String[] salesPeople = {"Alice", "Bob", "Charlie", "Diana", "Eve"};
        
        SalesRecord record = new SalesRecord();
        record.setTransactionId(transactionId);
        record.setCustomerId(customers[random.nextInt(customers.length)]);
        record.setProductName(products[random.nextInt(products.length)]);
        record.setProductCategory(categories[random.nextInt(categories.length)]);
        record.setQuantity(random.nextInt(3) + 1);
        record.setUnitPrice(BigDecimal.valueOf(random.nextDouble() * 500 + 100)); // $100-$600
        record.setStoreLocation(locations[random.nextInt(locations.length)]);
        record.setSalesPerson(salesPeople[random.nextInt(salesPeople.length)]);
        record.setSaleDate(LocalDateTime.now());
        
        return record;
    }
    
    /**
     * Helper method to create a high-value sales record for analytics testing
     */
    private static SalesRecord createHighValueSalesRecord() {
        SalesRecord record = new SalesRecord();
        record.setTransactionId("HIGH_VALUE_001");
        record.setCustomerId("VIP_CUSTOMER");
        record.setProductName("Enterprise Server");
        record.setProductCategory("Enterprise Hardware");
        record.setQuantity(2);
        record.setUnitPrice(BigDecimal.valueOf(5000.00)); // $5,000 each = $10,000 total
        record.setStoreLocation("Enterprise Sales");
        record.setSalesPerson("Senior Account Manager");
        record.setSaleDate(LocalDateTime.now());
        
        return record;
    }
} 