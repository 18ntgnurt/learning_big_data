# 🐳 Docker Database Setup

This guide helps you set up MySQL and PostgreSQL databases using Docker for your Data Engineering learning project.

## 📋 Prerequisites

- **Docker Desktop** installed and running
- **Docker Compose** (included with Docker Desktop)

## 🚀 Quick Start

### 1. Start the Databases

```bash
# Start all databases in the background
docker-compose up -d

# Check if containers are running
docker-compose ps
```

### 2. Verify Database Connections

Run your Java application and test connections:

```bash
mvn exec:java -Dexec.mainClass="com.dataengineering.DataEngineeringApplication"
```

Choose option **6** to test database connections. You should now see:
- ✅ H2 Database: Connection successful
- ✅ MySQL Database: Connection successful  
- ✅ PostgreSQL Database: Connection successful

## 🗃️ Database Details

### MySQL
- **Host:** localhost
- **Port:** 3306
- **Database:** data_engineering
- **Username:** dataeng
- **Password:** dataeng123
- **Root Password:** password

### PostgreSQL
- **Host:** localhost
- **Port:** 5432
- **Database:** data_engineering
- **Username:** dataeng
- **Password:** dataeng123

### H2 (In-Memory)
- **URL:** jdbc:h2:mem:testdb
- **Username:** sa
- **Password:** (empty)

## 🌐 Database Management UI

Access Adminer (web-based database manager) at: http://localhost:8080

**Login Details:**
- **System:** MySQL or PostgreSQL
- **Server:** mysql or postgres (container names)
- **Username:** dataeng
- **Password:** dataeng123
- **Database:** data_engineering

## 🛠️ Common Commands

### Start/Stop Services
```bash
# Start all services
docker-compose up -d

# Stop all services
docker-compose down

# Stop and remove volumes (WARNING: deletes all data)
docker-compose down -v

# View logs
docker-compose logs mysql
docker-compose logs postgres
```

### Individual Container Management
```bash
# Start only MySQL
docker-compose up -d mysql

# Start only PostgreSQL
docker-compose up -d postgres

# Restart a service
docker-compose restart mysql
```

### Connect to Databases Directly
```bash
# Connect to MySQL
docker exec -it data_engineering_mysql mysql -u dataeng -p data_engineering

# Connect to PostgreSQL
docker exec -it data_engineering_postgres psql -U dataeng -d data_engineering
```

## 🔧 Troubleshooting

### Port Already in Use
If you get port conflicts:

```bash
# Check what's using the port
lsof -i :3306  # For MySQL
lsof -i :5432  # For PostgreSQL

# Stop conflicting services or change ports in docker-compose.yml
```

### Container Won't Start
```bash
# View detailed logs
docker-compose logs mysql
docker-compose logs postgres

# Remove and recreate containers
docker-compose down
docker-compose up -d
```

### Data Persistence
- Data is stored in Docker volumes and persists between container restarts
- To completely reset databases: `docker-compose down -v`

## 📊 Testing Your Setup

### 1. Run Connection Tests
```bash
mvn exec:java -Dexec.mainClass="com.dataengineering.DataEngineeringApplication"
# Choose option 6
```

### 2. Test Full ETL Pipeline
```bash
# In the application menu, choose option 1
# This will test all database operations
```

### 3. Compare Database Performance
Try running the same operations on different databases:
- H2 (fastest, in-memory)
- MySQL (good balance)
- PostgreSQL (feature-rich)

## 🎯 Learning Opportunities

With all three databases running, you can:

1. **Compare Performance:** Run the same queries on different databases
2. **Learn SQL Dialects:** See how different databases handle the same operations
3. **Practice Migration:** Move data between different database systems
4. **Understand Trade-offs:** Learn when to use each database type

## 🔄 Database Switching in Code

Your Java application automatically detects which databases are available. You can modify the `DEFAULT_DB_TYPE` in `DataEngineeringApplication.java` to switch between:

```java
// In DataEngineeringApplication.java
private static final DatabaseType DEFAULT_DB_TYPE = DatabaseType.H2;        // Fast, in-memory
private static final DatabaseType DEFAULT_DB_TYPE = DatabaseType.MYSQL;     // Traditional SQL
private static final DatabaseType DEFAULT_DB_TYPE = DatabaseType.POSTGRESQL; // Advanced features
```

## 📈 Next Steps

1. Start with H2 for basic learning
2. Move to MySQL for traditional SQL database experience  
3. Use PostgreSQL for advanced features and analytics
4. Compare performance and features across all three

---

**Happy Learning! 🎓** Your data engineering journey now includes real production-grade databases! 