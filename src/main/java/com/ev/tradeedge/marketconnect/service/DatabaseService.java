package com.ev.tradeedge.marketconnect.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ev.tradeedge.marketconnect.model.Config;

@Service
public class DatabaseService {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseService.class);
    
    private final ConfigService configService;
    
    // Cache connections to avoid creating new ones for every request
    private final Map<String, Connection> connectionCache = new HashMap<>();
   
    public DatabaseService(ConfigService configService) {
        this.configService = configService;
    }
    
    /**
     * Get a connection to the main database (meta DB)
     * @param serverType "staging" or "production"
     * @return A database connection
     */
    public Connection getMainDbConnection(String serverType) throws SQLException {
        String cacheKey = serverType + "-main";
        
        // Check if we have a cached connection that is still valid
        if (connectionCache.containsKey(cacheKey)) {
            Connection conn = connectionCache.get(cacheKey);
            if (conn != null && !conn.isClosed()) {
                return conn;
            }
            // Remove invalid connection from cache
            connectionCache.remove(cacheKey);
        }
        
        // Get configuration
        Config config = configService.getConfig();
        
        // Check if config is null
        if (config == null) {
            logger.error("Configuration is null. Make sure config.json exists and is properly loaded.");
            throw new SQLException("Database configuration not found");
        }
        
        // Check if db config is null
        Config.DbConfig dbConfig = config.getDb();
        if (dbConfig == null) {
            logger.error("Database configuration is null in config.json");
            throw new SQLException("Database configuration not found in config.json");
        }
        
        // Build connection URL based on server type
        String host, user, port, database, password;
        
        if ("staging".equals(serverType)) {
            host = dbConfig.getStagingHost();
            user = dbConfig.getStagingUser();
            port = dbConfig.getStagingPort();
            database = dbConfig.getStagingDatabase();
            password = dbConfig.getStagingPassword();
            
            logger.info("Using STAGING database configuration");
        } else {
            host = dbConfig.getProdHost();
            user = dbConfig.getProdUser();
            port = dbConfig.getProdPort();
            database = dbConfig.getProdDatabase();
            password = dbConfig.getProdPassword();
            
            logger.info("Using PRODUCTION database configuration");
        }
        
        // Log the connection details (without password)
        logger.debug("Connecting to database - Host: {}, Port: {}, Database: {}, User: {}", 
                     host, port, database, user);
        
        // Check for null values
        if (host == null || port == null || database == null) {
            logger.error("Database connection parameters are null - Host: {}, Port: {}, Database: {}", 
                         host, port, database);
            throw new SQLException("Database connection parameters cannot be null");
        }
        
        String url = String.format("jdbc:postgresql://%s:%s/%s", host, port, database);
        
        logger.info("Connecting to main database at {}", url);
        
        // Create new connection
        Connection conn = DriverManager.getConnection(url, user, password);
        
        // Cache the connection
        connectionCache.put(cacheKey, conn);
        
        return conn;
    }
    
    /**
     * Get a connection to a tenant database
     * @param host Database host (server name from tenant_datasource)
     * @param port Database port
     * @param database Database name
     * @param user Database user
     * @return A database connection
     */
    public Connection getTenantDbConnection(String host, String port, String database, String user) throws SQLException {
        // Map the host to IP if needed - this is the key part for tenant databases
        String hostIp = configService.getServerIp(host);
        
        String cacheKey = String.format("%s-%s-%s-%s", hostIp, port, database, user);
        
        // Check if we have a cached connection that is still valid
        if (connectionCache.containsKey(cacheKey)) {
            Connection conn = connectionCache.get(cacheKey);
            if (conn != null && !conn.isClosed()) {
                return conn;
            }
            // Remove invalid connection from cache
            connectionCache.remove(cacheKey);
        }
        
        // Get configuration
        Config config = configService.getConfig();
        
        // Determine which tenant password to use based on the host
        // This is a simple heuristic - you might need a more sophisticated approach
        String password;
        if (host.contains("mcmstg") || hostIp.startsWith("192.168.")) {
            password = config.getDb().getStagingTenantPassword();
            logger.info("Using STAGING tenant password for host: {}", host);
        } else {
            password = config.getDb().getProdTenantPassword();
            logger.info("Using PRODUCTION tenant password for host: {}", host);
        }
        
        String url = String.format("jdbc:postgresql://%s:%s/%s", hostIp, port, database);
        
        logger.info("Connecting to tenant database at {} (mapped from {})", url, host);
        
        // Create new connection
        Connection conn = DriverManager.getConnection(url, user, password);
        
        // Cache the connection
        connectionCache.put(cacheKey, conn);
        
        return conn;
    }
    
    /**
     * Close all cached connections
     */
    public void closeAllConnections() {
        for (Connection conn : connectionCache.values()) {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                logger.error("Error closing database connection", e);
            }
        }
        connectionCache.clear();
    }
}
