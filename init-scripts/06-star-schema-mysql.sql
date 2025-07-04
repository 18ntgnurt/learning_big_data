-- ============================================================================
-- STAR SCHEMA IMPLEMENTATION FOR FRAUD DETECTION
-- ============================================================================
-- This script creates a dimensional model designed for ML-based fraud detection
-- Features: Customer behavior tracking, risk scoring, temporal patterns

-- ============================================================================
-- FACT TABLE: Central transaction facts with fraud-specific measures
-- ============================================================================
CREATE TABLE IF NOT EXISTS fact_sales_transactions (
    -- Primary Key
    transaction_sk BIGINT PRIMARY KEY AUTO_INCREMENT,
    transaction_id VARCHAR(50) UNIQUE NOT NULL,
    
    -- Dimension Foreign Keys
    customer_sk BIGINT NOT NULL,
    product_sk BIGINT NOT NULL,
    store_sk BIGINT NOT NULL,
    payment_method_sk BIGINT NOT NULL,
    date_sk BIGINT NOT NULL,
    time_sk BIGINT NOT NULL,
    
    -- Core Transaction Measures
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    discount_amount DECIMAL(10,2) DEFAULT 0,
    tax_amount DECIMAL(10,2) DEFAULT 0,
    
    -- Fraud Detection Measures
    risk_score DECIMAL(5,4) DEFAULT 0.0000,
    fraud_probability DECIMAL(5,4) DEFAULT 0.0000,
    anomaly_score DECIMAL(5,4) DEFAULT 0.0000,
    
    -- Fraud Detection Features
    amount_zscore DECIMAL(8,4) DEFAULT 0.0000,           -- Amount compared to customer history
    velocity_score DECIMAL(5,4) DEFAULT 0.0000,          -- Transaction velocity indicator
    time_anomaly_score DECIMAL(5,4) DEFAULT 0.0000,      -- Time pattern anomaly
    location_risk_score DECIMAL(5,4) DEFAULT 0.0000,     -- Geographic risk score
    
    -- Behavioral Flags (JSON for flexibility)
    fraud_indicators JSON,                                 -- Array of fraud indicators
    feature_vector JSON,                                   -- ML feature vector
    
    -- Labels and Outcomes
    is_fraud BOOLEAN DEFAULT FALSE,                        -- Actual fraud label
    is_investigated BOOLEAN DEFAULT FALSE,                 -- Investigation status
    investigation_result VARCHAR(50),                      -- Investigation outcome
    
    -- Temporal Context
    hours_since_last_transaction INTEGER,                  -- Time between transactions
    transactions_today INTEGER DEFAULT 1,                  -- Count of today's transactions
    transactions_this_hour INTEGER DEFAULT 1,             -- Count this hour
    
    -- Metadata
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,                               -- ML processing timestamp
    
    -- Indexes for fraud detection queries
    INDEX idx_fraud_risk_score (risk_score),
    INDEX idx_fraud_probability (fraud_probability),
    INDEX idx_customer_date (customer_sk, date_sk),
    INDEX idx_amount_range (total_amount),
    INDEX idx_fraud_labels (is_fraud, is_investigated),
    INDEX idx_transaction_velocity (customer_sk, time_sk),
    INDEX idx_anomaly_detection (anomaly_score, customer_sk)
);

