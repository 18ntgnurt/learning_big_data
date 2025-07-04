package com.dataengineering.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * DatabaseConfig class manages database connections for the data engineering project.
 * This class demonstrates how to handle different database types and connection pooling concepts.
 * 
 * Key Learning Points:
 * - Database connection management
 * - Configuration management
 * - Resource cleanup patterns
 * - Exception handling in data engineering
 */
public class DatabaseConfig {
    
    // Database configuration constants
    private static final String H2_URL = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE";
    private static final String H2_DRIVER = "org.h2.Driver";
    private static final String H2_USER = "sa";
    private static final String H2_PASSWORD = "";
    
    // MySQL configuration (configured for Docker container)
    private static final String MYSQL_URL = "jdbc:mysql://localhost:3306/data_engineering";
    private static final String MYSQL_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String MYSQL_USER = "dataeng";
    private static final String MYSQL_PASSWORD = "dataeng123";
    
    // PostgreSQL configuration (configured for Docker container)
    private static final String POSTGRES_URL = "jdbc:postgresql://localhost:5432/data_engineering";
    private static final String POSTGRES_DRIVER = "org.postgresql.Driver";
    private static final String POSTGRES_USER = "dataeng";
    private static final String POSTGRES_PASSWORD = "dataeng123";
    
    /**
     * Database types supported by this configuration
     */
    public enum DatabaseType {
        H2, MYSQL, POSTGRESQL
    }
    
    /**
     * Creates a database connection based on the specified database type.
     * 
     * @param dbType The type of database to connect to
     * @return A database connection
     * @throws SQLException If connection fails
     * @throws ClassNotFoundException If database driver is not found
     */
    public static Connection getConnection(DatabaseType dbType) throws SQLException, ClassNotFoundException {
        Connection connection = null;
        
        switch (dbType) {
            case H2:
                // Load H2 driver and create connection
                Class.forName(H2_DRIVER);
                connection = DriverManager.getConnection(H2_URL, H2_USER, H2_PASSWORD);
                System.out.println("✅ Connected to H2 in-memory database");
                break;
                
            case MYSQL:
                // Load MySQL driver and create connection
                Class.forName(MYSQL_DRIVER);
                Properties mysqlProps = new Properties();
                mysqlProps.setProperty("user", MYSQL_USER);
                mysqlProps.setProperty("password", MYSQL_PASSWORD);
                mysqlProps.setProperty("useSSL", "false");
                mysqlProps.setProperty("allowPublicKeyRetrieval", "true");
                
                connection = DriverManager.getConnection(MYSQL_URL, mysqlProps);
                System.out.println("✅ Connected to MySQL database");
                break;
                
            case POSTGRESQL:
                // Load PostgreSQL driver and create connection
                Class.forName(POSTGRES_DRIVER);
                Properties pgProps = new Properties();
                pgProps.setProperty("user", POSTGRES_USER);
                pgProps.setProperty("password", POSTGRES_PASSWORD);
                
                connection = DriverManager.getConnection(POSTGRES_URL, pgProps);
                System.out.println("✅ Connected to PostgreSQL database");
                break;
                
            default:
                throw new IllegalArgumentException("Unsupported database type: " + dbType);
        }
        
        return connection;
    }
    
    /**
     * Safely closes a database connection.
     * This method demonstrates proper resource cleanup in data engineering.
     * 
     * @param connection The connection to close
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("🔒 Database connection closed");
            } catch (SQLException e) {
                System.err.println("❌ Error closing database connection: " + e.getMessage());
            }
        }
    }
    
    /**
     * Tests database connectivity for all supported database types.
     * This method is useful for verifying your database setup.
     */
    public static void testConnections() {
        System.out.println("🔍 Testing database connections...\n");
        
        // Test H2 connection (this should always work as it's in-memory)
        try (Connection h2Conn = getConnection(DatabaseType.H2)) {
            System.out.println("H2 Database: Connection successful ✅");
        } catch (Exception e) {
            System.out.println("H2 Database: Connection failed ❌ - " + e.getMessage());
        }
        
        // Test MySQL connection (comment out if MySQL is not available)
        try (Connection mysqlConn = getConnection(DatabaseType.MYSQL)) {
            System.out.println("MySQL Database: Connection successful ✅");
        } catch (Exception e) {
            System.out.println("MySQL Database: Connection failed ❌ - " + e.getMessage());
        }
        
        // Test PostgreSQL connection (comment out if PostgreSQL is not available)
        try (Connection pgConn = getConnection(DatabaseType.POSTGRESQL)) {
            System.out.println("PostgreSQL Database: Connection successful ✅");
        } catch (Exception e) {
            System.out.println("PostgreSQL Database: Connection failed ❌ - " + e.getMessage());
        }
        
        System.out.println("\n✅ Database connection testing completed");
    }
} 