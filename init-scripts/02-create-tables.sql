-- Create tables for the data engineering project (MySQL version)
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

-- Create indexes for better query performance (MySQL syntax)
-- Check if index exists before creating to avoid errors
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
     WHERE table_schema = DATABASE() AND table_name = 'sales_records' AND index_name = 'idx_sales_customer_id') > 0,
    'SELECT "Index idx_sales_customer_id already exists"',
    'CREATE INDEX idx_sales_customer_id ON sales_records(customer_id)'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
     WHERE table_schema = DATABASE() AND table_name = 'sales_records' AND index_name = 'idx_sales_date') > 0,
    'SELECT "Index idx_sales_date already exists"',
    'CREATE INDEX idx_sales_date ON sales_records(sale_date)'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
     WHERE table_schema = DATABASE() AND table_name = 'sales_records' AND index_name = 'idx_sales_category') > 0,
    'SELECT "Index idx_sales_category already exists"',
    'CREATE INDEX idx_sales_category ON sales_records(product_category)'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
     WHERE table_schema = DATABASE() AND table_name = 'sales_records' AND index_name = 'idx_sales_location') > 0,
    'SELECT "Index idx_sales_location already exists"',
    'CREATE INDEX idx_sales_location ON sales_records(store_location)'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Insert a test record to verify table creation
INSERT INTO sales_records (
    transaction_id, customer_id, product_name, product_category, quantity,
    unit_price, total_amount, sale_date, store_location, sales_person
) VALUES (
    'TEST-001', 'CUST-001', 'Test Product', 'Electronics', 1,
    99.99, 99.99, NOW(), 'Test Store', 'Test Sales Person'
) ON DUPLICATE KEY UPDATE transaction_id = transaction_id;

SELECT 'Sales records table created successfully' as status; 