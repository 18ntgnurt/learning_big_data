package com.dataengineering.kafka.testing;

import com.dataengineering.kafka.config.KafkaConfig;
import com.dataengineering.kafka.consumer.SalesEventConsumer;
import com.dataengineering.kafka.producer.SalesEventProducer;
import com.dataengineering.kafka.streams.SalesStreamProcessor;
import com.dataengineering.model.SalesRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

/**
 * StreamProcessorTestSuite demonstrates Kafka Streams (streaming mode) vs traditional producer-consumer.
 * 
 * This class provides comprehensive testing for:
 * - Stream processing with real-time analytics
 * - Comparison between streaming and consumer modes
 * - Windowed aggregations testing
 * - High-value sales detection
 * - Real-time monitoring of stream processing
 */
public class StreamProcessorTestSuite {
    
    private static final Logger logger = LoggerFactory.getLogger(StreamProcessorTestSuite.class);
    
    private SalesEventProducer producer;
    private SalesEventConsumer consumer;
    private SalesStreamProcessor streamProcessor;
    
    public static void main(String[] args) {
        StreamProcessorTestSuite testSuite = new StreamProcessorTestSuite();
        
        if (args.length == 0) {
            testSuite.showHelp();
            return;
        }
        
        String testMode = args[0].toLowerCase();
        
        switch (testMode) {
            case "stream-only":
                testSuite.testStreamProcessorOnly();
                break;
            case "consumer-only":
                testSuite.testConsumerOnly();
                break;
            case "comparison":
                testSuite.testStreamVsConsumer();
                break;
            case "windowed":
                testSuite.testWindowedAggregations();
                break;
            case "real-time":
                testSuite.testRealTimeAnalytics();
                break;
            case "interactive":
                testSuite.interactiveStreamTesting();
                break;
            default:
                testSuite.showHelp();
        }
    }
    
    private void showHelp() {
        System.out.println("\n🌊 KAFKA STREAMS TEST SUITE");
        System.out.println("============================");
        System.out.println("Available test modes:");
        System.out.println();
        System.out.println("📊 STREAMING MODE TESTS:");
        System.out.println("  'stream-only'   - Test Kafka Streams processor only");
        System.out.println("  'windowed'      - Test time-windowed aggregations");
        System.out.println("  'real-time'     - Real-time analytics demonstration");
        System.out.println();
        System.out.println("🔄 COMPARISON TESTS:");
        System.out.println("  'consumer-only' - Test traditional consumer only");
        System.out.println("  'comparison'    - Compare streaming vs consumer modes");
        System.out.println();
        System.out.println("🎮 INTERACTIVE TESTS:");
        System.out.println("  'interactive'   - Interactive stream testing");
        System.out.println();
        System.out.println("Example usage:");
        System.out.println("mvn exec:java -Dexec.mainClass=\"com.dataengineering.kafka.testing.StreamProcessorTestSuite\" -Dexec.args=\"stream-only\"");
    }
    
    /**
     * Test Kafka Streams processor only
     */
    private void testStreamProcessorOnly() {
        System.out.println("\n🌊 STREAM PROCESSOR ONLY MODE");
        System.out.println("==============================");
        
        try {
            // Initialize components
            producer = new SalesEventProducer();
            streamProcessor = new SalesStreamProcessor();
            
            // Start stream processor
            System.out.println("🚀 Starting stream processor...");
            streamProcessor.start();
            Thread.sleep(3000); // Wait for startup
            
            // Send test data
            System.out.println("📤 Sending test sales data...");
            List<SalesRecord> testData = generateStreamingTestData();
            sendDataWithDelay(testData, 1000); // 1 second between messages
            
            // Keep running to see stream processing
            System.out.println("🔄 Stream processing active. Check output topics:");
            System.out.println("   - " + KafkaConfig.HIGH_VALUE_SALES_TOPIC);
            System.out.println("   - " + KafkaConfig.SALES_ANALYTICS_TOPIC);
            System.out.println("   - " + KafkaConfig.WINDOWED_SALES_METRICS_TOPIC);
            System.out.println();
            System.out.println("⏰ Running for 30 seconds... Press Ctrl+C to stop");
            
            Thread.sleep(30000);
            
        } catch (Exception e) {
            logger.error("❌ Stream processor test failed", e);
        } finally {
            cleanup();
        }
    }
    
