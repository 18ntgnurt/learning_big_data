-- Create tables for the data engineering project (Final MySQL version)
-- This script creates the sales_records table used by the Java application

-- Create sales_records table
CREATE TABLE IF NOT EXISTS sales_records (
    transaction_id VARCHAR(50) PRIMARY KEY,
    customer_id VARCHAR(50) NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    product_category VARCHAR(100),
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    sale_date TIMESTAMP NOT NULL,
    store_location VARCHAR(100),
    sales_person VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    -- Add indexes directly in table definition
    INDEX idx_sales_customer_id (customer_id),
    INDEX idx_sales_date (sale_date),
    INDEX idx_sales_category (product_category),
    INDEX idx_sales_location (store_location)
);

-- Insert a test record to verify table creation
INSERT IGNORE INTO sales_records (
    transaction_id, customer_id, product_name, product_category, quantity,
    unit_price, total_amount, sale_date, store_location, sales_person
) VALUES (
    'TEST-001', 'CUST-001', 'Test Product', 'Electronics', 1,
    99.99, 99.99, NOW(), 'Test Store', 'Test Sales Person'
);

SELECT 'Sales records table created successfully' as status; 