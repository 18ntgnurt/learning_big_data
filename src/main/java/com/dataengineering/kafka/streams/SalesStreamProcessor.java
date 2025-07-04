package com.dataengineering.kafka.streams;

import com.dataengineering.kafka.config.KafkaConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;
import java.util.Properties;

/**
 * SalesStreamProcessor implements real-time stream processing using Kafka Streams.
 * 
 * Key Learning Points:
 * - Stream processing topology
 * - Windowed aggregations  
 * - Stream transformations and filtering
 * - Stateful vs stateless operations
 * - Real-time analytics patterns
 * 
 * TODO: Implement the following methods:
 * 1. buildTopology() - Create processing topology
 * 2. filterHighValueSales() - Filter for large transactions
 * 3. aggregateSalesByRegion() - Real-time regional aggregations
 * 4. detectAnomalies() - Real-time anomaly detection
 * 5. start/stop() - Lifecycle management
 */
public class SalesStreamProcessor {
    
    private static final Logger logger = LoggerFactory.getLogger(SalesStreamProcessor.class);
    
    // Kafka Streams instance
    private KafkaStreams streams;
    
    // ObjectMapper for JSON processing
    private final ObjectMapper objectMapper;
    
    // Stream processing configuration
    private static final String APPLICATION_ID = "sales-stream-processor";
    private static final double HIGH_VALUE_THRESHOLD = 1000.0;
    
    /**
     * Constructor - Initialize stream processor
     */
    public SalesStreamProcessor() {
        // Initialize ObjectMapper
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules(); // For LocalDateTime support
        this.objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        // Build topology and create streams instance
        StreamsBuilder builder = new StreamsBuilder();
        buildTopology(builder);
        
        Properties props = KafkaConfig.getStreamsProperties(APPLICATION_ID);
        this.streams = new KafkaStreams(builder.build(), props);
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
        
        logger.info("✅ SalesStreamProcessor initialized");
    }
    
    /**
     * Build the stream processing topology
     * 
     * This method defines the complete data flow:
     * 1. Read from sales-events topic
     * 2. Parse JSON messages
     * 3. Apply various transformations:
     *    - Filter high-value sales
     *    - Aggregate by region/product
     *    - Detect anomalies
     *    - Calculate windowed metrics
     * 4. Send results to output topics
     * 
     * @param builder StreamsBuilder for creating topology
     */
    private void buildTopology(StreamsBuilder builder) {
        // Create input stream from sales events topic
        KStream<String, String> salesStream = builder.stream(KafkaConfig.SALES_EVENTS_TOPIC);
        
        // Parse JSON and filter invalid records
        KStream<String, JsonNode> parsedStream = salesStream
            .mapValues(this::parseJson)
            .filter((key, value) -> value != null);
        
        // Process high-value sales directly
        KStream<String, JsonNode> highValueStream = parsedStream
            .filter((key, sale) -> isHighValueSale(sale));
        processHighValueSales(highValueStream);
        
        // Process normal sales for aggregations
        KStream<String, JsonNode> normalStream = parsedStream
            .filter((key, sale) -> !isHighValueSale(sale));
        processNormalSales(normalStream);
        
        // Create windowed aggregations
        createWindowedAggregations(parsedStream);
        
        logger.info("📊 Stream processing topology built");
    }
    
    /**
     * Process high-value sales
     * 
     * Steps:
     * 1. Filter sales above threshold
     * 2. Enrich with additional data
     * 3. Send alerts for very large transactions
     * 4. Save to high-value-sales topic
     * 
     * @param highValueStream Stream of high-value sales
     */
    private void processHighValueSales(KStream<String, JsonNode> highValueStream) {
        highValueStream
            .filter((key, sale) -> getAmount(sale) > HIGH_VALUE_THRESHOLD)
            .peek((key, sale) -> logger.info("🚨 High-value sale detected: {} - ${}", 
                                              key, getAmount(sale)))
            .mapValues(this::enrichSalesData)
            .to(KafkaConfig.HIGH_VALUE_SALES_TOPIC, Produced.with(Serdes.String(), Serdes.String()));
    }
    
