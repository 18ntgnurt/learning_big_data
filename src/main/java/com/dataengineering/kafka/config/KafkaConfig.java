package com.dataengineering.kafka.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

/**
 * KafkaConfig centralizes all Kafka configuration settings.
 * 
 * Key Learning Points:
 * - Producer configuration for performance and reliability
 * - Consumer configuration for processing guarantees
 * - Serialization/deserialization setup
 * - Connection and timeout settings
 * 
 * TODO: Implement the following methods:
 * 1. getProducerProperties() - Configure Kafka producer
 * 2. getConsumerProperties() - Configure Kafka consumer  
 * 3. getStreamsProperties() - Configure Kafka Streams
 */
public class KafkaConfig {
    
    // Kafka broker connection settings
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String SCHEMA_REGISTRY_URL = "http://localhost:8081";
    
    // Topic names - centralized topic management
    public static final String SALES_EVENTS_TOPIC = "sales-events";
    public static final String SALES_ANALYTICS_TOPIC = "sales-analytics";
    public static final String ANOMALY_ALERTS_TOPIC = "anomaly-alerts";
    public static final String HIGH_VALUE_SALES_TOPIC = "high-value-sales";
    public static final String WINDOWED_SALES_METRICS_TOPIC = "windowed-sales-metrics";
    
    /**
     * TODO: Implement producer configuration
     * 
     * Key settings to configure:
     * - bootstrap.servers: Kafka broker addresses
     * - key.serializer: How to serialize message keys
     * - value.serializer: How to serialize message values
     * - acks: Acknowledgment level (0, 1, or all)
     * - retries: Number of retry attempts
     * - batch.size: Batch size for performance
     * - linger.ms: Time to wait before sending batch
     * - buffer.memory: Total memory for buffering
     * 
     * @return Properties configured for Kafka producer
     */
    public static Properties getProducerProperties() {
        Properties props = new Properties();
        
        // TODO: Add bootstrap servers configuration
        // props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);

        // TODO: Add serializer configuration
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // TODO: Add reliability settings
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);

        // TODO: Add performance settings
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 5);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);

        return props;
    }
    
    /**
     * TODO: Implement consumer configuration
     * 
     * Key settings to configure:
     * - bootstrap.servers: Kafka broker addresses
     * - group.id: Consumer group identifier
     * - key.deserializer: How to deserialize message keys
     * - value.deserializer: How to deserialize message values
     * - auto.offset.reset: Where to start reading (earliest/latest)
     * - enable.auto.commit: Automatic offset commits
     * - auto.commit.interval.ms: Commit frequency
     * - session.timeout.ms: Session timeout
     * 
     * @param groupId Consumer group ID
     * @return Properties configured for Kafka consumer
     */
    public static Properties getConsumerProperties(String groupId) {
        Properties props = new Properties();
        
        // TODO: Add bootstrap servers configuration
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        
        // TODO: Add consumer group configuration
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        
        // TODO: Add deserializer configuration
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        
        // TODO: Add offset management
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false); // Manual commit for reliability
        
        // TODO: Add session management
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 10000);
        
        return props;
    }
    
    /**
     * TODO: Implement Kafka Streams configuration
     * 
     * Key settings to configure:
     * - application.id: Unique application identifier
     * - bootstrap.servers: Kafka broker addresses
     * - default.key.serde: Default key serialization
     * - default.value.serde: Default value serialization
     * - processing.guarantee: Processing semantics (at_least_once/exactly_once)
     * - commit.interval.ms: Commit frequency
     * 
     * @param applicationId Streams application ID
     * @return Properties configured for Kafka Streams
     */
    public static Properties getStreamsProperties(String applicationId) {
        Properties props = new Properties();
        
        // TODO: Add basic streams configuration
        props.put(org.apache.kafka.streams.StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
        props.put(org.apache.kafka.streams.StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        
        // TODO: Add serialization configuration
        props.put(org.apache.kafka.streams.StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, org.apache.kafka.common.serialization.Serdes.String().getClass());
        props.put(org.apache.kafka.streams.StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, org.apache.kafka.common.serialization.Serdes.String().getClass());
        
        // TODO: Add processing guarantees
        props.put(org.apache.kafka.streams.StreamsConfig.PROCESSING_GUARANTEE_CONFIG, org.apache.kafka.streams.StreamsConfig.EXACTLY_ONCE_V2);
        props.put(org.apache.kafka.streams.StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 10000);
        
        return props;
    }
    
    /**
     * TODO: Implement connection test method
     * 
     * This method should:
     * 1. Create a simple producer
     * 2. Try to connect to Kafka
     * 3. Return true if successful, false otherwise
     * 4. Handle any connection exceptions
     * 
     * @return true if Kafka is accessible, false otherwise
     */
    public static boolean testKafkaConnection() {
        // TODO: Implement connection test
        // Try to create a producer and get metadata
        // Return true if successful, false if any exception
        return false;
    }
} 