package com.dataengineering.database;

import com.dataengineering.config.DatabaseConfig;
import com.dataengineering.config.DatabaseConfig.DatabaseType;
import com.dataengineering.model.SalesRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DatabaseOperations demonstrates database operations in data engineering.
 * This class covers essential database patterns used in data pipelines.
 * 
 * Key Learning Points:
 * - Database schema creation and management
 * - Batch inserts for performance
 * - Data retrieval and filtering
 * - Transaction management
 * - Database performance optimization
 * - Error handling in database operations
 */
public class DatabaseOperations {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseOperations.class);
    
    // SQL statements for table creation and operations
    private static final String CREATE_TABLE_SQL = 
        "CREATE TABLE IF NOT EXISTS sales_records (" +
        "    transaction_id VARCHAR(50) PRIMARY KEY," +
        "    customer_id VARCHAR(50) NOT NULL," +
        "    product_name VARCHAR(200) NOT NULL," +
        "    product_category VARCHAR(100)," +
        "    quantity INTEGER NOT NULL," +
        "    unit_price DECIMAL(10,2) NOT NULL," +
        "    total_amount DECIMAL(10,2) NOT NULL," +
        "    sale_date TIMESTAMP NOT NULL," +
        "    store_location VARCHAR(100)," +
        "    sales_person VARCHAR(100)," +
        "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
        ")";
    
    private static final String INSERT_RECORD_SQL = 
        "INSERT INTO sales_records " +
        "(transaction_id, customer_id, product_name, product_category, quantity, " +
        " unit_price, total_amount, sale_date, store_location, sales_person) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
    private static final String SELECT_ALL_SQL = 
        "SELECT transaction_id, customer_id, product_name, product_category, quantity, " +
        "       unit_price, total_amount, sale_date, store_location, sales_person " +
        "FROM sales_records " +
        "ORDER BY sale_date DESC";
    
    private static final String SELECT_BY_CUSTOMER_SQL = 
        "SELECT transaction_id, customer_id, product_name, product_category, quantity, " +
        "       unit_price, total_amount, sale_date, store_location, sales_person " +
        "FROM sales_records " +
        "WHERE customer_id = ? " +
        "ORDER BY sale_date DESC";
    
    private static final String COUNT_RECORDS_SQL = "SELECT COUNT(*) FROM sales_records";
    
    private static final String DELETE_ALL_SQL = "DELETE FROM sales_records";
    
    /**
     * Creates the sales_records table if it doesn't exist.
     * This demonstrates database schema management in data engineering.
     * 
     * @param dbType Database type to use
     * @throws SQLException If database operation fails
     * @throws ClassNotFoundException If database driver not found
     */
    public void createTable(DatabaseType dbType) throws SQLException, ClassNotFoundException {
        logger.info("🗄️ Creating sales_records table...");
        
        try (Connection conn = DatabaseConfig.getConnection(dbType);
             Statement stmt = conn.createStatement()) {
            
            stmt.execute(CREATE_TABLE_SQL);
            logger.info("✅ Table created successfully or already exists");
            
        } catch (SQLException e) {
            logger.error("❌ Error creating table: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Inserts a single sales record into the database.
     * 
     * @param record The sales record to insert
     * @param dbType Database type to use
     * @throws SQLException If database operation fails
     * @throws ClassNotFoundException If database driver not found
     */
    public void insertRecord(SalesRecord record, DatabaseType dbType) 
            throws SQLException, ClassNotFoundException {
        
        logger.debug("💾 Inserting record: {}", record.getTransactionId());
        
        try (Connection conn = DatabaseConfig.getConnection(dbType);
             PreparedStatement pstmt = conn.prepareStatement(INSERT_RECORD_SQL)) {
            
            setRecordParameters(pstmt, record);
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.debug("✅ Record inserted successfully: {}", record.getTransactionId());
            } else {
                logger.warn("⚠️ No rows affected for record: {}", record.getTransactionId());
            }
            
        } catch (SQLException e) {
            logger.error("❌ Error inserting record {}: {}", record.getTransactionId(), e.getMessage());
            throw e;
        }
    }
    
    /**
     * Inserts multiple sales records using batch processing.
     * This demonstrates efficient bulk data loading in data engineering.
     * 
     * @param records List of sales records to insert
     * @param dbType Database type to use
     * @param batchSize Number of records to process in each batch
     * @throws SQLException If database operation fails
     * @throws ClassNotFoundException If database driver not found
     */
    public void insertRecordsBatch(List<SalesRecord> records, DatabaseType dbType, int batchSize) 
            throws SQLException, ClassNotFoundException {
        
        logger.info("📦 Starting batch insert of {} records with batch size {}", records.size(), batchSize);
        
        try (Connection conn = DatabaseConfig.getConnection(dbType)) {
            conn.setAutoCommit(false); // Enable transaction management
            
            try (PreparedStatement pstmt = conn.prepareStatement(INSERT_RECORD_SQL)) {
                
                int recordCount = 0;
                int successCount = 0;
                int errorCount = 0;
                
                for (SalesRecord record : records) {
                    try {
                        setRecordParameters(pstmt, record);
                        pstmt.addBatch();
                        recordCount++;
                        
                        // Execute batch when reaching batch size
                        if (recordCount % batchSize == 0) {
                            int[] results = pstmt.executeBatch();
                            successCount += countSuccessfulInserts(results);
                            conn.commit();
                            logger.info("📊 Processed {} records so far...", recordCount);
                        }
                        
                    } catch (SQLException e) {
                        errorCount++;
                        logger.error("❌ Error preparing record {}: {}", 
                                   record.getTransactionId(), e.getMessage());
                    }
                }
                
                // Execute remaining records in the batch
                if (recordCount % batchSize != 0) {
                    int[] results = pstmt.executeBatch();
                    successCount += countSuccessfulInserts(results);
                    conn.commit();
                }
                
                logger.info("✅ Batch insert completed!");
                logger.info("📈 Total records processed: {}", recordCount);
                logger.info("✅ Successfully inserted: {}", successCount);
                logger.info("❌ Errors: {}", errorCount);
                
            } catch (SQLException e) {
                conn.rollback();
                logger.error("❌ Batch insert failed, rolling back: {}", e.getMessage());
                throw e;
            }
            
        } catch (SQLException e) {
            logger.error("❌ Database connection error: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Retrieves all sales records from the database.
     * 
     * @param dbType Database type to use
     * @return List of all sales records
     * @throws SQLException If database operation fails
     * @throws ClassNotFoundException If database driver not found
     */
    public List<SalesRecord> getAllRecords(DatabaseType dbType) 
            throws SQLException, ClassNotFoundException {
        
        logger.info("🔍 Retrieving all sales records...");
        
        List<SalesRecord> records = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection(dbType);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL_SQL)) {
            
            while (rs.next()) {
                SalesRecord record = mapResultSetToRecord(rs);
                records.add(record);
            }
            
            logger.info("✅ Retrieved {} records", records.size());
            
        } catch (SQLException e) {
            logger.error("❌ Error retrieving records: {}", e.getMessage());
            throw e;
        }
        
        return records;
    }
    
    /**
     * Retrieves sales records for a specific customer.
     * This demonstrates parameterized queries and filtering.
     * 
     * @param customerId Customer ID to filter by
     * @param dbType Database type to use
     * @return List of sales records for the customer
     * @throws SQLException If database operation fails
     * @throws ClassNotFoundException If database driver not found
     */
    public List<SalesRecord> getRecordsByCustomer(String customerId, DatabaseType dbType) 
            throws SQLException, ClassNotFoundException {
        
        logger.info("🔍 Retrieving records for customer: {}", customerId);
        
        List<SalesRecord> records = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection(dbType);
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_CUSTOMER_SQL)) {
            
            pstmt.setString(1, customerId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    SalesRecord record = mapResultSetToRecord(rs);
                    records.add(record);
                }
            }
            
            logger.info("✅ Retrieved {} records for customer {}", records.size(), customerId);
            
        } catch (SQLException e) {
            logger.error("❌ Error retrieving records for customer {}: {}", customerId, e.getMessage());
            throw e;
        }
        
        return records;
    }
    
    /**
     * Gets the total count of records in the database.
     * Useful for monitoring and reporting.
     * 
     * @param dbType Database type to use
     * @return Total count of records
     * @throws SQLException If database operation fails
     * @throws ClassNotFoundException If database driver not found
     */
    public long getRecordCount(DatabaseType dbType) throws SQLException, ClassNotFoundException {
        logger.info("🔢 Getting total record count...");
        
        try (Connection conn = DatabaseConfig.getConnection(dbType);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(COUNT_RECORDS_SQL)) {
            
            if (rs.next()) {
                long count = rs.getLong(1);
                logger.info("📊 Total records in database: {}", count);
                return count;
            }
            
        } catch (SQLException e) {
            logger.error("❌ Error getting record count: {}", e.getMessage());
            throw e;
        }
        
        return 0;
    }
    
    /**
     * Deletes all records from the database.
     * Use with caution - this is mainly for testing purposes.
     * 
     * @param dbType Database type to use
     * @throws SQLException If database operation fails
     * @throws ClassNotFoundException If database driver not found
     */
    public void deleteAllRecords(DatabaseType dbType) throws SQLException, ClassNotFoundException {
        logger.warn("🗑️ Deleting all records from database...");
        
        try (Connection conn = DatabaseConfig.getConnection(dbType);
             Statement stmt = conn.createStatement()) {
            
            int deletedCount = stmt.executeUpdate(DELETE_ALL_SQL);
            logger.info("✅ Deleted {} records", deletedCount);
            
        } catch (SQLException e) {
            logger.error("❌ Error deleting records: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Helper method to set parameters for PreparedStatement.
     */
    private void setRecordParameters(PreparedStatement pstmt, SalesRecord record) throws SQLException {
        pstmt.setString(1, record.getTransactionId());
        pstmt.setString(2, record.getCustomerId());
        pstmt.setString(3, record.getProductName());
        pstmt.setString(4, record.getProductCategory());
        pstmt.setInt(5, record.getQuantity());
        pstmt.setBigDecimal(6, record.getUnitPrice());
        pstmt.setBigDecimal(7, record.getTotalAmount());
        pstmt.setTimestamp(8, Timestamp.valueOf(record.getSaleDate()));
        pstmt.setString(9, record.getStoreLocation());
        pstmt.setString(10, record.getSalesPerson());
    }
    
    /**
     * Helper method to map ResultSet to SalesRecord object.
     */
    private SalesRecord mapResultSetToRecord(ResultSet rs) throws SQLException {
        SalesRecord record = new SalesRecord();
        record.setTransactionId(rs.getString("transaction_id"));
        record.setCustomerId(rs.getString("customer_id"));
        record.setProductName(rs.getString("product_name"));
        record.setProductCategory(rs.getString("product_category"));
        record.setQuantity(rs.getInt("quantity"));
        record.setUnitPrice(rs.getBigDecimal("unit_price"));
        record.setSaleDate(rs.getTimestamp("sale_date").toLocalDateTime());
        record.setStoreLocation(rs.getString("store_location"));
        record.setSalesPerson(rs.getString("sales_person"));
        return record;
    }
    
    /**
     * Helper method to count successful inserts from batch results.
     */
    private int countSuccessfulInserts(int[] results) {
        int count = 0;
        for (int result : results) {
            if (result >= 0) count++;
        }
        return count;
    }
} 