    /**
     * Test traditional consumer only (for comparison)
     */
    private void testConsumerOnly() {
        System.out.println("\n🔄 CONSUMER ONLY MODE");
        System.out.println("=====================");
        
        try {
            // Initialize components
            producer = new SalesEventProducer();
            consumer = new SalesEventConsumer();
            
            // Start consumer in background
            System.out.println("🚀 Starting consumer...");
            CompletableFuture.runAsync(() -> consumer.startConsuming());
            Thread.sleep(2000); // Wait for startup
            
            // Send test data
            System.out.println("📤 Sending test sales data...");
            List<SalesRecord> testData = generateStreamingTestData();
            sendDataWithDelay(testData, 1000); // 1 second between messages
            
            // Keep running
            System.out.println("🔄 Consumer processing active...");
            System.out.println("⏰ Running for 30 seconds... Press Ctrl+C to stop");
            
            Thread.sleep(30000);
            
        } catch (Exception e) {
            logger.error("❌ Consumer test failed", e);
        } finally {
            cleanup();
        }
    }
    
    /**
     * Compare streaming mode vs consumer mode
     */
    private void testStreamVsConsumer() {
        System.out.println("\n⚖️  STREAMING VS CONSUMER COMPARISON");
        System.out.println("====================================");
        
        try {
            // Initialize all components
            producer = new SalesEventProducer();
            consumer = new SalesEventConsumer();
            streamProcessor = new SalesStreamProcessor();
            
            // Start both consumer and stream processor
            System.out.println("🚀 Starting consumer and stream processor...");
            
            CompletableFuture.runAsync(() -> consumer.startConsuming());
            streamProcessor.start();
            Thread.sleep(3000); // Wait for startup
            
            // Send test data
            System.out.println("📤 Sending test data to both...");
            List<SalesRecord> testData = generateComparisonTestData();
            
            System.out.println("\n📊 WHAT TO OBSERVE:");
            System.out.println("🔄 Consumer: Processes each message individually");
            System.out.println("🌊 Stream:   Real-time aggregations and analytics");
            System.out.println();
            
            sendDataWithDelay(testData, 2000); // 2 seconds between messages
            
            System.out.println("⏰ Running for 45 seconds... Press Ctrl+C to stop");
            Thread.sleep(45000);
            
        } catch (Exception e) {
            logger.error("❌ Comparison test failed", e);
        } finally {
            cleanup();
        }
    }
    
    /**
     * Test windowed aggregations specifically
     */
    private void testWindowedAggregations() {
        System.out.println("\n⏰ WINDOWED AGGREGATIONS TEST");
        System.out.println("=============================");
        
        try {
            producer = new SalesEventProducer();
            streamProcessor = new SalesStreamProcessor();
            
            System.out.println("🚀 Starting stream processor...");
            streamProcessor.start();
            Thread.sleep(3000);
            
            System.out.println("📊 Testing 5-minute tumbling windows...");
            System.out.println("📤 Sending sales from different regions...");
            
            // Send data in bursts to test windowing
            List<SalesRecord> burst1 = generateRegionalSalesData("US", 5);
            List<SalesRecord> burst2 = generateRegionalSalesData("EU", 3);
            List<SalesRecord> burst3 = generateRegionalSalesData("ASIA", 4);
            
            // Send bursts with delays
            System.out.println("🇺🇸 Sending US sales burst...");
            sendDataBatch(burst1);
            Thread.sleep(2000);
            
            System.out.println("🇪🇺 Sending EU sales burst...");
            sendDataBatch(burst2);
            Thread.sleep(2000);
            
            System.out.println("🇯🇵 Sending ASIA sales burst...");
            sendDataBatch(burst3);
            
            System.out.println("\n📈 Watch windowed metrics in topic: " + KafkaConfig.WINDOWED_SALES_METRICS_TOPIC);
            System.out.println("⏰ Window duration: 5 minutes");
            System.out.println("🔄 Running for 60 seconds...");
            
            Thread.sleep(60000);
            
        } catch (Exception e) {
            logger.error("❌ Windowed aggregations test failed", e);
        } finally {
            cleanup();
        }
    }
    
