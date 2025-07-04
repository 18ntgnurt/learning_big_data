-- This script runs when the databases are first created
-- It ensures the data_engineering database exists and is ready to use

-- For MySQL, the database is already created via environment variables
-- For PostgreSQL, the database is already created via environment variables

-- Create a simple test table to verify the database is working
-- This will be replaced by the Java application when it creates the real tables

SELECT 'Database initialization completed' as status; 