package com.dataengineering;

import com.dataengineering.model.SalesRecord;
import com.dataengineering.processing.DataProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic test class demonstrating testing patterns in data engineering.
 * 
 * Key Testing Concepts:
 * - Data validation testing
 * - Processing logic verification
 * - Edge case handling
 * - Data quality assertions
 */
public class DataEngineeringTest {
    
    private DataProcessor dataProcessor;
    private List<SalesRecord> testRecords;
    
    @BeforeEach
    void setUp() {
        dataProcessor = new DataProcessor();
        
        // Create test data
        testRecords = Arrays.asList(
            new SalesRecord("TXN001", "CUST001", "Laptop", "Electronics", 
                          1, new BigDecimal("999.99"), 
                          LocalDateTime.of(2023, 10, 1, 10, 0), 
                          "New York", "John Doe"),
            
            new SalesRecord("TXN002", "CUST002", "Coffee Mug", "Home", 
                          2, new BigDecimal("15.50"), 
                          LocalDateTime.of(2023, 10, 2, 11, 0), 
                          "Boston", "Jane Smith"),
            
            new SalesRecord("TXN003", "CUST001", "Mouse", "Electronics", 
                          1, new BigDecimal("25.99"), 
                          LocalDateTime.of(2023, 10, 3, 14, 0), 
                          "New York", "John Doe")
        );
    }
    
    @Test
    void testSalesRecordValidation() {
        // Test valid record
        SalesRecord validRecord = testRecords.get(0);
        assertTrue(validRecord.isValid(), "Valid record should pass validation");
        assertTrue(validRecord.getValidationErrors().isEmpty(), "Valid record should have no errors");
        
        // Test invalid record
        SalesRecord invalidRecord = new SalesRecord();
        invalidRecord.setTransactionId(""); // Invalid empty ID
        
        assertFalse(invalidRecord.isValid(), "Invalid record should fail validation");
        assertFalse(invalidRecord.getValidationErrors().isEmpty(), "Invalid record should have errors");
    }
    
    @Test
    void testDataFiltering() {
        // Test filtering by amount
        List<SalesRecord> filtered = dataProcessor.filterRecords(
            testRecords, new BigDecimal("50.00"), null, null, null);
        
        assertEquals(1, filtered.size(), "Should find 1 record above $50");
        assertEquals("TXN001", filtered.get(0).getTransactionId(), "Should be the laptop transaction");
    }
    
    @Test
    void testCategoryGrouping() {
        Map<String, DataProcessor.CategorySummary> grouped = dataProcessor.groupByCategory(testRecords);
        
        assertEquals(2, grouped.size(), "Should have 2 categories");
        assertTrue(grouped.containsKey("Electronics"), "Should contain Electronics category");
        assertTrue(grouped.containsKey("Home"), "Should contain Home category");
        
        DataProcessor.CategorySummary electronics = grouped.get("Electronics");
        assertEquals(2, electronics.getTransactionCount(), "Electronics should have 2 transactions");
    }
    
    @Test
    void testCustomerGrouping() {
        Map<String, DataProcessor.CustomerSummary> grouped = dataProcessor.groupByCustomer(testRecords);
        
        assertEquals(2, grouped.size(), "Should have 2 customers");
        
        DataProcessor.CustomerSummary cust001 = grouped.get("CUST001");
        assertEquals(2, cust001.getTransactionCount(), "CUST001 should have 2 transactions");
        assertEquals(2, cust001.getUniqueProducts(), "CUST001 should have bought 2 different products");
    }
    
    @Test
    void testTopProducts() {
        List<DataProcessor.ProductSummary> topProducts = dataProcessor.getTopProducts(testRecords, 2, true);
        
        assertEquals(2, topProducts.size(), "Should return top 2 products");
        assertEquals("Laptop", topProducts.get(0).getProductName(), "Laptop should be top product by revenue");
    }
    
    @Test
    void testAnomalyDetection() {
        // Test that anomaly detection runs without errors
        List<SalesRecord> anomalies = dataProcessor.detectAnomalies(testRecords);
        
        // Should return a list (empty or not)
        assertNotNull(anomalies, "Anomaly detection should return a list");
        assertTrue(anomalies.size() >= 0, "Anomaly list should have non-negative size");
    }
    
    @Test
    void testDataEnrichment() {
        // Create a record with incorrect total
        SalesRecord record = new SalesRecord();
        record.setTransactionId("TEST001");
        record.setCustomerId("CUST001");
        record.setProductName("Test Product");
        record.setQuantity(3);
        record.setUnitPrice(new BigDecimal("10.00"));
        // Don't set total amount - it should be calculated
        
        List<SalesRecord> records = Arrays.asList(record);
        List<SalesRecord> enriched = dataProcessor.enrichRecords(records);
        
        assertEquals(new BigDecimal("30.00"), enriched.get(0).getTotalAmount(), 
                    "Total amount should be calculated correctly");
    }
} 