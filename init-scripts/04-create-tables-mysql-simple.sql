-- Create tables for the data engineering project (Simple MySQL version)
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
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Drop existing indexes if they exist (ignore errors)
DROP INDEX IF EXISTS idx_sales_customer_id ON sales_records;
DROP INDEX IF EXISTS idx_sales_date ON sales_records;
DROP INDEX IF EXISTS idx_sales_category ON sales_records;
DROP INDEX IF EXISTS idx_sales_location ON sales_records;

-- Create indexes for better query performance
CREATE INDEX idx_sales_customer_id ON sales_records(customer_id);
CREATE INDEX idx_sales_date ON sales_records(sale_date);
CREATE INDEX idx_sales_category ON sales_records(product_category);
CREATE INDEX idx_sales_location ON sales_records(store_location);

-- Insert a test record to verify table creation
INSERT INTO sales_records (
    transaction_id, customer_id, product_name, product_category, quantity,
    unit_price, total_amount, sale_date, store_location, sales_person
) VALUES (
    'TEST-001', 'CUST-001', 'Test Product', 'Electronics', 1,
    99.99, 99.99, NOW(), 'Test Store', 'Test Sales Person'
) ON DUPLICATE KEY UPDATE transaction_id = transaction_id;

SELECT 'Sales records table created successfully' as status; 