-- ============================================================================
-- DIMENSION: Customer with behavioral patterns
-- ============================================================================
CREATE TABLE IF NOT EXISTS dim_customer (
    customer_sk BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id VARCHAR(50) UNIQUE NOT NULL,
    
    -- Customer Demographics
    customer_name VARCHAR(200),
    email VARCHAR(200),
    phone VARCHAR(50),
    customer_segment VARCHAR(50),                         -- VIP, Regular, New, etc.
    registration_date DATE,
    
    -- Purchase History Aggregates
    total_purchases INTEGER DEFAULT 0,
    lifetime_value DECIMAL(12,2) DEFAULT 0.00,
    avg_transaction_amount DECIMAL(10,2) DEFAULT 0.00,
    max_transaction_amount DECIMAL(10,2) DEFAULT 0.00,
    min_transaction_amount DECIMAL(10,2) DEFAULT 0.00,
    std_transaction_amount DECIMAL(10,2) DEFAULT 0.00,    -- Standard deviation
    
    -- Behavioral Patterns
    preferred_categories JSON,                             -- Array of preferred categories
    preferred_stores JSON,                                 -- Array of preferred stores
    preferred_payment_methods JSON,                        -- Payment method preferences
    typical_purchase_hours JSON,                          -- Hourly purchase patterns
    typical_purchase_days JSON,                           -- Day-of-week patterns
    
    -- Risk Indicators
    risk_level VARCHAR(20) DEFAULT 'LOW',                 -- LOW, MEDIUM, HIGH, CRITICAL
    fraud_history_count INTEGER DEFAULT 0,               -- Number of past fraud incidents
    investigation_count INTEGER DEFAULT 0,               -- Number of investigations
    chargeback_count INTEGER DEFAULT 0,                  -- Number of chargebacks
    
    -- Geographic Patterns
    primary_country VARCHAR(100),
    primary_state VARCHAR(100),
    primary_city VARCHAR(100),
    location_diversity_score DECIMAL(5,4) DEFAULT 0,     -- Geographic diversity metric
    
    -- Temporal Patterns
    last_purchase_date DATE,
    average_days_between_purchases DECIMAL(8,2),
    purchase_frequency_category VARCHAR(50),              -- Daily, Weekly, Monthly, etc.
    
    -- Account Status
    is_vip BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    is_blocked BOOLEAN DEFAULT FALSE,
    account_status VARCHAR(50) DEFAULT 'ACTIVE',
    
    -- Feature Engineering Aggregates
    transaction_velocity_7d DECIMAL(8,4) DEFAULT 0,      -- 7-day transaction velocity
    transaction_velocity_30d DECIMAL(8,4) DEFAULT 0,     -- 30-day transaction velocity
    amount_velocity_7d DECIMAL(12,2) DEFAULT 0,          -- 7-day spending velocity
    amount_velocity_30d DECIMAL(12,2) DEFAULT 0,         -- 30-day spending velocity
    
    -- Metadata
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_feature_update TIMESTAMP,                       -- Last ML feature update
    
    -- Indexes for behavioral analysis
    INDEX idx_customer_risk (risk_level),
    INDEX idx_customer_segment (customer_segment),
    INDEX idx_customer_status (account_status, is_active),
    INDEX idx_customer_fraud_history (fraud_history_count),
    INDEX idx_customer_ltv (lifetime_value),
    INDEX idx_customer_velocity (transaction_velocity_7d, amount_velocity_7d)
);

-- ============================================================================
-- DIMENSION: Product with risk factors
-- ============================================================================
CREATE TABLE IF NOT EXISTS dim_product (
    product_sk BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id VARCHAR(50) UNIQUE NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    product_category VARCHAR(100),
    product_subcategory VARCHAR(100),
    product_brand VARCHAR(100),
    
    -- Pricing Information
    current_price DECIMAL(10,2),
    price_range VARCHAR(20),                              -- Budget, Mid-range, Premium, Luxury
    price_tier INTEGER,                                   -- 1-5 pricing tier
    
    -- Risk Factors
    risk_category VARCHAR(20) DEFAULT 'LOW',              -- LOW, MEDIUM, HIGH
    high_risk_category BOOLEAN DEFAULT FALSE,            -- Electronics, Gift Cards, etc.
    fraud_incidents INTEGER DEFAULT 0,                   -- Historical fraud count
    chargeback_rate DECIMAL(5,4) DEFAULT 0,              -- Chargeback percentage
    return_rate DECIMAL(5,4) DEFAULT 0,                  -- Return rate
    
    -- Product Popularity
    total_sales INTEGER DEFAULT 0,
    popularity_score DECIMAL(5,4) DEFAULT 0,             -- Sales-based popularity
    seasonal_demand BOOLEAN DEFAULT FALSE,                -- Seasonal product flag
    limited_availability BOOLEAN DEFAULT FALSE,           -- Limited edition, etc.
    
    -- Geographic Distribution
    primary_regions JSON,                                 -- Regions where popular
    restricted_regions JSON,                              -- Regions with restrictions
    
    -- Fraud-specific Attributes
    requires_identity_verification BOOLEAN DEFAULT FALSE,
    age_restricted BOOLEAN DEFAULT FALSE,
    high_value_threshold DECIMAL(10,2),                  -- Amount requiring extra verification
    
    -- Metadata
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Indexes for product analysis
    INDEX idx_product_category (product_category),
    INDEX idx_product_risk (risk_category),
    INDEX idx_product_fraud_rate (fraud_incidents, chargeback_rate),
    INDEX idx_product_price_tier (price_tier),
    INDEX idx_high_risk_products (high_risk_category, requires_identity_verification)
);

