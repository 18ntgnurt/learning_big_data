package com.dataengineering.kafka.testing;

import com.dataengineering.kafka.producer.SalesEventProducer;
import com.dataengineering.kafka.streams.SalesStreamProcessor;
import com.dataengineering.model.SalesRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Scanner;

/**
 * SimpleStreamDemo provides an easy way to understand and interact with Kafka Streams.
 * 
 * This demo shows:
 * - Step-by-step stream processor initialization
 * - Real-time message sending and processing
 * - Clear output of what's happening behind the scenes
 * - Interactive commands to send different types of data
 */
public class SimpleStreamDemo {
    
    private static final Logger logger = LoggerFactory.getLogger(SimpleStreamDemo.class);
    
    private SalesEventProducer producer;
    private SalesStreamProcessor streamProcessor;
    private boolean isRunning = false;
    
    public static void main(String[] args) {
        SimpleStreamDemo demo = new SimpleStreamDemo();
        demo.runDemo();
    }
    
    public void runDemo() {
        System.out.println("\n🌊 SIMPLE KAFKA STREAMS DEMO");
        System.out.println("=============================");
        System.out.println("This demo shows you step-by-step how streaming mode works!");
        System.out.println();
        
        try {
            // Step 1: Initialize components
            initializeComponents();
            
            // Step 2: Start stream processing
            startStreamProcessing();
            
            // Step 3: Interactive demo
            runInteractiveDemo();
            
        } catch (Exception e) {
            logger.error("❌ Demo failed", e);
            System.err.println("❌ Demo failed: " + e.getMessage());
        } finally {
            cleanup();
        }
    }
    
    private void initializeComponents() {
        System.out.println("📋 STEP 1: Initializing Components");
        System.out.println("-----------------------------------");
        
        System.out.print("🔧 Creating Kafka producer... ");
        producer = new SalesEventProducer();
        System.out.println("✅ Done!");
        
        System.out.print("🌊 Creating stream processor... ");
        streamProcessor = new SalesStreamProcessor();
        System.out.println("✅ Done!");
        
        System.out.println("✅ All components initialized!\n");
    }
    
    private void startStreamProcessing() throws InterruptedException {
        System.out.println("🚀 STEP 2: Starting Stream Processing");
        System.out.println("--------------------------------------");
        
        System.out.print("🔄 Starting stream processor...");
        streamProcessor.start();
        
        // Wait and show progress
        for (int i = 0; i < 10; i++) {
            Thread.sleep(500);
            System.out.print(".");
        }
        System.out.println(" ✅ Started!");
        
        System.out.println("📊 Stream processor state: " + streamProcessor.getState());
        System.out.println("🎯 Ready to process sales events!\n");
        
        isRunning = true;
    }
    
    private void runInteractiveDemo() {
        System.out.println("🎮 STEP 3: Interactive Demo");
        System.out.println("----------------------------");
        System.out.println("Now you can send sales data and see real-time stream processing!");
        System.out.println();
        
        Scanner scanner = new Scanner(System.in);
        boolean demoRunning = true;
        
        while (demoRunning && isRunning) {
            showDemoMenu();
            String command = scanner.nextLine().trim().toLowerCase();
            
            switch (command) {
                case "1":
                case "normal":
                    sendNormalSale();
                    break;
                case "2":
                case "high":
                    sendHighValueSale();
                    break;
                case "3":
                case "batch":
                    sendSalesBatch();
                    break;
                case "4":
                case "status":
                    showStatus();
                    break;
                case "5":
                case "explain":
                    explainStreaming();
                    break;
                case "quit":
                case "exit":
                    demoRunning = false;
                    break;
                default:
                    System.out.println("❌ Unknown command. Try again!");
            }
            
            if (demoRunning) {
                System.out.println("\n" + "=".repeat(50) + "\n");
            }
        }
    }
    
    private void showDemoMenu() {
        System.out.println("🎯 DEMO COMMANDS:");
        System.out.println("  1 or 'normal' - Send a normal sale ($100-$500)");
        System.out.println("  2 or 'high'   - Send a high-value sale ($1000+)");
        System.out.println("  3 or 'batch'  - Send 5 mixed sales");
        System.out.println("  4 or 'status' - Check stream processor status");
        System.out.println("  5 or 'explain'- Explain what streaming does");
        System.out.println("  'quit' or 'exit' - Exit demo");
        System.out.print("\n🎮 Enter command: ");
    }
    
    private void sendNormalSale() {
        System.out.println("\n📤 SENDING NORMAL SALE");
        System.out.println("----------------------");
        
        SalesRecord sale = createSampleSale("NORMAL_001", "Laptop", 299.99, "Electronics", "US");
        
        System.out.println("💼 Sale Details:");
        System.out.println("   ID: " + sale.getTransactionId());
        System.out.println("   Product: " + sale.getProductName());
        System.out.println("   Amount: $" + sale.getTotalAmount());
        System.out.println("   Category: " + sale.getProductCategory());
        System.out.println("   Region: " + sale.getStoreLocation());
        
        producer.sendSalesEventAsync(sale);
        producer.flush();
        
        System.out.println("✅ Sale sent to Kafka!");
        System.out.println("🌊 Stream processor will:");
        System.out.println("   - Parse the JSON message");
        System.out.println("   - Check if it's high-value (>$1000) - NO");
        System.out.println("   - Add to category aggregations (Electronics)");
        System.out.println("   - Add to regional window aggregations (US)");
    }
    
