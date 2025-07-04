package com.dataengineering.kafka.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MonitoringTest - Demonstrates Kafka monitoring capabilities
 */
public class MonitoringTest {
    
    private static final Logger logger = LoggerFactory.getLogger(MonitoringTest.class);
    
    public static void main(String[] args) {
        System.out.println("🔍 Kafka Monitoring Demo");
        System.out.println("=======================");
        
        KafkaMonitor monitor = new KafkaMonitor();
        
        try {
            // Test connectivity first
            if (!monitor.testConnection()) {
                System.err.println("❌ Cannot connect to Kafka!");
                System.err.println("💡 Make sure Kafka is running: docker-compose -f docker-compose-kafka.yml up -d");
                return;
            }
            
            // Show comprehensive monitoring
            System.out.println("\n📊 COMPREHENSIVE KAFKA MONITORING");
            System.out.println("=================================");
            
            monitor.showClusterInfo();
            monitor.showTopicInfo();
            monitor.showConsumerGroups();
            
            System.out.println("\n💡 MONITORING OPTIONS:");
            System.out.println("===================");
            System.out.println("🌐 Web UI: http://localhost:9090");
            System.out.println("🔧 CLI: ./monitor-kafka.sh");
            System.out.println("☕ Java Live: mvn exec:java -Dexec.mainClass=\"com.dataengineering.kafka.monitoring.KafkaMonitor\" -Dexec.args=\"live 5\"");
            System.out.println("📊 Java One-time: mvn exec:java -Dexec.mainClass=\"com.dataengineering.kafka.monitoring.KafkaMonitor\"");
            
        } catch (Exception e) {
            System.err.println("❌ Monitoring failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            monitor.close();
        }
    }
} 