-- ============================================================================
-- DIMENSION: Store with geographic risk data
-- ============================================================================
CREATE TABLE IF NOT EXISTS dim_store (
    store_sk BIGINT PRIMARY KEY AUTO_INCREMENT,
    store_id VARCHAR(50) UNIQUE NOT NULL,
    store_name VARCHAR(200),
    store_type VARCHAR(50),                               -- Physical, Online, Mobile, Kiosk
    
    -- Geographic Information
    address_line1 VARCHAR(200),
    address_line2 VARCHAR(200),
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100),
    postal_code VARCHAR(20),
    region VARCHAR(50),                                   -- Sales region
    timezone VARCHAR(50),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    
    -- Store Characteristics
    store_size VARCHAR(20),                               -- Small, Medium, Large
    opening_hours JSON,                                   -- Operating hours by day
    staff_count INTEGER,
    has_security_cameras BOOLEAN DEFAULT FALSE,
    has_security_guards BOOLEAN DEFAULT FALSE,
    
    -- Risk Factors
    risk_level VARCHAR(20) DEFAULT 'MEDIUM',              -- LOW, MEDIUM, HIGH, CRITICAL
    crime_rate_area DECIMAL(5,4) DEFAULT 0,              -- Local crime statistics
    fraud_incidents INTEGER DEFAULT 0,                   -- Historical fraud at location
    theft_incidents INTEGER DEFAULT 0,                   -- Theft incidents
    
    -- Economic Indicators
    median_income_area DECIMAL(12,2),                    -- Local median income
    poverty_rate_area DECIMAL(5,4),                      -- Local poverty rate
    economic_risk_score DECIMAL(5,4) DEFAULT 0,          -- Economic risk indicator
    
    -- Operational Metrics
    total_transactions INTEGER DEFAULT 0,
    avg_transaction_amount DECIMAL(10,2) DEFAULT 0,
    peak_hours JSON,                                      -- Busiest hours
    seasonal_patterns JSON,                              -- Seasonal transaction patterns
    
    -- Technology and Security
    pos_system_type VARCHAR(100),
    payment_methods_accepted JSON,                        -- Accepted payment types
    has_emv_capability BOOLEAN DEFAULT TRUE,
    has_contactless_capability BOOLEAN DEFAULT FALSE,
    
    -- Compliance and Verification
    kyc_requirements JSON,                                -- Know Your Customer requirements
    verification_level VARCHAR(50),                      -- Basic, Enhanced, Premium
    compliance_score DECIMAL(5,4) DEFAULT 0,
    
    -- Metadata
    opening_date DATE,
    last_security_audit DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Indexes for geographic and risk analysis
    INDEX idx_store_location (country, state, city),
    INDEX idx_store_risk (risk_level),
    INDEX idx_store_type (store_type),
    INDEX idx_store_fraud_incidents (fraud_incidents),
    INDEX idx_store_coordinates (latitude, longitude),
    INDEX idx_store_economic_risk (economic_risk_score)
);