    private void sendHighValueSale() {
        System.out.println("\n🚨 SENDING HIGH-VALUE SALE");
        System.out.println("---------------------------");
        
        SalesRecord sale = createSampleSale("HIGH_001", "Enterprise Server", 2500.00, "IT Equipment", "US");
        
        System.out.println("💼 Sale Details:");
        System.out.println("   ID: " + sale.getTransactionId());
        System.out.println("   Product: " + sale.getProductName());
        System.out.println("   Amount: $" + sale.getTotalAmount() + " 🚨 HIGH VALUE!");
        System.out.println("   Category: " + sale.getProductCategory());
        System.out.println("   Region: " + sale.getStoreLocation());
        
        producer.sendSalesEventAsync(sale);
        producer.flush();
        
        System.out.println("✅ High-value sale sent to Kafka!");
        System.out.println("🌊 Stream processor will:");
        System.out.println("   - Parse the JSON message");
        System.out.println("   - Check if it's high-value (>$1000) - YES! 🚨");
        System.out.println("   - Send alert to high-value-sales topic");
        System.out.println("   - Enrich with metadata (timestamp, alert level)");
        System.out.println("   - Add to category aggregations (IT Equipment)");
        System.out.println("   - Add to regional window aggregations (US)");
    }
    
    private void sendSalesBatch() {
        System.out.println("\n📦 SENDING SALES BATCH");
        System.out.println("----------------------");
        
        SalesRecord[] sales = {
            createSampleSale("BATCH_001", "Phone", 899.99, "Electronics", "EU"),
            createSampleSale("BATCH_002", "Luxury Watch", 1599.99, "Accessories", "US"),
            createSampleSale("BATCH_003", "Book", 24.99, "Books", "ASIA"),
            createSampleSale("BATCH_004", "Gaming PC", 2199.99, "Electronics", "US"),
            createSampleSale("BATCH_005", "Shoes", 129.99, "Clothing", "EU")
        };
        
        System.out.println("💼 Batch Details:");
        for (SalesRecord sale : sales) {
            String valueType = sale.getTotalAmount().doubleValue() > 1000 ? "🚨 HIGH" : "📊 NORMAL";
            System.out.println("   " + sale.getTransactionId() + ": $" + 
                             sale.getTotalAmount() + " " + valueType + " (" + 
                             sale.getStoreLocation() + ")");
        }
        
        for (SalesRecord sale : sales) {
            producer.sendSalesEventAsync(sale);
        }
        producer.flush();
        
        System.out.println("✅ Batch of 5 sales sent to Kafka!");
        System.out.println("🌊 Stream processor will process each one:");
        System.out.println("   - 2 high-value sales → high-value-sales topic");
        System.out.println("   - 3 normal sales → category aggregations");
        System.out.println("   - All 5 → regional window aggregations");
        System.out.println("   - Real-time analytics updated!");
    }
    
    private void showStatus() {
        System.out.println("\n📊 STREAM PROCESSOR STATUS");
        System.out.println("---------------------------");
        System.out.println("State: " + streamProcessor.getState());
        System.out.println("Running: " + (isRunning ? "✅ YES" : "❌ NO"));
        System.out.println();
        System.out.println("📈 What the stream processor is doing:");
        System.out.println("   🔄 Continuously reading from 'sales-events' topic");
        System.out.println("   📊 Aggregating sales by category in real-time");
        System.out.println("   ⏰ Creating 5-minute window summaries by region");
        System.out.println("   🚨 Detecting high-value sales (>$1000)");
        System.out.println("   📤 Sending results to multiple output topics");
    }
    
    private void explainStreaming() {
        System.out.println("\n🎓 WHAT IS STREAMING MODE?");
        System.out.println("---------------------------");
        System.out.println("🔄 PRODUCER-CONSUMER (what we had before):");
        System.out.println("   Producer → Topic → Consumer → Process one message");
        System.out.println("   Simple, but limited to basic processing");
        System.out.println();
        System.out.println("🌊 KAFKA STREAMS (streaming mode):");
        System.out.println("   Producer → Stream Processor → Multiple Analytics");
        System.out.println("   ├── High-value detection");
        System.out.println("   ├── Real-time aggregations");
        System.out.println("   ├── Time-windowed analytics");
        System.out.println("   └── Complex transformations");
        System.out.println();
        System.out.println("💡 KEY DIFFERENCES:");
        System.out.println("   📊 Stateful: Remembers data across messages");
        System.out.println("   ⏰ Windowed: Groups data by time periods");
        System.out.println("   🔀 Branched: Processes different data differently");
        System.out.println("   📈 Real-time: Analytics update immediately");
        System.out.println();
        System.out.println("🎯 USE CASES:");
        System.out.println("   - Real-time dashboards");
        System.out.println("   - Fraud detection");
        System.out.println("   - Live recommendations");
        System.out.println("   - Instant alerts");
    }
    
    private SalesRecord createSampleSale(String id, String product, double amount, String category, String region) {
        int quantity = 1;
        BigDecimal unitPrice = BigDecimal.valueOf(amount);
        
        return new SalesRecord(
            id,
            "CUST_DEMO_001",
            product,
            category,
            quantity,
            unitPrice,
            LocalDateTime.now(),
            region,
            "DEMO_SALES_REP"
        );
    }
    
    private void cleanup() {
        System.out.println("\n🧹 CLEANING UP");
        System.out.println("---------------");
        
        if (streamProcessor != null) {
            System.out.print("🛑 Stopping stream processor... ");
            streamProcessor.stop();
            System.out.println("✅ Stopped!");
        }
        
        if (producer != null) {
            System.out.print("🔒 Closing producer... ");
            producer.close();
            System.out.println("✅ Closed!");
        }
        
        System.out.println("✅ Demo cleanup completed!");
        System.out.println("\n🎉 Thanks for trying Kafka Streams!");
    }
} 