    /**
     * Real-time analytics demonstration
     */
    private void testRealTimeAnalytics() {
        System.out.println("\n📈 REAL-TIME ANALYTICS DEMO");
        System.out.println("============================");
        
        try {
            producer = new SalesEventProducer();
            streamProcessor = new SalesStreamProcessor();
            
            System.out.println("🚀 Starting real-time analytics...");
            streamProcessor.start();
            Thread.sleep(3000);
            
            // Simulate real-time business scenarios
            System.out.println("🎭 Simulating real business scenarios:");
            System.out.println("  📊 Normal sales flow");
            System.out.println("  🚨 High-value transactions");
            System.out.println("  📈 Regional patterns");
            System.out.println("  ⏰ Time-based aggregations");
            System.out.println();
            
            // Scenario 1: Normal sales
            System.out.println("📊 Scenario 1: Normal business flow...");
            sendDataBatch(generateNormalSalesData(10));
            Thread.sleep(5000);
            
            // Scenario 2: High-value sales spike
            System.out.println("🚨 Scenario 2: High-value sales spike...");
            sendDataBatch(generateHighValueSalesData(5));
            Thread.sleep(5000);
            
            // Scenario 3: Regional activity
            System.out.println("🌍 Scenario 3: Regional activity patterns...");
            sendDataBatch(generateRegionalSalesData("US", 8));
            Thread.sleep(2000);
            sendDataBatch(generateRegionalSalesData("EU", 6));
            Thread.sleep(2000);
            sendDataBatch(generateRegionalSalesData("ASIA", 4));
            
            System.out.println("\n📊 Real-time analytics active for 60 seconds...");
            Thread.sleep(60000);
            
        } catch (Exception e) {
            logger.error("❌ Real-time analytics test failed", e);
        } finally {
            cleanup();
        }
    }
    
    /**
     * Interactive stream testing
     */
    private void interactiveStreamTesting() {
        System.out.println("\n🎮 INTERACTIVE STREAM TESTING");
        System.out.println("==============================");
        
        try {
            producer = new SalesEventProducer();
            streamProcessor = new SalesStreamProcessor();
            
            System.out.println("🚀 Starting interactive mode...");
            streamProcessor.start();
            Thread.sleep(3000);
            
            Scanner scanner = new Scanner(System.in);
            boolean running = true;
            
            while (running) {
                showInteractiveMenu();
                String command = scanner.nextLine().trim().toLowerCase();
                
                switch (command) {
                    case "1":
                    case "normal":
                        sendDataBatch(generateNormalSalesData(5));
                        System.out.println("✅ Sent 5 normal sales");
                        break;
                    case "2":
                    case "high":
                        sendDataBatch(generateHighValueSalesData(3));
                        System.out.println("✅ Sent 3 high-value sales");
                        break;
                    case "3":
                    case "regional":
                        System.out.print("Enter region (US/EU/ASIA): ");
                        String region = scanner.nextLine().trim().toUpperCase();
                        sendDataBatch(generateRegionalSalesData(region, 5));
                        System.out.println("✅ Sent 5 sales for region: " + region);
                        break;
                    case "4":
                    case "burst":
                        sendDataBatch(generateStreamingTestData());
                        System.out.println("✅ Sent mixed sales burst");
                        break;
                    case "5":
                    case "status":
                        System.out.println("📊 Stream processor state: " + streamProcessor.getState());
                        break;
                    case "quit":
                    case "exit":
                        running = false;
                        break;
                    default:
                        System.out.println("❌ Unknown command: " + command);
                }
                
                if (running) {
                    System.out.println();
                }
            }
            
        } catch (Exception e) {
            logger.error("❌ Interactive testing failed", e);
        } finally {
            cleanup();
        }
    }
    
    private void showInteractiveMenu() {
        System.out.println("\n🎮 STREAM TESTING COMMANDS:");
        System.out.println("  1 or 'normal'   - Send normal sales data");
        System.out.println("  2 or 'high'     - Send high-value sales");
        System.out.println("  3 or 'regional' - Send regional sales data");
        System.out.println("  4 or 'burst'    - Send mixed sales burst");
        System.out.println("  5 or 'status'   - Check stream processor status");
        System.out.println("  'quit' or 'exit' - Exit");
        System.out.print("\n🎯 Enter command: ");
    }
    
    // Data generation methods
    
