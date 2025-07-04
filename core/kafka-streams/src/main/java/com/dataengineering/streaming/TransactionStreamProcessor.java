package com.dataengineering.streaming;

import com.dataengineering.model.Transaction;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Transaction Stream Processor
 * 
 * Comprehensive Kafka Streams processing that handles:
 * - Real-time transaction validation and enrichment
 * - High-value transaction detection and alerting
 * - Windowed analytics and aggregations
 * - Fraud detection preprocessing
 * - Performance monitoring and health checks
 */
@Component
public class TransactionStreamProcessor {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionStreamProcessor.class);
    
    private KafkaStreams streams;
    private final ObjectMapper objectMapper;
    
    // Metrics
    private final AtomicLong processedCount = new AtomicLong(0);
    private final AtomicLong highValueCount = new AtomicLong(0);
    private final AtomicLong suspiciousCount = new AtomicLong(0);
    private final AtomicLong errorCount = new AtomicLong(0);
    
    // Configuration
    private static final String APPLICATION_ID = "transaction-stream-processor";
    private static final double HIGH_VALUE_THRESHOLD = 1000.0;
    private static final double SUSPICIOUS_THRESHOLD = 5000.0;
    
    // Topic names following new convention
    private static final String INPUT_TOPIC = "transactions-raw-v1";
    private static final String VALIDATED_TOPIC = "transactions-validated-v1";
    private static final String ENRICHED_TOPIC = "transactions-enriched-v1";
    private static final String HIGH_VALUE_TOPIC = "transactions-high-value-v1";
    private static final String SUSPICIOUS_TOPIC = "transactions-suspicious-v1";
    private static final String ANALYTICS_TOPIC = "analytics-aggregated-v1";
    private static final String MONITORING_TOPIC = "monitoring-metrics-v1";
    private static final String DLQ_TOPIC = "transactions-dlq-v1";
    
    public TransactionStreamProcessor() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules();
        logger.info("Transaction Stream Processor initialized");
    }
    
    @PostConstruct
    public void start() {
        try {
            Properties props = createStreamProperties();
            StreamsBuilder builder = new StreamsBuilder();
            
            buildTopology(builder);
            
            streams = new KafkaStreams(builder.build(), props);
            
            // Add state change listener
            streams.setStateListener((newState, oldState) -> {
                logger.info("Stream state changed from {} to {}", oldState, newState);
            });
            
            // Add exception handler
            streams.setUncaughtExceptionHandler((exception) -> {
                logger.error("Uncaught exception in stream: {}", exception.getMessage(), exception);
                return org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.REPLACE_THREAD;
            });
            
            streams.start();
            logger.info("Transaction Stream Processor started successfully");
            
        } catch (Exception e) {
            logger.error("Failed to start Transaction Stream Processor", e);
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Build the complete stream processing topology
     */
    private void buildTopology(StreamsBuilder builder) {
        // Main input stream
        KStream<String, String> rawTransactions = builder.stream(INPUT_TOPIC);
        
        // Parse and validate transactions
        KStream<String, JsonNode> parsedStream = rawTransactions
                .mapValues(this::parseTransaction)
                .filter((key, value) -> value != null)
                .peek((key, value) -> processedCount.incrementAndGet());
        
        // Validate transactions
        KStream<String, JsonNode> validatedStream = parsedStream
                .mapValues(this::validateAndEnrichTransaction)
                .filter((key, value) -> value != null);
        
        // Send validated transactions
        validatedStream
                .mapValues(JsonNode::toString)
                .to(VALIDATED_TOPIC);
        
        // Branch processing based on transaction characteristics
        Predicate<String, JsonNode> isHighValue = (key, tx) -> getAmount(tx) >= HIGH_VALUE_THRESHOLD;
        Predicate<String, JsonNode> isSuspicious = (key, tx) -> getAmount(tx) >= SUSPICIOUS_THRESHOLD;
        Predicate<String, JsonNode> isNormal = (key, tx) -> getAmount(tx) < HIGH_VALUE_THRESHOLD;
        
        KStream<String, JsonNode>[] branches = validatedStream.branch(
                isSuspicious, isHighValue, isNormal
        );
        
        KStream<String, JsonNode> suspiciousTransactions = branches[0];
        KStream<String, JsonNode> highValueTransactions = branches[1];
        KStream<String, JsonNode> normalTransactions = branches[2];
        
        // Process suspicious transactions
        processSuspiciousTransactions(suspiciousTransactions);
        
        // Process high-value transactions
        processHighValueTransactions(highValueTransactions);
        
        // Process normal transactions
        processNormalTransactions(normalTransactions);
        
        // Create enriched stream for all transactions
        KStream<String, JsonNode> enrichedStream = validatedStream
                .mapValues(this::enrichTransaction);
        
        enrichedStream
                .mapValues(JsonNode::toString)
                .to(ENRICHED_TOPIC);
        
        // Create real-time aggregations
        createRealTimeAggregations(enrichedStream);
        
        // Create monitoring metrics
        createMonitoringMetrics(enrichedStream);
        
        logger.info("Stream topology built successfully");
    }
    
    /**
     * Process suspicious transactions (>$5000)
     */
    private void processSuspiciousTransactions(KStream<String, JsonNode> suspiciousStream) {
        suspiciousStream
                .peek((key, tx) -> {
                    suspiciousCount.incrementAndGet();
                    logger.warn("Suspicious transaction detected: {} - ${}", 
                              tx.get("transaction_id").asText(), getAmount(tx));
                })
                .mapValues(this::addSuspiciousFlags)
                .mapValues(JsonNode::toString)
                .to(SUSPICIOUS_TOPIC);
    }
    
    /**
     * Process high-value transactions ($1000-$5000)
     */
    private void processHighValueTransactions(KStream<String, JsonNode> highValueStream) {
        highValueStream
                .peek((key, tx) -> {
                    highValueCount.incrementAndGet();
                    logger.info("High-value transaction detected: {} - ${}", 
                              tx.get("transaction_id").asText(), getAmount(tx));
                })
                .mapValues(this::addHighValueFlags)
                .mapValues(JsonNode::toString)
                .to(HIGH_VALUE_TOPIC);
    }
    
    /**
     * Process normal transactions (<$1000)
     */
    private void processNormalTransactions(KStream<String, JsonNode> normalStream) {
        normalStream
                .mapValues(this::addNormalFlags)
                .mapValues(JsonNode::toString)
                .to(VALIDATED_TOPIC);
    }
    
    /**
     * Create real-time aggregations with windowing
     */
    private void createRealTimeAggregations(KStream<String, JsonNode> enrichedStream) {
        // 5-minute tumbling windows for real-time analytics
        TimeWindows timeWindow = TimeWindows.of(Duration.ofMinutes(5))
                .advanceBy(Duration.ofMinutes(1)); // 1-minute advance for overlapping windows
        
        // Aggregate by merchant
        enrichedStream
                .groupBy((key, tx) -> tx.get("merchant_id").asText())
                .windowedBy(timeWindow)
                .aggregate(
                        this::initializeAggregation,
                        this::updateMerchantAggregation,
                        Materialized.with(Serdes.String(), Serdes.String())
                )
                .toStream()
                .map((windowedKey, aggregation) -> KeyValue.pair(
                        "merchant:" + windowedKey.key() + ":" + windowedKey.window().start(),
                        aggregation
                ))
                .to(ANALYTICS_TOPIC);
        
        // Aggregate by customer
        enrichedStream
                .groupBy((key, tx) -> tx.get("customer_id").asText())
                .windowedBy(timeWindow)
                .aggregate(
                        this::initializeAggregation,
                        this::updateCustomerAggregation,
                        Materialized.with(Serdes.String(), Serdes.String())
                )
                .toStream()
                .map((windowedKey, aggregation) -> KeyValue.pair(
                        "customer:" + windowedKey.key() + ":" + windowedKey.window().start(),
                        aggregation
                ))
                .to(ANALYTICS_TOPIC);
        
        // Aggregate by location
        enrichedStream
                .groupBy((key, tx) -> tx.get("location").asText("unknown"))
                .windowedBy(timeWindow)
                .aggregate(
                        this::initializeAggregation,
                        this::updateLocationAggregation,
                        Materialized.with(Serdes.String(), Serdes.String())
                )
                .toStream()
                .map((windowedKey, aggregation) -> KeyValue.pair(
                        "location:" + windowedKey.key() + ":" + windowedKey.window().start(),
                        aggregation
                ))
                .to(ANALYTICS_TOPIC);
    }
    
    /**
     * Create monitoring metrics stream
     */
    private void createMonitoringMetrics(KStream<String, JsonNode> enrichedStream) {
        // Create health metrics every minute
        enrichedStream
                .groupByKey()
                .windowedBy(TimeWindows.of(Duration.ofMinutes(1)))
                .aggregate(
                        this::initializeHealthMetrics,
                        this::updateHealthMetrics,
                        Materialized.with(Serdes.String(), Serdes.String())
                )
                .toStream()
                .map((windowedKey, metrics) -> KeyValue.pair(
                        "health:processor:" + windowedKey.window().start(),
                        metrics
                ))
                .to(MONITORING_TOPIC);
    }
    
    /**
     * Parse transaction JSON with error handling
     */
    private JsonNode parseTransaction(String transactionJson) {
        try {
            return objectMapper.readTree(transactionJson);
        } catch (Exception e) {
            logger.warn("Failed to parse transaction JSON: {}", transactionJson, e);
            errorCount.incrementAndGet();
            // Send to dead letter queue
            sendToDeadLetterQueue(transactionJson, "JSON_PARSE_ERROR", e.getMessage());
            return null;
        }
    }
    
    /**
     * Validate and enrich transaction with business rules
     */
    private JsonNode validateAndEnrichTransaction(JsonNode transaction) {
        try {
            ObjectNode enriched = transaction.deepCopy();
            
            // Validate required fields
            if (!hasRequiredFields(transaction)) {
                sendToDeadLetterQueue(transaction.toString(), "VALIDATION_ERROR", "Missing required fields");
                return null;
            }
            
            // Add validation timestamp
            enriched.put("validated_at", System.currentTimeMillis());
            enriched.put("validation_status", "PASSED");
            
            return enriched;
            
        } catch (Exception e) {
            logger.warn("Failed to validate transaction: {}", transaction, e);
            errorCount.incrementAndGet();
            sendToDeadLetterQueue(transaction.toString(), "VALIDATION_ERROR", e.getMessage());
            return null;
        }
    }
    
    /**
     * Enrich transaction with additional metadata
     */
    private JsonNode enrichTransaction(JsonNode transaction) {
        ObjectNode enriched = transaction.deepCopy();
        
        try {
            // Add processing metadata
            enriched.put("enriched_at", System.currentTimeMillis());
            enriched.put("processor_version", "1.0.0");
            
            // Add derived fields
            double amount = getAmount(transaction);
            enriched.put("amount_category", categorizeAmount(amount));
            enriched.put("risk_level", calculateRiskLevel(transaction));
            enriched.put("hour_of_day", java.time.LocalTime.now().getHour());
            enriched.put("day_of_week", java.time.LocalDate.now().getDayOfWeek().getValue());
            
            // Add merchant metadata (simplified)
            String merchantId = transaction.get("merchant_id").asText();
            enriched.put("merchant_risk_score", calculateMerchantRisk(merchantId));
            
            return enriched;
            
        } catch (Exception e) {
            logger.warn("Failed to enrich transaction: {}", transaction, e);
            return transaction; // Return original if enrichment fails
        }
    }
    
    /**
     * Add flags for suspicious transactions
     */
    private JsonNode addSuspiciousFlags(JsonNode transaction) {
        ObjectNode flagged = transaction.deepCopy();
        flagged.put("is_suspicious", true);
        flagged.put("requires_manual_review", true);
        flagged.put("alert_level", "HIGH");
        flagged.put("flagged_at", System.currentTimeMillis());
        return flagged;
    }
    
    /**
     * Add flags for high-value transactions
     */
    private JsonNode addHighValueFlags(JsonNode transaction) {
        ObjectNode flagged = transaction.deepCopy();
        flagged.put("is_high_value", true);
        flagged.put("requires_review", true);
        flagged.put("alert_level", "MEDIUM");
        flagged.put("flagged_at", System.currentTimeMillis());
        return flagged;
    }
    
    /**
     * Add flags for normal transactions
     */
    private JsonNode addNormalFlags(JsonNode transaction) {
        ObjectNode flagged = transaction.deepCopy();
        flagged.put("is_normal", true);
        flagged.put("alert_level", "LOW");
        flagged.put("processed_at", System.currentTimeMillis());
        return flagged;
    }
    
    // Aggregation methods
    private String initializeAggregation() {
        try {
            ObjectNode aggregation = objectMapper.createObjectNode();
            aggregation.put("count", 0);
            aggregation.put("total_amount", 0.0);
            aggregation.put("avg_amount", 0.0);
            aggregation.put("min_amount", Double.MAX_VALUE);
            aggregation.put("max_amount", 0.0);
            aggregation.put("window_start", System.currentTimeMillis());
            return objectMapper.writeValueAsString(aggregation);
        } catch (Exception e) {
            logger.error("Failed to initialize aggregation", e);
            return "{}";
        }
    }
    
    private String updateMerchantAggregation(String key, JsonNode transaction, String currentAggregation) {
        return updateAggregation(transaction, currentAggregation, "merchant");
    }
    
    private String updateCustomerAggregation(String key, JsonNode transaction, String currentAggregation) {
        return updateAggregation(transaction, currentAggregation, "customer");
    }
    
    private String updateLocationAggregation(String key, JsonNode transaction, String currentAggregation) {
        return updateAggregation(transaction, currentAggregation, "location");
    }
    
    private String updateAggregation(JsonNode transaction, String currentAggregation, String type) {
        try {
            JsonNode current = objectMapper.readTree(currentAggregation);
            ObjectNode updated = current.deepCopy();
            
            double amount = getAmount(transaction);
            int count = current.get("count").asInt() + 1;
            double totalAmount = current.get("total_amount").asDouble() + amount;
            double avgAmount = totalAmount / count;
            double minAmount = Math.min(current.get("min_amount").asDouble(), amount);
            double maxAmount = Math.max(current.get("max_amount").asDouble(), amount);
            
            updated.put("count", count);
            updated.put("total_amount", totalAmount);
            updated.put("avg_amount", avgAmount);
            updated.put("min_amount", minAmount);
            updated.put("max_amount", maxAmount);
            updated.put("aggregation_type", type);
            updated.put("last_updated", System.currentTimeMillis());
            
            return objectMapper.writeValueAsString(updated);
            
        } catch (Exception e) {
            logger.error("Failed to update aggregation", e);
            return currentAggregation;
        }
    }
    
    private String initializeHealthMetrics() {
        try {
            ObjectNode metrics = objectMapper.createObjectNode();
            metrics.put("processed_count", 0);
            metrics.put("error_count", 0);
            metrics.put("high_value_count", 0);
            metrics.put("suspicious_count", 0);
            metrics.put("window_start", System.currentTimeMillis());
            return objectMapper.writeValueAsString(metrics);
        } catch (Exception e) {
            logger.error("Failed to initialize health metrics", e);
            return "{}";
        }
    }
    
    private String updateHealthMetrics(String key, JsonNode transaction, String currentMetrics) {
        try {
            JsonNode current = objectMapper.readTree(currentMetrics);
            ObjectNode updated = current.deepCopy();
            
            updated.put("processed_count", current.get("processed_count").asInt() + 1);
            updated.put("error_count", errorCount.get());
            updated.put("high_value_count", highValueCount.get());
            updated.put("suspicious_count", suspiciousCount.get());
            updated.put("last_updated", System.currentTimeMillis());
            updated.put("processor_health", "HEALTHY");
            
            return objectMapper.writeValueAsString(updated);
            
        } catch (Exception e) {
            logger.error("Failed to update health metrics", e);
            return currentMetrics;
        }
    }
    
    // Helper methods
    private double getAmount(JsonNode transaction) {
        return transaction.get("amount").asDouble(0.0);
    }
    
    private boolean hasRequiredFields(JsonNode transaction) {
        return transaction.has("transaction_id") &&
               transaction.has("customer_id") &&
               transaction.has("amount") &&
               transaction.get("amount").asDouble() > 0;
    }
    
    private String categorizeAmount(double amount) {
        if (amount >= SUSPICIOUS_THRESHOLD) return "SUSPICIOUS";
        if (amount >= HIGH_VALUE_THRESHOLD) return "HIGH";
        if (amount >= 100) return "MEDIUM";
        return "LOW";
    }
    
    private String calculateRiskLevel(JsonNode transaction) {
        double amount = getAmount(transaction);
        // Simplified risk calculation
        if (amount >= SUSPICIOUS_THRESHOLD) return "HIGH";
        if (amount >= HIGH_VALUE_THRESHOLD) return "MEDIUM";
        return "LOW";
    }
    
    private double calculateMerchantRisk(String merchantId) {
        // Simplified merchant risk calculation
        return Math.abs(merchantId.hashCode() % 100) / 100.0;
    }
    
    private void sendToDeadLetterQueue(String message, String errorType, String errorDetails) {
        try {
            ObjectNode dlqMessage = objectMapper.createObjectNode();
            dlqMessage.put("original_message", message);
            dlqMessage.put("error_type", errorType);
            dlqMessage.put("error_details", errorDetails);
            dlqMessage.put("timestamp", System.currentTimeMillis());
            
            // In a real implementation, we would send this to DLQ topic
            logger.error("Dead letter queue message: {}", dlqMessage.toString());
            
        } catch (Exception e) {
            logger.error("Failed to create DLQ message", e);
        }
    }
    
    /**
     * Create stream properties
     */
    private Properties createStreamProperties() {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, APPLICATION_ID);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE_V2);
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 10000);
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 16 * 1024 * 1024L); // 16MB
        props.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, 2);
        
        return props;
    }
    
    /**
     * Get current processing metrics
     */
    public ProcessingMetrics getMetrics() {
        return new ProcessingMetrics(
                processedCount.get(),
                highValueCount.get(),
                suspiciousCount.get(),
                errorCount.get(),
                streams != null ? streams.state().name() : "NOT_STARTED"
        );
    }
    
    @PreDestroy
    public void stop() {
        if (streams != null) {
            logger.info("Stopping Transaction Stream Processor...");
            streams.close(Duration.ofSeconds(30));
            logger.info("Transaction Stream Processor stopped");
        }
    }
    
    /**
     * Processing metrics data class
     */
    public static class ProcessingMetrics {
        private final long processedCount;
        private final long highValueCount;
        private final long suspiciousCount;
        private final long errorCount;
        private final String streamState;
        
        public ProcessingMetrics(long processedCount, long highValueCount, 
                               long suspiciousCount, long errorCount, String streamState) {
            this.processedCount = processedCount;
            this.highValueCount = highValueCount;
            this.suspiciousCount = suspiciousCount;
            this.errorCount = errorCount;
            this.streamState = streamState;
        }
        
        // Getters
        public long getProcessedCount() { return processedCount; }
        public long getHighValueCount() { return highValueCount; }
        public long getSuspiciousCount() { return suspiciousCount; }
        public long getErrorCount() { return errorCount; }
        public String getStreamState() { return streamState; }
        public double getErrorRate() {
            return processedCount > 0 ? (double) errorCount / processedCount : 0.0;
        }
    }
} 