-- ============================================================================
-- DIMENSION: Payment method with risk assessment
-- ============================================================================
CREATE TABLE IF NOT EXISTS dim_payment_method (
    payment_method_sk BIGINT PRIMARY KEY AUTO_INCREMENT,
    payment_method_id VARCHAR(50) UNIQUE NOT NULL,
    
    -- Payment Method Details
    payment_type VARCHAR(50) NOT NULL,                   -- Credit, Debit, Digital, Cash, Crypto
    payment_subtype VARCHAR(50),                         -- Visa, Mastercard, PayPal, etc.
    provider VARCHAR(100),                               -- Card issuer or service provider
    provider_country VARCHAR(100),                       -- Provider's country
    
    -- Risk Assessment
    risk_level VARCHAR(20) DEFAULT 'MEDIUM',             -- LOW, MEDIUM, HIGH, CRITICAL
    fraud_rate DECIMAL(5,4) DEFAULT 0,                   -- Historical fraud rate
    chargeback_rate DECIMAL(5,4) DEFAULT 0,              -- Chargeback rate
    dispute_rate DECIMAL(5,4) DEFAULT 0,                 -- Dispute rate
    
    -- Security Features
    has_chip BOOLEAN DEFAULT FALSE,                      -- EMV chip capability
    has_pin BOOLEAN DEFAULT FALSE,                       -- PIN verification
    has_contactless BOOLEAN DEFAULT FALSE,               -- Contactless payment
    has_biometric BOOLEAN DEFAULT FALSE,                 -- Biometric verification
    encryption_level VARCHAR(50),                        -- Security encryption level
    
    -- Transaction Limits
    daily_limit DECIMAL(12,2),                          -- Daily transaction limit
    transaction_limit DECIMAL(12,2),                    -- Per-transaction limit
    velocity_limit INTEGER,                             -- Transactions per hour limit
    
    -- Geographic and Regulatory
    allowed_countries JSON,                              -- Countries where accepted
    restricted_countries JSON,                           -- Countries with restrictions
    regulatory_requirements JSON,                        -- Compliance requirements
    
    -- Usage Patterns
    popular_age_groups JSON,                             -- Age demographics
    popular_merchant_categories JSON,                    -- Popular merchant types
    peak_usage_hours JSON,                              -- Peak usage times
    
    -- Verification Requirements
    requires_cvv BOOLEAN DEFAULT TRUE,
    requires_address_verification BOOLEAN DEFAULT FALSE,
    requires_phone_verification BOOLEAN DEFAULT FALSE,
    requires_identity_document BOOLEAN DEFAULT FALSE,
    
    -- Operational Metrics
    total_transactions INTEGER DEFAULT 0,
    success_rate DECIMAL(5,4) DEFAULT 0,                -- Transaction success rate
    decline_rate DECIMAL(5,4) DEFAULT 0,                -- Decline rate
    processing_time_ms INTEGER DEFAULT 0,               -- Average processing time
    
    -- Metadata
    introduced_date DATE,
    deprecated_date DATE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Indexes for payment analysis
    INDEX idx_payment_type (payment_type),
    INDEX idx_payment_risk (risk_level),
    INDEX idx_payment_fraud_rate (fraud_rate),
    INDEX idx_payment_provider (provider),
    INDEX idx_payment_active (is_active),
    INDEX idx_payment_security (has_chip, has_pin, encryption_level)
);