    private List<SalesRecord> generateStreamingTestData() {
        List<SalesRecord> records = new ArrayList<>();
        Random random = new Random();
        String[] regions = {"US", "EU", "ASIA"};
        String[] categories = {"Electronics", "Clothing", "Books", "Home"};
        String[] products = {"Laptop", "Phone", "Tablet", "Watch", "Shoes", "Shirt"};
        
        for (int i = 1; i <= 15; i++) {
            String region = regions[random.nextInt(regions.length)];
            String category = categories[random.nextInt(categories.length)];
            String product = products[random.nextInt(products.length)];
            
            // Mix of normal and high-value sales
            double baseAmount = random.nextDouble() * 500 + 50; // $50-$550
            if (i % 4 == 0) {
                baseAmount += 1000; // Make some high-value
            }
            
            SalesRecord record = new SalesRecord(
                "STREAM_" + String.format("%03d", i),
                "CUST" + String.format("%03d", random.nextInt(100)),
                product,
                category,
                random.nextInt(5) + 1,
                BigDecimal.valueOf(baseAmount / (random.nextInt(5) + 1)),
                LocalDateTime.now(),
                region,
                "SALES_REP_" + random.nextInt(10)
            );
            records.add(record);
        }
        
        return records;
    }
    
    private List<SalesRecord> generateComparisonTestData() {
        List<SalesRecord> records = new ArrayList<>();
        
        // Generate data that will show difference between streaming and consumer
        // High-value sales for stream alerts
        records.add(createSalesRecord("COMP_001", "Laptop", 1500.0, "Electronics", "US"));
        records.add(createSalesRecord("COMP_002", "Phone", 800.0, "Electronics", "EU"));
        records.add(createSalesRecord("COMP_003", "Server", 2500.0, "Electronics", "US"));
        
        // Normal sales for aggregations
        records.add(createSalesRecord("COMP_004", "Book", 25.0, "Books", "US"));
        records.add(createSalesRecord("COMP_005", "Shirt", 45.0, "Clothing", "EU"));
        records.add(createSalesRecord("COMP_006", "Shoes", 120.0, "Clothing", "ASIA"));
        
        return records;
    }
    
    private List<SalesRecord> generateRegionalSalesData(String region, int count) {
        List<SalesRecord> records = new ArrayList<>();
        Random random = new Random();
        String[] products = {"Product_A", "Product_B", "Product_C"};
        
        for (int i = 1; i <= count; i++) {
            String product = products[random.nextInt(products.length)];
            double amount = random.nextDouble() * 300 + 50;
            
            records.add(createSalesRecord(
                region + "_" + String.format("%03d", i),
                product,
                amount,
                "General",
                region
            ));
        }
        
        return records;
    }
    
    private List<SalesRecord> generateNormalSalesData(int count) {
        List<SalesRecord> records = new ArrayList<>();
        Random random = new Random();
        
        for (int i = 1; i <= count; i++) {
            double amount = random.nextDouble() * 500 + 50; // $50-$550
            records.add(createSalesRecord(
                "NORMAL_" + String.format("%03d", i),
                "Product_" + i,
                amount,
                "General",
                "US"
            ));
        }
        
        return records;
    }
    
    private List<SalesRecord> generateHighValueSalesData(int count) {
        List<SalesRecord> records = new ArrayList<>();
        Random random = new Random();
        
        for (int i = 1; i <= count; i++) {
            double amount = random.nextDouble() * 2000 + 1000; // $1000-$3000
            records.add(createSalesRecord(
                "HIGH_" + String.format("%03d", i),
                "Premium_Product_" + i,
                amount,
                "Premium",
                "US"
            ));
        }
        
        return records;
    }
    
    private SalesRecord createSalesRecord(String id, String product, double amount, String category, String region) {
        Random random = new Random();
        int quantity = random.nextInt(3) + 1;
        BigDecimal unitPrice = BigDecimal.valueOf(amount / quantity);
        
        return new SalesRecord(
            id,
            "CUST" + String.format("%03d", random.nextInt(100)),
            product,
            category,
            quantity,
            unitPrice,
            LocalDateTime.now(),
            region,
            "SALES_REP_" + random.nextInt(10)
        );
    }
    
    // Utility methods
    
    private void sendDataBatch(List<SalesRecord> records) {
        for (SalesRecord record : records) {
            producer.sendSalesEventAsync(record);
        }
        producer.flush(); // Ensure all messages are sent
    }
    
    private void sendDataWithDelay(List<SalesRecord> records, long delayMs) {
        for (SalesRecord record : records) {
            producer.sendSalesEventAsync(record);
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    private void cleanup() {
        System.out.println("\n🧹 Cleaning up...");
        
        if (streamProcessor != null) {
            streamProcessor.stop();
        }
        if (consumer != null) {
            consumer.stop();
        }
        if (producer != null) {
            producer.close();
        }
        
        System.out.println("✅ Cleanup completed");
    }
} 