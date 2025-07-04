package com.dataengineering.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * SalesRecord represents a typical business data record in data engineering.
 * This class demonstrates:
 * - Data modeling best practices
 * - JSON serialization/deserialization
 * - Data validation
 * - Immutable data objects
 * - Proper equals/hashCode implementation
 */
public class SalesRecord {
    
    @JsonProperty("transaction_id")
    private String transactionId;
    
    @JsonProperty("customer_id")
    private String customerId;
    
    @JsonProperty("product_name")
    private String productName;
    
    @JsonProperty("product_category")
    private String productCategory;
    
    @JsonProperty("quantity")
    private Integer quantity;
    
    @JsonProperty("unit_price")
    private BigDecimal unitPrice;
    
    @JsonProperty("total_amount")
    private BigDecimal totalAmount;
    
    @JsonProperty("sale_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime saleDate;
    
    @JsonProperty("store_location")
    private String storeLocation;
    
    @JsonProperty("sales_person")
    private String salesPerson;
    
    // Default constructor for Jackson deserialization
    public SalesRecord() {}
    
    /**
     * Constructor for creating a new sales record
     */
    public SalesRecord(String transactionId, String customerId, String productName, 
                      String productCategory, Integer quantity, BigDecimal unitPrice, 
                      LocalDateTime saleDate, String storeLocation, String salesPerson) {
        this.transactionId = transactionId;
        this.customerId = customerId;
        this.productName = productName;
        this.productCategory = productCategory;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalAmount = calculateTotalAmount();
        this.saleDate = saleDate;
        this.storeLocation = storeLocation;
        this.salesPerson = salesPerson;
    }
    
    /**
     * Calculates the total amount based on quantity and unit price.
     * This demonstrates business logic in data models.
     * 
     * @return The calculated total amount
     */
    private BigDecimal calculateTotalAmount() {
        if (quantity != null && unitPrice != null) {
            return unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
        return BigDecimal.ZERO;
    }
    
    /**
     * Validates the sales record data.
     * Data validation is crucial in data engineering pipelines.
     * 
     * @return true if the record is valid, false otherwise
     */
    public boolean isValid() {
        return transactionId != null && !transactionId.trim().isEmpty() &&
               customerId != null && !customerId.trim().isEmpty() &&
               productName != null && !productName.trim().isEmpty() &&
               quantity != null && quantity > 0 &&
               unitPrice != null && unitPrice.compareTo(BigDecimal.ZERO) > 0 &&
               saleDate != null;
    }
    
    /**
     * Returns a string representation of validation errors.
     * Useful for data quality reporting.
     * 
     * @return Validation error message or empty string if valid
     */
    public String getValidationErrors() {
        StringBuilder errors = new StringBuilder();
        
        if (transactionId == null || transactionId.trim().isEmpty()) {
            errors.append("Transaction ID is required; ");
        }
        if (customerId == null || customerId.trim().isEmpty()) {
            errors.append("Customer ID is required; ");
        }
        if (productName == null || productName.trim().isEmpty()) {
            errors.append("Product name is required; ");
        }
        if (quantity == null || quantity <= 0) {
            errors.append("Quantity must be greater than 0; ");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            errors.append("Unit price must be greater than 0; ");
        }
        if (saleDate == null) {
            errors.append("Sale date is required; ");
        }
        
        return errors.toString();
    }
    
    // Getters and Setters
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    
    public String getProductCategory() { return productCategory; }
    public void setProductCategory(String productCategory) { this.productCategory = productCategory; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { 
        this.quantity = quantity;
        this.totalAmount = calculateTotalAmount(); // Recalculate when quantity changes
    }
    
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { 
        this.unitPrice = unitPrice;
        this.totalAmount = calculateTotalAmount(); // Recalculate when price changes
    }
    
    public BigDecimal getTotalAmount() { return totalAmount; }
    
    public LocalDateTime getSaleDate() { return saleDate; }
    public void setSaleDate(LocalDateTime saleDate) { this.saleDate = saleDate; }
    
    public String getStoreLocation() { return storeLocation; }
    public void setStoreLocation(String storeLocation) { this.storeLocation = storeLocation; }
    
    public String getSalesPerson() { return salesPerson; }
    public void setSalesPerson(String salesPerson) { this.salesPerson = salesPerson; }
    
    /**
     * Equals method for proper object comparison.
     * Important for data deduplication in data engineering.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SalesRecord that = (SalesRecord) obj;
        return Objects.equals(transactionId, that.transactionId);
    }
    
    /**
     * HashCode method for proper object hashing.
     * Important for using objects in hash-based collections.
     */
    @Override
    public int hashCode() {
        return Objects.hash(transactionId);
    }
    
    /**
     * String representation of the sales record.
     * Useful for debugging and logging.
     */
    @Override
    public String toString() {
        return String.format("SalesRecord{id='%s', customer='%s', product='%s', quantity=%d, amount=%s, date=%s}",
                transactionId, customerId, productName, quantity, totalAmount, saleDate);
    }
} 