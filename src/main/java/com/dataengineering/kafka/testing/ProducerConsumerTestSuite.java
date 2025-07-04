package com.dataengineering.kafka.testing;

import com.dataengineering.kafka.consumer.SalesEventConsumer;
import com.dataengineering.kafka.producer.SalesEventProducer;
import com.dataengineering.model.SalesRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Scanner;

/**
 * ProducerConsumerTestSuite - Multiple ways to test Producer and Consumer together
 * 
 * This class demonstrates:
 * 1. Automated end-to-end testing
 * 2. Interactive testing
 * 3. Separate terminal testing
 * 4. Load testing
 */
public class ProducerConsumerTestSuite {
    
    private static final Logger logger = LoggerFactory.getLogger(ProducerConsumerTestSuite.class);
    
    public static void main(String[] args) {
        System.out.println("🧪 Producer-Consumer Test Suite");
        System.out.println("==============================");
        
        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "auto":
                    runAutomatedTest();
                    break;
                case "interactive":
                    runInteractiveTest();
                    break;
                case "consumer-only":
                    runConsumerOnly();
                    break;
                case "producer-only":
                    runProducerOnly();
                    break;
                case "load":
                    runLoadTest();
                    break;
                default:
                    showUsage();
            }
        } else {
            showUsage();
        }
    }
    
    /**
     * Show usage instructions
     */
    private static void showUsage() {
        System.out.println("\n📖 USAGE:");
        System.out.println("=========");
        System.out.println("mvn exec:java -Dexec.mainClass=\"com.dataengineering.kafka.testing.ProducerConsumerTestSuite\" -Dexec.args=\"<mode>\"");
        System.out.println("\nMODES:");
        System.out.println("  auto         - Automated end-to-end test");
        System.out.println("  interactive  - Interactive producer with live consumer");
        System.out.println("  consumer-only - Start consumer (use with separate producer)");
        System.out.println("  producer-only - Start producer (use with separate consumer)");
        System.out.println("  load         - Load test with many messages");
        
        System.out.println("\n🚀 RECOMMENDED WORKFLOWS:");
        System.out.println("========================");
        System.out.println("1. AUTOMATED TEST:");
        System.out.println("   mvn exec:java -Dexec.mainClass=\"com.dataengineering.kafka.testing.ProducerConsumerTestSuite\" -Dexec.args=\"auto\"");
        
        System.out.println("\n2. INTERACTIVE TEST:");
        System.out.println("   mvn exec:java -Dexec.mainClass=\"com.dataengineering.kafka.testing.ProducerConsumerTestSuite\" -Dexec.args=\"interactive\"");
        
        System.out.println("\n3. SEPARATE TERMINALS:");
        System.out.println("   Terminal 1: mvn exec:java -Dexec.mainClass=\"com.dataengineering.kafka.testing.ProducerConsumerTestSuite\" -Dexec.args=\"consumer-only\"");
        System.out.println("   Terminal 2: mvn exec:java -Dexec.mainClass=\"com.dataengineering.kafka.testing.ProducerConsumerTestSuite\" -Dexec.args=\"producer-only\"");
    }
    
    /**
     * Method 1: Automated End-to-End Test
     */
    private static void runAutomatedTest() {
        System.out.println("\n🤖 AUTOMATED END-TO-END TEST");
        System.out.println("===========================");
        
        SalesEventConsumer consumer = null;
        SalesEventProducer producer = null;
        Thread consumerThread = null;
        
        try {
            // Start consumer
            System.out.println("📥 Starting consumer...");
            consumer = new SalesEventConsumer();
            consumerThread = consumer.startInBackground();
            Thread.sleep(2000); // Wait for consumer to initialize
            
            // Start producer
            System.out.println("📤 Starting producer...");
            producer = new SalesEventProducer();
            
            // Send test messages
            System.out.println("📦 Sending test messages...");
            for (int i = 1; i <= 3; i++) {
                SalesRecord record = createTestRecord("AUTO_TEST_" + String.format("%03d", i));
                System.out.println("   📤 Sending: " + record.getTransactionId() + " | Product: " + record.getProductName());
                
                boolean success = producer.sendSalesEvent(record);
                System.out.println("   " + (success ? "✅ Sent" : "❌ Failed"));
                
                Thread.sleep(1000); // Wait between messages
            }
            
            // Wait for processing
            System.out.println("⏳ Waiting for consumer to process...");
            Thread.sleep(5000);
            
            System.out.println("✅ Automated test completed!");
            
        } catch (Exception e) {
            System.err.println("❌ Test failed: " + e.getMessage());
        } finally {
            cleanup(producer, consumer, consumerThread);
        }
    }
    
    /**
     * Method 2: Interactive Test
     */
    private static void runInteractiveTest() {
        System.out.println("\n🎮 INTERACTIVE PRODUCER-CONSUMER TEST");
        System.out.println("====================================");
        
        SalesEventConsumer consumer = null;
        SalesEventProducer producer = null;
        Thread consumerThread = null;
        Scanner scanner = new Scanner(System.in);
        
        try {
            // Start consumer
            System.out.println("📥 Starting consumer...");
            consumer = new SalesEventConsumer();
            consumerThread = consumer.startInBackground();
            Thread.sleep(2000);
            
            // Start producer
            System.out.println("📤 Starting producer...");
            producer = new SalesEventProducer();
            
            System.out.println("\n🎯 INTERACTIVE MODE STARTED");
            System.out.println("==========================");
            System.out.println("Commands:");
            System.out.println("  'send' - Send a random message");
            System.out.println("  'high' - Send high-value message");
            System.out.println("  'batch' - Send 5 messages");
            System.out.println("  'quit' - Exit");
            
            String command;
            int messageCount = 1;
            
            while (true) {
                System.out.print("\n🎮 Command: ");
                command = scanner.nextLine().trim().toLowerCase();
                
                switch (command) {
                    case "send":
                        SalesRecord record = createTestRecord("INTERACTIVE_" + String.format("%03d", messageCount++));
                        System.out.println("📤 Sending: " + record.getTransactionId());
                        boolean success = producer.sendSalesEvent(record);
                        System.out.println(success ? "✅ Sent!" : "❌ Failed!");
                        break;
                        
                    case "high":
                        SalesRecord highValue = createHighValueRecord("HIGH_VALUE_" + String.format("%03d", messageCount++));
                        System.out.println("💰 Sending high-value: " + highValue.getTransactionId() + " ($" + highValue.getTotalAmount() + ")");
                        boolean highSuccess = producer.sendSalesEvent(highValue);
                        System.out.println(highSuccess ? "✅ Sent!" : "❌ Failed!");
                        break;
                        
                    case "batch":
                        System.out.println("📦 Sending batch of 5 messages...");
                        for (int i = 0; i < 5; i++) {
                            SalesRecord batchRecord = createTestRecord("BATCH_" + messageCount + "_" + (i + 1));
                            producer.sendSalesEvent(batchRecord);
                            System.out.println("   📤 Sent: " + batchRecord.getTransactionId());
                            Thread.sleep(500);
                        }
                        messageCount += 5;
                        System.out.println("✅ Batch completed!");
                        break;
                        
                    case "quit":
                        System.out.println("🛑 Exiting interactive mode...");
                        return;
                        
                    default:
                        System.out.println("❓ Unknown command. Use: send, high, batch, quit");
                }
            }
            
        } catch (Exception e) {
            System.err.println("❌ Interactive test failed: " + e.getMessage());
        } finally {
            cleanup(producer, consumer, consumerThread);
            scanner.close();
        }
    }
    
    /**
     * Method 3: Consumer Only (for separate terminal testing)
     */
    private static void runConsumerOnly() {
        System.out.println("\n📥 CONSUMER-ONLY MODE");
        System.out.println("====================");
        System.out.println("💡 Start producer in another terminal:");
        System.out.println("mvn exec:java -Dexec.mainClass=\"com.dataengineering.kafka.testing.ProducerConsumerTestSuite\" -Dexec.args=\"producer-only\"");
        
        try {
            SalesEventConsumer consumer = new SalesEventConsumer();
            
            // Add shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\n🛑 Shutting down consumer...");
                consumer.stop();
            }));
            
            // Start consuming (blocking)
            consumer.startConsuming();
            
        } catch (Exception e) {
            System.err.println("❌ Consumer failed: " + e.getMessage());
        }
    }
    
    /**
     * Method 4: Producer Only (for separate terminal testing)
     */
    private static void runProducerOnly() {
        System.out.println("\n📤 PRODUCER-ONLY MODE");
        System.out.println("====================");
        System.out.println("💡 Make sure consumer is running in another terminal!");
        
        Scanner scanner = new Scanner(System.in);
        
        try {
            SalesEventProducer producer = new SalesEventProducer();
            
            System.out.println("\n🎯 PRODUCER COMMANDS:");
            System.out.println("===================");
            System.out.println("  'send' - Send a message");
            System.out.println("  'batch <n>' - Send n messages");
            System.out.println("  'quit' - Exit");
            
            String command;
            int messageCount = 1;
            
            while (true) {
                System.out.print("\n📤 Producer: ");
                command = scanner.nextLine().trim();
                
                if (command.equals("quit")) {
                    break;
                } else if (command.equals("send")) {
                    SalesRecord record = createTestRecord("PRODUCER_" + String.format("%03d", messageCount++));
                    boolean success = producer.sendSalesEvent(record);
                    System.out.println((success ? "✅ Sent: " : "❌ Failed: ") + record.getTransactionId());
                } else if (command.startsWith("batch ")) {
                    try {
                        int count = Integer.parseInt(command.substring(6));
                        System.out.println("📦 Sending " + count + " messages...");
                        for (int i = 0; i < count; i++) {
                            SalesRecord record = createTestRecord("BATCH_" + messageCount + "_" + (i + 1));
                            producer.sendSalesEvent(record);
                            System.out.println("   📤 " + (i + 1) + "/" + count + ": " + record.getTransactionId());
                            Thread.sleep(200);
                        }
                        messageCount += count;
                        System.out.println("✅ Batch completed!");
                    } catch (NumberFormatException e) {
                        System.out.println("❌ Invalid batch size. Use: batch <number>");
                    }
                } else {
                    System.out.println("❓ Unknown command. Use: send, batch <n>, quit");
                }
            }
            
            producer.close();
            
        } catch (Exception e) {
            System.err.println("❌ Producer failed: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }
    
    /**
     * Method 5: Load Test
     */
    private static void runLoadTest() {
        System.out.println("\n🔥 LOAD TEST");
        System.out.println("============");
        
        SalesEventConsumer consumer = null;
        SalesEventProducer producer = null;
        Thread consumerThread = null;
        
        try {
            // Start consumer
            System.out.println("📥 Starting consumer...");
            consumer = new SalesEventConsumer();
            consumerThread = consumer.startInBackground();
            Thread.sleep(2000);
            
            // Start producer
            System.out.println("📤 Starting producer...");
            producer = new SalesEventProducer();
            
            // Load test parameters
            int messageCount = 50;
            int batchSize = 10;
            
            System.out.println("🚀 Starting load test: " + messageCount + " messages in batches of " + batchSize);
            
            long startTime = System.currentTimeMillis();
            
            for (int batch = 0; batch < messageCount / batchSize; batch++) {
                System.out.println("\n📦 Batch " + (batch + 1) + "/" + (messageCount / batchSize));
                
                for (int i = 0; i < batchSize; i++) {
                    int msgNum = batch * batchSize + i + 1;
                    SalesRecord record = createTestRecord("LOAD_TEST_" + String.format("%03d", msgNum));
                    producer.sendSalesEvent(record);
                    System.out.print("📤 ");
                }
                
                System.out.println(" ✅ Batch " + (batch + 1) + " sent!");
                Thread.sleep(1000); // Brief pause between batches
            }
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            System.out.println("\n📊 LOAD TEST RESULTS:");
            System.out.println("====================");
            System.out.println("Messages sent: " + messageCount);
            System.out.println("Duration: " + duration + "ms");
            System.out.println("Rate: " + (messageCount * 1000.0 / duration) + " messages/second");
            
            // Wait for processing
            System.out.println("\n⏳ Waiting for consumer to process all messages...");
            Thread.sleep(10000);
            
        } catch (Exception e) {
            System.err.println("❌ Load test failed: " + e.getMessage());
        } finally {
            cleanup(producer, consumer, consumerThread);
        }
    }
    
    /**
     * Helper method to create test records
     */
    private static SalesRecord createTestRecord(String transactionId) {
        String[] products = {"Laptop", "Mouse", "Keyboard", "Monitor", "Webcam"};
        String[] customers = {"CUST001", "CUST002", "CUST003", "CUST004", "CUST005"};
        
        SalesRecord record = new SalesRecord();
        record.setTransactionId(transactionId);
        record.setCustomerId(customers[(int) (Math.random() * customers.length)]);
        record.setProductName(products[(int) (Math.random() * products.length)]);
        record.setProductCategory("Electronics");
        record.setQuantity((int) (Math.random() * 3) + 1);
        record.setUnitPrice(BigDecimal.valueOf(Math.random() * 1000 + 100));
        record.setStoreLocation("Test Store");
        record.setSalesPerson("Test Sales");
        record.setSaleDate(LocalDateTime.now());
        
        return record;
    }
    
    /**
     * Helper method to create high-value records
     */
    private static SalesRecord createHighValueRecord(String transactionId) {
        SalesRecord record = createTestRecord(transactionId);
        record.setProductName("Enterprise Server");
        record.setUnitPrice(BigDecimal.valueOf(5000.0)); // High value for analytics
        record.setQuantity(2);
        return record;
    }
    
    /**
     * Cleanup resources
     */
    private static void cleanup(SalesEventProducer producer, SalesEventConsumer consumer, Thread consumerThread) {
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
    }
} 