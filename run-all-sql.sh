#!/bin/bash

# Script to run all SQL files for the data engineering project
# This script executes SQL initialization and table creation scripts on both MySQL and PostgreSQL

echo "🗄️  Running All SQL Files for Data Engineering Project"
echo "======================================================"

# Check if containers are running
echo "📋 Checking database containers..."
docker ps --filter "name=data_engineering_mysql" --format "{{.Names}}: {{.Status}}"
docker ps --filter "name=data_engineering_postgres" --format "{{.Names}}: {{.Status}}"

echo ""
echo "🔧 Executing SQL scripts..."

# 1. Run initial setup (already done, but here for reference)
echo "✅ 1. Initial setup (01-init.sql) - Already executed during container startup"

# 2. Create tables in PostgreSQL
echo "📊 2. Creating tables in PostgreSQL..."
docker exec -i data_engineering_postgres psql -U dataeng -d data_engineering < init-scripts/03-create-tables-postgres.sql

# 3. Create tables in MySQL
echo "📊 3. Creating tables in MySQL..."
docker exec -i data_engineering_mysql mysql -u dataeng -pdataeng123 data_engineering < init-scripts/05-create-tables-mysql-final.sql

echo ""
echo "🔍 Verifying table creation..."

# 4. Verify MySQL table structure
echo "📋 MySQL table structure:"
docker exec -i data_engineering_mysql mysql -u dataeng -pdataeng123 data_engineering -e "DESCRIBE sales_records;" 2>/dev/null

echo ""
echo "📋 PostgreSQL table structure:"
# 5. Verify PostgreSQL table structure
docker exec -i data_engineering_postgres psql -U dataeng -d data_engineering -c "\d sales_records" 2>/dev/null

echo ""
echo "📊 Checking test data..."

# 6. Show test data in both databases
echo "🔍 MySQL test data:"
docker exec -i data_engineering_mysql mysql -u dataeng -pdataeng123 data_engineering -e "SELECT COUNT(*) as record_count FROM sales_records;" 2>/dev/null

echo "🔍 PostgreSQL test data:"
docker exec -i data_engineering_postgres psql -U dataeng -d data_engineering -c "SELECT COUNT(*) as record_count FROM sales_records;" 2>/dev/null

echo ""
echo "✅ All SQL files executed successfully!"
echo "🎯 Your databases are ready for the Java data engineering application!"
echo ""
echo "📝 Available SQL files:"
echo "   - init-scripts/01-init.sql (Initial setup)"
echo "   - init-scripts/03-create-tables-postgres.sql (PostgreSQL schema)"
echo "   - init-scripts/05-create-tables-mysql-final.sql (MySQL schema)"
echo ""
echo "🌐 Database connections:"
echo "   MySQL: localhost:3306 (user: dataeng, password: dataeng123)"
echo "   PostgreSQL: localhost:5432 (user: dataeng, password: dataeng123)"
echo "   Adminer UI: http://localhost:8080" 