    /**
     * Process normal sales for analytics
     * 
     * Steps:
     * 1. Group by product category
     * 2. Calculate running totals
     * 3. Update analytics metrics
     * 4. Send to analytics topic
     * 
     * @param normalStream Stream of normal sales
     */
    private void processNormalSales(KStream<String, JsonNode> normalStream) {
        normalStream
            .groupBy((key, sale) -> getProductCategory(sale))
            .aggregate(
                () -> createEmptyAggregation(),
                (category, sale, aggregate) -> updateAggregation(aggregate, sale),
                Materialized.with(Serdes.String(), Serdes.String())
            )
            .toStream()
            .to(KafkaConfig.SALES_ANALYTICS_TOPIC, Produced.with(Serdes.String(), Serdes.String()));
    }
    
    /**
     * Create windowed aggregations
     * 
     * This demonstrates time-based aggregations:
     * 1. 5-minute tumbling windows
     * 2. Sales count and total amount per window
     * 3. Regional breakdown
     * 4. Real-time dashboard updates
     * 
     * @param salesStream Input sales stream
     */
    private void createWindowedAggregations(KStream<String, JsonNode> salesStream) {
        // Create 5-minute tumbling windows
        TimeWindows timeWindows = TimeWindows.of(Duration.ofMinutes(5));
        
        // Aggregate sales by region and time window
        salesStream
            .groupBy((key, sale) -> getRegion(sale))
            .windowedBy(timeWindows)
            .aggregate(
                () -> createWindowedMetrics(),
                (region, sale, metrics) -> updateWindowedMetrics(metrics, sale),
                Materialized.with(Serdes.String(), Serdes.String())
            )
            .toStream()
            .map((windowedKey, metrics) -> KeyValue.pair(
                windowedKey.key() + "@" + windowedKey.window().start(),
                metrics
            ))
            .to(KafkaConfig.WINDOWED_SALES_METRICS_TOPIC, Produced.with(Serdes.String(), Serdes.String()));
    }
    
    /**
     * Parse JSON with error handling
     * 
     * Steps:
     * 1. Parse JSON string to JsonNode
     * 2. Handle parsing exceptions gracefully
     * 3. Log parsing errors
     * 4. Return null for invalid JSON
     * 
     * @param jsonString JSON string to parse
     * @return JsonNode or null if parsing fails
     */
    private JsonNode parseJson(String jsonString) {
        try {
            return objectMapper.readTree(jsonString);
        } catch (Exception e) {
            logger.warn("⚠️ Failed to parse JSON: {}", jsonString, e);
            return null;
        }
    }
    
    /**
     * Check if sale is high-value
     * 
     * Steps:
     * 1. Extract amount from JsonNode
     * 2. Compare with threshold
     * 3. Handle missing or invalid amount fields
     * 
     * @param sale Sales record as JsonNode
     * @return true if high-value sale
     */
    private boolean isHighValueSale(JsonNode sale) {
        double amount = sale.path("totalAmount").asDouble(0.0);
        return amount > HIGH_VALUE_THRESHOLD;
    }
    
    /**
     * Enrich sales data with additional metadata
     * 
     * Steps:
     * 1. Add timestamp
     * 2. Add enrichment metadata
     * 3. Convert back to JSON string
     * 4. Handle enrichment errors
     * 
     * @param sale Original sales data
     * @return Enriched sales data as JSON string
     */
    private String enrichSalesData(JsonNode sale) {
        try {
            ObjectNode enriched = sale.deepCopy();
            enriched.put("processedAt", System.currentTimeMillis());
            enriched.put("alertLevel", "HIGH_VALUE");
            enriched.put("streamProcessor", "SalesStreamProcessor");
            return objectMapper.writeValueAsString(enriched);
        } catch (Exception e) {
            logger.error("❌ Failed to enrich sales data", e);
            return sale.toString();
        }
    }
    
