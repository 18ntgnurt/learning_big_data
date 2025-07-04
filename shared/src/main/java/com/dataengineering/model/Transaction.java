package com.dataengineering.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Transaction model representing financial transactions
 * Used across ETL and streaming components
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Transaction {
    
    @JsonProperty("transaction_id")
    private String transactionId;
    
    @JsonProperty("customer_id")
    private String customerId;
    
    @JsonProperty("merchant_id")
    private String merchantId;
    
    @JsonProperty("amount")
    private BigDecimal amount;
    
    @JsonProperty("currency")
    private String currency = "USD";
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("category")
    private String category;
    
    @JsonProperty("location")
    private String location;
    
    @JsonProperty("timestamp")
    private LocalDateTime transactionDate;
    
    @JsonProperty("channel")
    private String channel;
    
    @JsonProperty("payment_method")
    private String paymentMethod;
    
    @JsonProperty("metadata")
    private String metadata;
    
    // Default constructor for Jackson
    public Transaction() {}
    
    // Private constructor for builder
    private Transaction(Builder builder) {
        this.transactionId = builder.transactionId;
        this.customerId = builder.customerId;
        this.merchantId = builder.merchantId;
        this.amount = builder.amount;
        this.currency = builder.currency;
        this.description = builder.description;
        this.category = builder.category;
        this.location = builder.location;
        this.transactionDate = builder.transactionDate;
        this.channel = builder.channel;
        this.paymentMethod = builder.paymentMethod;
        this.metadata = builder.metadata;
    }
    
    // Getters
    public String getTransactionId() { return transactionId; }
    public String getCustomerId() { return customerId; }
    public String getMerchantId() { return merchantId; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public String getLocation() { return location; }
    public LocalDateTime getTransactionDate() { return transactionDate; }
    public String getChannel() { return channel; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getMetadata() { return metadata; }
    
    // Setters for Jackson
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public void setCurrency(String currency) { this.currency = currency; }
    public void setDescription(String description) { this.description = description; }
    public void setCategory(String category) { this.category = category; }
    public void setLocation(String location) { this.location = location; }
    public void setTransactionDate(LocalDateTime transactionDate) { this.transactionDate = transactionDate; }
    public void setChannel(String channel) { this.channel = channel; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
    
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String transactionId;
        private String customerId;
        private String merchantId;
        private BigDecimal amount;
        private String currency = "USD";
        private String description;
        private String category;
        private String location;
        private LocalDateTime transactionDate;
        private String channel;
        private String paymentMethod;
        private String metadata;
        
        public Builder transactionId(String transactionId) {
            this.transactionId = transactionId;
            return this;
        }
        
        public Builder customerId(String customerId) {
            this.customerId = customerId;
            return this;
        }
        
        public Builder merchantId(String merchantId) {
            this.merchantId = merchantId;
            return this;
        }
        
        public Builder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }
        
        public Builder currency(String currency) {
            this.currency = currency;
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder category(String category) {
            this.category = category;
            return this;
        }
        
        public Builder location(String location) {
            this.location = location;
            return this;
        }
        
        public Builder transactionDate(LocalDateTime transactionDate) {
            this.transactionDate = transactionDate;
            return this;
        }
        
        public Builder channel(String channel) {
            this.channel = channel;
            return this;
        }
        
        public Builder paymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
            return this;
        }
        
        public Builder metadata(String metadata) {
            this.metadata = metadata;
            return this;
        }
        
        public Transaction build() {
            return new Transaction(this);
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(transactionId, that.transactionId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(transactionId);
    }
    
    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId='" + transactionId + '\'' +
                ", customerId='" + customerId + '\'' +
                ", merchantId='" + merchantId + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", description='" + description + '\'' +
                ", category='" + category + '\'' +
                ", transactionDate=" + transactionDate +
                '}';
    }
} 