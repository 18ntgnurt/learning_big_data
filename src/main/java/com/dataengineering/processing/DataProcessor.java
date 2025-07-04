package com.dataengineering.processing;

import com.dataengineering.model.SalesRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * DataProcessor demonstrates data transformation and processing operations.
 * This class covers essential data processing patterns in data engineering.
 * 
 * Key Learning Points:
 * - Data filtering and transformation
 * - Aggregation operations
 * - Data grouping and statistics
 * - Data quality and cleansing
 * - Stream processing with Java 8+ features
 * - Performance optimization techniques
 */
public class DataProcessor {
    
    private static final Logger logger = LoggerFactory.getLogger(DataProcessor.class);
    
    /**
     * Filters sales records based on various criteria.
     * This demonstrates data filtering in ETL pipelines.
     * 
     * @param records Input sales records
     * @param minAmount Minimum total amount filter
     * @param startDate Start date filter (inclusive)
     * @param endDate End date filter (inclusive)
     * @param productCategory Product category filter (null to ignore)
     * @return Filtered list of sales records
     */
    public List<SalesRecord> filterRecords(List<SalesRecord> records, 
                                         BigDecimal minAmount,
                                         LocalDate startDate, 
                                         LocalDate endDate,
                                         String productCategory) {
        
        logger.info("🔍 Filtering {} records with criteria: minAmount={}, dateRange={} to {}, category={}", 
                   records.size(), minAmount, startDate, endDate, productCategory);
        
        List<SalesRecord> filteredRecords = records.stream()
            .filter(record -> {
                // Amount filter
                if (minAmount != null && record.getTotalAmount().compareTo(minAmount) < 0) {
                    return false;
                }
                
                // Date range filter
                LocalDate recordDate = record.getSaleDate().toLocalDate();
                if (startDate != null && recordDate.isBefore(startDate)) {
                    return false;
                }
                if (endDate != null && recordDate.isAfter(endDate)) {
                    return false;
                }
                
                // Category filter
                if (productCategory != null && !productCategory.equalsIgnoreCase(record.getProductCategory())) {
                    return false;
                }
                
                return true;
            })
            .collect(Collectors.toList());
        
        logger.info("✅ Filtered to {} records", filteredRecords.size());
        return filteredRecords;
    }
    
    /**
     * Groups sales records by product category and calculates aggregates.
     * This demonstrates group-by operations and aggregations.
     * 
     * @param records Input sales records
     * @return Map of category to aggregated sales data
     */
    public Map<String, CategorySummary> groupByCategory(List<SalesRecord> records) {
        logger.info("📊 Grouping {} records by product category...", records.size());
        
        Map<String, CategorySummary> categoryMap = records.stream()
            .filter(record -> record.getProductCategory() != null)
            .collect(Collectors.groupingBy(
                SalesRecord::getProductCategory,
                Collectors.collectingAndThen(
                    Collectors.toList(),
                    this::calculateCategorySummary
                )
            ));
        
        logger.info("✅ Grouped into {} categories", categoryMap.size());
        
        // Log summary for each category
        categoryMap.forEach((category, summary) -> {
            logger.info("📈 {}: {} transactions, total: ${}, avg: ${}",
                       category, summary.getTransactionCount(), 
                       summary.getTotalAmount(), summary.getAverageAmount());
        });
        
        return categoryMap;
    }
    
    /**
     * Groups sales records by customer and calculates customer metrics.
     * This demonstrates customer analytics patterns.
     * 
     * @param records Input sales records
     * @return Map of customer ID to customer summary
     */
    public Map<String, CustomerSummary> groupByCustomer(List<SalesRecord> records) {
        logger.info("👥 Grouping {} records by customer...", records.size());
        
        Map<String, CustomerSummary> customerMap = records.stream()
            .filter(record -> record.getCustomerId() != null)
            .collect(Collectors.groupingBy(
                SalesRecord::getCustomerId,
                Collectors.collectingAndThen(
                    Collectors.toList(),
                    this::calculateCustomerSummary
                )
            ));
        
        logger.info("✅ Analyzed {} customers", customerMap.size());
        return customerMap;
    }
    
