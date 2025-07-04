package com.dataengineering.shared.kafka;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Standardized Kafka topology configuration for the refactored architecture.
 * Defines all topics, partitions, and replication factors in one place.
 */
@Component
public class KafkaTopologyConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(KafkaTopologyConfig.class);
    
    // Topic configurations
    public static final class Topics {
        // Input topics
        public static final String TRANSACTIONS = "transactions";
        public static final String RAW_SALES_DATA = "raw-sales-data";
        
        // Processing topics
        public static final String ENRICHED_TRANSACTIONS = "enriched-transactions";
        public static final String HIGH_VALUE_TRANSACTIONS = "high-value-transactions";
        public static final String SUSPICIOUS_TRANSACTIONS = "suspicious-transactions";
        
        // Analytics topics
        public static final String TRANSACTION_ANALYTICS = "transaction-analytics";
        public static final String CUSTOMER_ANALYTICS = "customer-analytics";
        public static final String MERCHANT_ANALYTICS = "merchant-analytics";
        
        // ML and fraud detection topics
        public static final String FRAUD_DETECTION = "fraud-detection";
        public static final String FRAUD_PREDICTIONS = "fraud-predictions";
        public static final String FRAUD_ALERTS = "fraud-alerts";
        public static final String MODEL_TRAINING_DATA = "model-training-data";
        
        // Data quality topics
        public static final String DATA_QUALITY_ALERTS = "data-quality-alerts";
        public static final String DRIFT_DETECTION = "drift-detection";
        
        // Monitoring topics
        public static final String SYSTEM_METRICS = "system-metrics";
        public static final String PROCESSING_LOGS = "processing-logs";
        
        // Dead letter topics
        public static final String DLQ_TRANSACTIONS = "dlq-transactions";
        public static final String DLQ_FRAUD_DETECTION = "dlq-fraud-detection";
    }
    
    // Default configurations
    private static final int DEFAULT_PARTITIONS = 3;
    private static final short DEFAULT_REPLICATION_FACTOR = 1;
    private static final int HIGH_THROUGHPUT_PARTITIONS = 6;
    private static final int LOW_THROUGHPUT_PARTITIONS = 1;
    
    /**
     * Get all topic configurations for the system
     */
    public List<TopicConfig> getAllTopicConfigs() {
        return Arrays.asList(
            // Input topics - high throughput
            new TopicConfig(Topics.TRANSACTIONS, HIGH_THROUGHPUT_PARTITIONS, DEFAULT_REPLICATION_FACTOR,
                          "Main transaction stream from ETL layer"),
            new TopicConfig(Topics.RAW_SALES_DATA, DEFAULT_PARTITIONS, DEFAULT_REPLICATION_FACTOR,
                          "Raw sales data for batch processing"),
            
            // Processing topics - medium throughput
            new TopicConfig(Topics.ENRICHED_TRANSACTIONS, DEFAULT_PARTITIONS, DEFAULT_REPLICATION_FACTOR,
                          "Transactions enriched with customer/merchant data"),
            new TopicConfig(Topics.HIGH_VALUE_TRANSACTIONS, DEFAULT_PARTITIONS, DEFAULT_REPLICATION_FACTOR,
                          "High-value transactions requiring special handling"),
            new TopicConfig(Topics.SUSPICIOUS_TRANSACTIONS, DEFAULT_PARTITIONS, DEFAULT_REPLICATION_FACTOR,
                          "Potentially suspicious transactions for fraud analysis"),
            
            // Analytics topics - medium throughput
            new TopicConfig(Topics.TRANSACTION_ANALYTICS, DEFAULT_PARTITIONS, DEFAULT_REPLICATION_FACTOR,
                          "Real-time transaction analytics and aggregations"),
            new TopicConfig(Topics.CUSTOMER_ANALYTICS, DEFAULT_PARTITIONS, DEFAULT_REPLICATION_FACTOR,
                          "Customer behavior analytics"),
            new TopicConfig(Topics.MERCHANT_ANALYTICS, DEFAULT_PARTITIONS, DEFAULT_REPLICATION_FACTOR,
                          "Merchant analytics and risk scoring"),
            
            // ML and fraud detection topics - critical for fraud prevention
            new TopicConfig(Topics.FRAUD_DETECTION, HIGH_THROUGHPUT_PARTITIONS, DEFAULT_REPLICATION_FACTOR,
                          "Transactions sent to fraud detection service"),
            new TopicConfig(Topics.FRAUD_PREDICTIONS, DEFAULT_PARTITIONS, DEFAULT_REPLICATION_FACTOR,
                          "Fraud prediction results from ML service"),
            new TopicConfig(Topics.FRAUD_ALERTS, DEFAULT_PARTITIONS, DEFAULT_REPLICATION_FACTOR,
                          "High-risk fraud alerts requiring immediate action"),
            new TopicConfig(Topics.MODEL_TRAINING_DATA, DEFAULT_PARTITIONS, DEFAULT_REPLICATION_FACTOR,
                          "Training data for model retraining"),
            
            // Data quality topics - low throughput
            new TopicConfig(Topics.DATA_QUALITY_ALERTS, LOW_THROUGHPUT_PARTITIONS, DEFAULT_REPLICATION_FACTOR,
                          "Data quality issues and alerts"),
            new TopicConfig(Topics.DRIFT_DETECTION, LOW_THROUGHPUT_PARTITIONS, DEFAULT_REPLICATION_FACTOR,
                          "Model drift detection results"),
            
            // Monitoring topics - low throughput
            new TopicConfig(Topics.SYSTEM_METRICS, LOW_THROUGHPUT_PARTITIONS, DEFAULT_REPLICATION_FACTOR,
                          "System performance metrics"),
            new TopicConfig(Topics.PROCESSING_LOGS, LOW_THROUGHPUT_PARTITIONS, DEFAULT_REPLICATION_FACTOR,
                          "Application processing logs"),
            
            // Dead letter topics - low throughput
            new TopicConfig(Topics.DLQ_TRANSACTIONS, LOW_THROUGHPUT_PARTITIONS, DEFAULT_REPLICATION_FACTOR,
                          "Failed transaction processing messages"),
            new TopicConfig(Topics.DLQ_FRAUD_DETECTION, LOW_THROUGHPUT_PARTITIONS, DEFAULT_REPLICATION_FACTOR,
                          "Failed fraud detection messages")
        );
    }
    
    /**
     * Create all topics in Kafka cluster
     */
    public void createAllTopics(Properties kafkaProps) {
        logger.info("Creating Kafka topics for refactored architecture...");
        
        try (AdminClient adminClient = AdminClient.create(kafkaProps)) {
            List<TopicConfig> topicConfigs = getAllTopicConfigs();
            List<NewTopic> newTopics = topicConfigs.stream()
                .map(this::createNewTopic)
                .toList();
            
            CreateTopicsResult result = adminClient.createTopics(newTopics);
            
            // Wait for all topics to be created
            result.all().get();
            
            logger.info("Successfully created {} topics", newTopics.size());
            
            // Log each created topic
            for (TopicConfig config : topicConfigs) {
                logger.info("Created topic: {} (partitions: {}, replication: {}) - {}",
                          config.getName(), config.getPartitions(), config.getReplicationFactor(), 
                          config.getDescription());
            }
            
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Failed to create Kafka topics: {}", e.getMessage(), e);
            throw new RuntimeException("Topic creation failed", e);
        }
    }
    
    /**
     * Create a single NewTopic from TopicConfig
     */
    private NewTopic createNewTopic(TopicConfig config) {
        NewTopic newTopic = new NewTopic(config.getName(), config.getPartitions(), config.getReplicationFactor());
        
        // Add topic-specific configurations
        if (config.getName().contains("fraud") || config.getName().contains("alert")) {
            // Critical topics - shorter retention for faster processing
            newTopic.configs(java.util.Map.of(
                "retention.ms", "86400000", // 1 day
                "compression.type", "lz4",
                "min.insync.replicas", "1"
            ));
        } else if (config.getName().contains("analytics")) {
            // Analytics topics - longer retention for analysis
            newTopic.configs(java.util.Map.of(
                "retention.ms", "604800000", // 7 days
                "compression.type", "snappy",
                "cleanup.policy", "compact"
            ));
        } else if (config.getName().contains("dlq")) {
            // Dead letter queues - very long retention for debugging
            newTopic.configs(java.util.Map.of(
                "retention.ms", "2592000000", // 30 days
                "compression.type", "gzip"
            ));
        } else {
            // Default configurations
            newTopic.configs(java.util.Map.of(
                "retention.ms", "259200000", // 3 days
                "compression.type", "snappy"
            ));
        }
        
        return newTopic;
    }
    
    /**
     * Get topic configuration by name
     */
    public TopicConfig getTopicConfig(String topicName) {
        return getAllTopicConfigs().stream()
            .filter(config -> config.getName().equals(topicName))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown topic: " + topicName));
    }
    
    /**
     * Get all fraud detection related topics
     */
    public List<String> getFraudDetectionTopics() {
        return Arrays.asList(
            Topics.FRAUD_DETECTION,
            Topics.FRAUD_PREDICTIONS,
            Topics.FRAUD_ALERTS,
            Topics.MODEL_TRAINING_DATA,
            Topics.DRIFT_DETECTION
        );
    }
    
    /**
     * Get all analytics topics
     */
    public List<String> getAnalyticsTopics() {
        return Arrays.asList(
            Topics.TRANSACTION_ANALYTICS,
            Topics.CUSTOMER_ANALYTICS,
            Topics.MERCHANT_ANALYTICS
        );
    }
    
    /**
     * Get all monitoring topics
     */
    public List<String> getMonitoringTopics() {
        return Arrays.asList(
            Topics.SYSTEM_METRICS,
            Topics.PROCESSING_LOGS,
            Topics.DATA_QUALITY_ALERTS
        );
    }
    
    /**
     * Configuration class for Kafka topics
     */
    public static class TopicConfig {
        private final String name;
        private final int partitions;
        private final short replicationFactor;
        private final String description;
        
        public TopicConfig(String name, int partitions, short replicationFactor, String description) {
            this.name = name;
            this.partitions = partitions;
            this.replicationFactor = replicationFactor;
            this.description = description;
        }
        
        // Getters
        public String getName() { return name; }
        public int getPartitions() { return partitions; }
        public short getReplicationFactor() { return replicationFactor; }
        public String getDescription() { return description; }
        
        @Override
        public String toString() {
            return String.format("TopicConfig{name='%s', partitions=%d, replication=%d, description='%s'}",
                               name, partitions, replicationFactor, description);
        }
    }
} 