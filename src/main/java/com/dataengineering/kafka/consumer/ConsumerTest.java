package com.dataengineering.kafka.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ConsumerTest - Test class for testing Kafka consumer functionality
 * 
 * This class demonstrates how to:
 * 1. Start the consumer in background
 * 2. Monitor message consumption
 * 3. Handle graceful shutdown
 */
public class ConsumerTest {
    
    private static final Logger logger = LoggerFactory.getLogger(ConsumerTest.class);
    
    public static void main(String[] args) {
        logger.info("🧪 Starting Kafka Consumer Test...");
        System.out.println("🧪 Starting Kafka Consumer Test...");
        
        try {
            // Create and start consumer
            SalesEventConsumer consumer = new SalesEventConsumer();
            Thread consumerThread = consumer.startInBackground();
            
            // Let it run for 30 seconds to consume any messages
            System.out.println("⏳ Consumer running for 30 seconds...");
            System.out.println("📢 Send some messages using the producer test!");
            System.out.println("💡 In another terminal, run: mvn exec:java -Dexec.mainClass=\"com.dataengineering.kafka.producer.ProducerTest\"");
            
            Thread.sleep(30000);
            
            // Stop consumer
            System.out.println("🛑 Stopping consumer...");
            consumer.stop();
            
            // Wait for consumer thread to finish
            consumerThread.join(5000);
            
            System.out.println("✅ Consumer test completed!");
            
        } catch (Exception e) {
            logger.error("❌ Consumer test failed", e);
            System.err.println("❌ Consumer test failed: " + e.getMessage());
        }
    }
} 