-- ============================================================================
-- DIMENSION: Date for temporal analysis
-- ============================================================================
CREATE TABLE IF NOT EXISTS dim_date (
    date_sk BIGINT PRIMARY KEY,
    full_date DATE UNIQUE NOT NULL,
    
    -- Date Components
    year INTEGER,
    quarter INTEGER,
    month INTEGER,
    month_name VARCHAR(20),
    day INTEGER,
    day_of_week INTEGER,
    day_name VARCHAR(20),
    day_of_year INTEGER,
    week_of_year INTEGER,
    
    -- Business Calendar
    is_weekend BOOLEAN DEFAULT FALSE,
    is_holiday BOOLEAN DEFAULT FALSE,
    holiday_name VARCHAR(100),
    is_business_day BOOLEAN DEFAULT TRUE,
    
    -- Fiscal Calendar
    fiscal_year INTEGER,
    fiscal_quarter INTEGER,
    fiscal_month INTEGER,
    
    -- Fraud Pattern Analysis
    is_payday BOOLEAN DEFAULT FALSE,                     -- Common payday dates
    is_month_end BOOLEAN DEFAULT FALSE,                  -- Month-end patterns
    is_black_friday BOOLEAN DEFAULT FALSE,               -- High-volume shopping days
    is_cyber_monday BOOLEAN DEFAULT FALSE,
    shopping_season VARCHAR(50),                         -- Holiday, Back-to-school, etc.
    
    -- Risk Indicators
    historical_fraud_rate DECIMAL(5,4) DEFAULT 0,       -- Historical fraud rate for this date
    transaction_volume_category VARCHAR(20),            -- Low, Medium, High, Peak
    
    INDEX idx_date_components (year, month, day),
    INDEX idx_date_business (is_business_day, is_weekend),
    INDEX idx_date_holidays (is_holiday),
    INDEX idx_date_fraud_patterns (is_payday, is_month_end, shopping_season)
);

-- ============================================================================
-- DIMENSION: Time for intraday analysis
-- ============================================================================
CREATE TABLE IF NOT EXISTS dim_time (
    time_sk BIGINT PRIMARY KEY,
    time_value TIME UNIQUE NOT NULL,
    
    -- Time Components
    hour INTEGER,
    minute INTEGER,
    second INTEGER,
    
    -- Business Hours
    hour_of_day VARCHAR(20),                             -- Morning, Afternoon, Evening, Night
    business_hours BOOLEAN DEFAULT FALSE,               -- 9 AM - 5 PM
    extended_hours BOOLEAN DEFAULT FALSE,               -- 7 AM - 10 PM
    peak_hours BOOLEAN DEFAULT FALSE,                   -- High transaction volume hours
    
    -- Fraud Risk Indicators
    high_risk_time BOOLEAN DEFAULT FALSE,               -- Times with higher fraud rates
    night_transaction BOOLEAN DEFAULT FALSE,            -- Late night transactions
    early_morning BOOLEAN DEFAULT FALSE,                -- Very early morning
    
    -- Time Categories
    time_category VARCHAR(20),                          -- Business, Personal, Night, etc.
    rush_hour BOOLEAN DEFAULT FALSE,                    -- Traffic rush hours
    lunch_hour BOOLEAN DEFAULT FALSE,                   -- Lunch time
    
    INDEX idx_time_components (hour, minute),
    INDEX idx_time_business (business_hours, peak_hours),
    INDEX idx_time_risk (high_risk_time, night_transaction)
);

-- ============================================================================
-- FEATURE ENGINEERING VIEWS
-- ============================================================================

-- Real-time customer behavior metrics
CREATE OR REPLACE VIEW v_customer_behavior_features AS
SELECT 
    c.customer_sk,
    c.customer_id,
    c.risk_level,
    c.lifetime_value,
    c.avg_transaction_amount,
    c.transaction_velocity_7d,
    c.transaction_velocity_30d,
    c.fraud_history_count,
    -- Recent transaction patterns
    COUNT(f.transaction_sk) as transactions_last_30d,
    AVG(f.total_amount) as avg_amount_last_30d,
    MAX(f.total_amount) as max_amount_last_30d,
    STDDEV(f.total_amount) as stddev_amount_last_30d,
    -- Time pattern anomalies
    COUNT(DISTINCT f.date_sk) as active_days_last_30d,
    COUNT(DISTINCT f.store_sk) as stores_used_last_30d,
    COUNT(DISTINCT f.payment_method_sk) as payment_methods_last_30d