    /**
     * Calculates daily sales aggregates.
     * This demonstrates time-based aggregations.
     * 
     * @param records Input sales records
     * @return Map of date to daily sales summary
     */
    public Map<LocalDate, DailySummary> groupByDate(List<SalesRecord> records) {
        logger.info("📅 Grouping {} records by date...", records.size());
        
        Map<LocalDate, DailySummary> dailyMap = records.stream()
            .filter(record -> record.getSaleDate() != null)
            .collect(Collectors.groupingBy(
                record -> record.getSaleDate().toLocalDate(),
                Collectors.collectingAndThen(
                    Collectors.toList(),
                    this::calculateDailySummary
                )
            ));
        
        logger.info("✅ Aggregated data for {} days", dailyMap.size());
        return dailyMap;
    }
    
    /**
     * Identifies top-selling products by quantity or revenue.
     * This demonstrates ranking and sorting operations.
     * 
     * @param records Input sales records
     * @param limit Number of top products to return
     * @param byRevenue If true, rank by revenue; if false, rank by quantity
     * @return List of top-selling products
     */
    public List<ProductSummary> getTopProducts(List<SalesRecord> records, int limit, boolean byRevenue) {
        logger.info("🏆 Finding top {} products by {}", limit, byRevenue ? "revenue" : "quantity");
        
        Map<String, ProductSummary> productMap = records.stream()
            .filter(record -> record.getProductName() != null)
            .collect(Collectors.groupingBy(
                SalesRecord::getProductName,
                Collectors.collectingAndThen(
                    Collectors.toList(),
                    this::calculateProductSummary
                )
            ));
        
        Comparator<ProductSummary> comparator = byRevenue 
            ? Comparator.comparing(ProductSummary::getTotalRevenue).reversed()
            : Comparator.comparing(ProductSummary::getTotalQuantity).reversed();
        
        List<ProductSummary> topProducts = productMap.values().stream()
            .sorted(comparator)
            .limit(limit)
            .collect(Collectors.toList());
        
        logger.info("✅ Top products identified");
        topProducts.forEach(product -> {
            if (byRevenue) {
                logger.info("🥇 {}: ${} revenue ({} units)",
                           product.getProductName(), product.getTotalRevenue(), product.getTotalQuantity());
            } else {
                logger.info("🥇 {}: {} units (${} revenue)",
                           product.getProductName(), product.getTotalQuantity(), product.getTotalRevenue());
            }
        });
        
        return topProducts;
    }
    