    /**
     * Helper methods for extracting data from JsonNode
     */
    
    private double getAmount(JsonNode sale) {
        return sale.path("totalAmount").asDouble(0.0);
    }
    
    private String getRegion(JsonNode sale) {
        return sale.path("storeLocation").asText("UNKNOWN");
    }
    
    private String getProductCategory(JsonNode sale) {
        return sale.path("productCategory").asText("UNKNOWN");
    }
    
    /**
     * Create empty aggregation object
     */
    private String createEmptyAggregation() {
        try {
            ObjectNode aggregation = objectMapper.createObjectNode();
            aggregation.put("count", 0);
            aggregation.put("totalAmount", 0.0);
            aggregation.put("lastUpdated", System.currentTimeMillis());
            return objectMapper.writeValueAsString(aggregation);
        } catch (Exception e) {
            logger.error("❌ Failed to create empty aggregation", e);
            return "{}";
        }
    }
    
    /**
     * Update aggregation with new sale
     */
    private String updateAggregation(String currentAggregation, JsonNode sale) {
        try {
            JsonNode current = objectMapper.readTree(currentAggregation);
            ObjectNode updated = current.deepCopy();
            
            int newCount = current.path("count").asInt(0) + 1;
            double newTotal = current.path("totalAmount").asDouble(0.0) + getAmount(sale);
            
            updated.put("count", newCount);
            updated.put("totalAmount", newTotal);
            updated.put("lastUpdated", System.currentTimeMillis());
            
            return objectMapper.writeValueAsString(updated);
        } catch (Exception e) {
            logger.error("❌ Failed to update aggregation", e);
            return currentAggregation;
        }
    }
    
    /**
     * Create windowed metrics object
     */
    private String createWindowedMetrics() {
        try {
            ObjectNode metrics = objectMapper.createObjectNode();
            metrics.put("salesCount", 0);
            metrics.put("totalRevenue", 0.0);
            metrics.put("avgSaleAmount", 0.0);
            metrics.put("windowStart", System.currentTimeMillis());
            return objectMapper.writeValueAsString(metrics);
        } catch (Exception e) {
            logger.error("❌ Failed to create windowed metrics", e);
            return "{}";
        }
    }
    
    /**
     * Update windowed metrics
     */
    private String updateWindowedMetrics(String currentMetrics, JsonNode sale) {
        try {
            JsonNode current = objectMapper.readTree(currentMetrics);
            ObjectNode updated = current.deepCopy();
            
            int newCount = current.path("salesCount").asInt(0) + 1;
            double newTotal = current.path("totalRevenue").asDouble(0.0) + getAmount(sale);
            double avgAmount = newTotal / newCount;
            
            updated.put("salesCount", newCount);
            updated.put("totalRevenue", newTotal);
            updated.put("avgSaleAmount", avgAmount);
            
            return objectMapper.writeValueAsString(updated);
        } catch (Exception e) {
            logger.error("❌ Failed to update windowed metrics", e);
            return currentMetrics;
        }
    }
    
    /**
     * Start the stream processor
     */
    public void start() {
        try {
            streams.start();
            
            // Wait for streams to be running
            while (streams.state() != KafkaStreams.State.RUNNING) {
                Thread.sleep(100);
            }
            
            logger.info("🚀 Sales stream processor started successfully");
            
        } catch (Exception e) {
            logger.error("❌ Failed to start stream processor", e);
        }
    }
    
    /**
     * Graceful shutdown
     */
    public void stop() {
        if (streams != null) {
            logger.info("🛑 Stopping sales stream processor...");
            streams.close(Duration.ofSeconds(10));
            logger.info("✅ Stream processor stopped");
        }
    }
    
    /**
     * Get current stream state
     * 
     * @return Current state of the stream processor
     */
    public String getState() {
        return streams != null ? streams.state().toString() : "NOT_INITIALIZED";
    }
} 