FROM dim_customer c
LEFT JOIN fact_sales_transactions f ON c.customer_sk = f.customer_sk
    AND f.date_sk >= DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 30 DAY), '%Y%m%d')
GROUP BY c.customer_sk, c.customer_id, c.risk_level, c.lifetime_value, 
         c.avg_transaction_amount, c.transaction_velocity_7d, 
         c.transaction_velocity_30d, c.fraud_history_count;

-- Transaction risk scoring view
CREATE OR REPLACE VIEW v_transaction_risk_features AS
SELECT 
    f.transaction_sk,
    f.transaction_id,
    f.total_amount,
    f.risk_score,
    f.fraud_probability,
    
    -- Customer context
    c.risk_level as customer_risk,
    c.avg_transaction_amount as customer_avg_amount,
    (f.total_amount / NULLIF(c.avg_transaction_amount, 0)) as amount_ratio_to_avg,
    
    -- Geographic context
    s.risk_level as store_risk,
    s.fraud_incidents as store_fraud_history,
    
    -- Product context
    p.risk_category as product_risk,
    p.fraud_incidents as product_fraud_history,
    
    -- Payment context
    pm.risk_level as payment_risk,
    pm.fraud_rate as payment_fraud_rate,
    
    -- Temporal context
    d.is_weekend,
    d.is_holiday,
    d.historical_fraud_rate as date_fraud_rate,
    t.high_risk_time,
    t.night_transaction,
    
    -- Velocity indicators
    f.transactions_today,
    f.transactions_this_hour,
    f.hours_since_last_transaction
    
FROM fact_sales_transactions f
JOIN dim_customer c ON f.customer_sk = c.customer_sk
JOIN dim_store s ON f.store_sk = s.store_sk
JOIN dim_product p ON f.product_sk = p.product_sk
JOIN dim_payment_method pm ON f.payment_method_sk = pm.payment_method_sk
JOIN dim_date d ON f.date_sk = d.date_sk
JOIN dim_time t ON f.time_sk = t.time_sk;

-- ============================================================================
-- SAMPLE DATA POPULATION
-- ============================================================================

-- Insert sample payment methods
INSERT INTO dim_payment_method (payment_method_id, payment_type, payment_subtype, provider, risk_level, fraud_rate) VALUES
('PM001', 'Credit', 'Visa', 'Visa Inc.', 'LOW', 0.0023),
('PM002', 'Credit', 'Mastercard', 'Mastercard Inc.', 'LOW', 0.0021),
('PM003', 'Debit', 'Visa Debit', 'Visa Inc.', 'LOW', 0.0015),
('PM004', 'Digital', 'PayPal', 'PayPal Inc.', 'MEDIUM', 0.0045),
('PM005', 'Digital', 'Apple Pay', 'Apple Inc.', 'LOW', 0.0012),
('PM006', 'Crypto', 'Bitcoin', 'Various', 'HIGH', 0.0234),
('PM007', 'Cash', 'Cash', 'N/A', 'LOW', 0.0001)
ON DUPLICATE KEY UPDATE payment_method_id = payment_method_id;

-- Insert sample time dimension (subset for demonstration)
INSERT INTO dim_time (time_sk, time_value, hour, minute, hour_of_day, business_hours, high_risk_time, night_transaction) VALUES
(0000, '00:00:00', 0, 0, 'Night', FALSE, TRUE, TRUE),
(0900, '09:00:00', 9, 0, 'Morning', TRUE, FALSE, FALSE),
(1200, '12:00:00', 12, 0, 'Afternoon', TRUE, FALSE, FALSE),
(1500, '15:00:00', 15, 0, 'Afternoon', TRUE, FALSE, FALSE),
(1800, '18:00:00', 18, 0, 'Evening', FALSE, FALSE, FALSE),
(2300, '23:00:00', 23, 0, 'Night', FALSE, TRUE, TRUE)
ON DUPLICATE KEY UPDATE time_sk = time_sk;

-- Success message
SELECT 'Star schema for fraud detection created successfully!' as status; 