    /**
     * Detects anomalies in sales data.
     * This demonstrates data quality and anomaly detection patterns.
     * 
     * @param records Input sales records
     * @return List of records that appear to be anomalies
     */
    public List<SalesRecord> detectAnomalies(List<SalesRecord> records) {
        logger.info("🔍 Detecting anomalies in {} records...", records.size());
        
        if (records.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Calculate statistics for anomaly detection
        DoubleSummaryStatistics amountStats = records.stream()
            .filter(record -> record.getTotalAmount() != null)
            .mapToDouble(record -> record.getTotalAmount().doubleValue())
            .summaryStatistics();
        
        DoubleSummaryStatistics quantityStats = records.stream()
            .filter(record -> record.getQuantity() != null)
            .mapToDouble(SalesRecord::getQuantity)
            .summaryStatistics();
        
        // Define thresholds (using simple statistical approach)
        double amountThreshold = amountStats.getAverage() + (3 * calculateStandardDeviation(records, "amount"));
        double quantityThreshold = quantityStats.getAverage() + (3 * calculateStandardDeviation(records, "quantity"));
        
        List<SalesRecord> anomalies = records.stream()
            .filter(record -> isAnomaly(record, amountThreshold, quantityThreshold))
            .collect(Collectors.toList());
        
        logger.info("⚠️ Detected {} potential anomalies", anomalies.size());
        anomalies.forEach(anomaly -> {
            logger.warn("🚨 Anomaly: {} - Amount: ${}, Quantity: {}",
                       anomaly.getTransactionId(), anomaly.getTotalAmount(), anomaly.getQuantity());
        });
        
        return anomalies;
    }
    
    /**
     * Transforms records to include calculated fields.
     * This demonstrates data enrichment and transformation.
     * 
     * @param records Input sales records
     * @return Enhanced records with calculated fields
     */
    public List<SalesRecord> enrichRecords(List<SalesRecord> records) {
        logger.info("🔧 Enriching {} records with calculated fields...", records.size());
        
        // For this example, we'll ensure total amount is calculated correctly
        // In a real scenario, you might add more complex enrichments
        records.forEach(record -> {
            if (record.getQuantity() != null && record.getUnitPrice() != null) {
                BigDecimal calculatedTotal = record.getUnitPrice()
                    .multiply(BigDecimal.valueOf(record.getQuantity()))
                    .setScale(2, RoundingMode.HALF_UP);
                
                // Update if different (data quality check)
                if (record.getTotalAmount() == null || 
                    record.getTotalAmount().compareTo(calculatedTotal) != 0) {
                    logger.debug("🔧 Correcting total amount for {}: {} -> {}",
                               record.getTransactionId(), record.getTotalAmount(), calculatedTotal);
                    record.setUnitPrice(record.getUnitPrice()); // This will trigger recalculation
                }
            }
        });
        
        logger.info("✅ Record enrichment completed");
        return records;
    }
    
    // Helper methods for calculations
    
    private CategorySummary calculateCategorySummary(List<SalesRecord> records) {
        BigDecimal totalAmount = records.stream()
            .map(SalesRecord::getTotalAmount)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        int transactionCount = records.size();
        BigDecimal averageAmount = transactionCount > 0 
            ? totalAmount.divide(BigDecimal.valueOf(transactionCount), 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
        
        return new CategorySummary(records.get(0).getProductCategory(), transactionCount, totalAmount, averageAmount);
    }
    
    private CustomerSummary calculateCustomerSummary(List<SalesRecord> records) {
        BigDecimal totalSpent = records.stream()
            .map(SalesRecord::getTotalAmount)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        int transactionCount = records.size();
        LocalDateTime lastPurchase = records.stream()
            .map(SalesRecord::getSaleDate)
            .filter(Objects::nonNull)
            .max(LocalDateTime::compareTo)
            .orElse(null);
        
        Set<String> uniqueProducts = records.stream()
            .map(SalesRecord::getProductName)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        
        return new CustomerSummary(records.get(0).getCustomerId(), transactionCount, 
                                 totalSpent, lastPurchase, uniqueProducts.size());
    }
    
    private DailySummary calculateDailySummary(List<SalesRecord> records) {
        LocalDate date = records.get(0).getSaleDate().toLocalDate();
        BigDecimal totalRevenue = records.stream()
            .map(SalesRecord::getTotalAmount)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        int totalTransactions = records.size();
        int uniqueCustomers = (int) records.stream()
            .map(SalesRecord::getCustomerId)
            .filter(Objects::nonNull)
            .distinct()
            .count();
        
        return new DailySummary(date, totalRevenue, totalTransactions, uniqueCustomers);
    }
    
    private ProductSummary calculateProductSummary(List<SalesRecord> records) {
        String productName = records.get(0).getProductName();
        BigDecimal totalRevenue = records.stream()
            .map(SalesRecord::getTotalAmount)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        int totalQuantity = records.stream()
            .mapToInt(SalesRecord::getQuantity)
            .sum();
        
        return new ProductSummary(productName, totalRevenue, totalQuantity);
    }
    
    private double calculateStandardDeviation(List<SalesRecord> records, String field) {
        if (records.isEmpty()) return 0.0;
        
        double[] values = records.stream()
            .mapToDouble(record -> {
                if ("amount".equals(field)) {
                    return record.getTotalAmount() != null ? record.getTotalAmount().doubleValue() : 0.0;
                } else if ("quantity".equals(field)) {
                    return record.getQuantity() != null ? record.getQuantity().doubleValue() : 0.0;
                }
                return 0.0;
            })
            .toArray();
        
        double mean = Arrays.stream(values).average().orElse(0.0);
        double variance = Arrays.stream(values)
            .map(value -> Math.pow(value - mean, 2))
            .average()
            .orElse(0.0);
        
        return Math.sqrt(variance);
    }
    
    private boolean isAnomaly(SalesRecord record, double amountThreshold, double quantityThreshold) {
        double amount = record.getTotalAmount() != null ? record.getTotalAmount().doubleValue() : 0.0;
        double quantity = record.getQuantity() != null ? record.getQuantity().doubleValue() : 0.0;
        
        return amount > amountThreshold || quantity > quantityThreshold;
    }
    
    // Summary classes for aggregated data
    
    public static class CategorySummary {
        private final String category;
        private final int transactionCount;
        private final BigDecimal totalAmount;
        private final BigDecimal averageAmount;
        
        public CategorySummary(String category, int transactionCount, BigDecimal totalAmount, BigDecimal averageAmount) {
            this.category = category;
            this.transactionCount = transactionCount;
            this.totalAmount = totalAmount;
            this.averageAmount = averageAmount;
        }
        
        // Getters
        public String getCategory() { return category; }
        public int getTransactionCount() { return transactionCount; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public BigDecimal getAverageAmount() { return averageAmount; }
    }
    
    public static class CustomerSummary {
        private final String customerId;
        private final int transactionCount;
        private final BigDecimal totalSpent;
        private final LocalDateTime lastPurchase;
        private final int uniqueProducts;
        
        public CustomerSummary(String customerId, int transactionCount, BigDecimal totalSpent, 
                             LocalDateTime lastPurchase, int uniqueProducts) {
            this.customerId = customerId;
            this.transactionCount = transactionCount;
            this.totalSpent = totalSpent;
            this.lastPurchase = lastPurchase;
            this.uniqueProducts = uniqueProducts;
        }
        
        // Getters
        public String getCustomerId() { return customerId; }
        public int getTransactionCount() { return transactionCount; }
        public BigDecimal getTotalSpent() { return totalSpent; }
        public LocalDateTime getLastPurchase() { return lastPurchase; }
        public int getUniqueProducts() { return uniqueProducts; }
    }
    
    public static class DailySummary {
        private final LocalDate date;
        private final BigDecimal totalRevenue;
        private final int totalTransactions;
        private final int uniqueCustomers;
        
        public DailySummary(LocalDate date, BigDecimal totalRevenue, int totalTransactions, int uniqueCustomers) {
            this.date = date;
            this.totalRevenue = totalRevenue;
            this.totalTransactions = totalTransactions;
            this.uniqueCustomers = uniqueCustomers;
        }
        
        // Getters
        public LocalDate getDate() { return date; }
        public BigDecimal getTotalRevenue() { return totalRevenue; }
        public int getTotalTransactions() { return totalTransactions; }
        public int getUniqueCustomers() { return uniqueCustomers; }
    }
    
    public static class ProductSummary {
        private final String productName;
        private final BigDecimal totalRevenue;
        private final int totalQuantity;
        
        public ProductSummary(String productName, BigDecimal totalRevenue, int totalQuantity) {
            this.productName = productName;
            this.totalRevenue = totalRevenue;
            this.totalQuantity = totalQuantity;
        }
        
        // Getters
        public String getProductName() { return productName; }
        public BigDecimal getTotalRevenue() { return totalRevenue; }
        public int getTotalQuantity() { return totalQuantity; }
    }
} 