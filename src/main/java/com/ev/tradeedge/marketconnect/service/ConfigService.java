package com.ev.tradeedge.marketconnect.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.ev.tradeedge.marketconnect.model.Config;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;

@Service
public class ConfigService {
    private static final Logger logger = LoggerFactory.getLogger(ConfigService.class);
    private static final String CONFIG_FILE = "config.json";
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private Config config;
    private LocalDateTime lastModified;

    @PostConstruct
    public void init() {
        loadConfig();
    }

    /**
     * Reload configuration every 30 seconds if the file has been modified
     */
    @Scheduled(fixedDelay = 30000)
    public void checkAndReloadConfig() {
        try {
            Path path = Paths.get(CONFIG_FILE);
            if (Files.exists(path)) {
                LocalDateTime fileLastModified = LocalDateTime.ofInstant(
                    Files.getLastModifiedTime(path).toInstant(), 
                    java.time.ZoneId.systemDefault()
                );
                
                if (lastModified == null || fileLastModified.isAfter(lastModified)) {
                    logger.info("Configuration file has been modified. Reloading...");
                    loadConfig();
                }
            }
        } catch (IOException e) {
            logger.error("Error checking configuration file modification time", e);
        }
    }

    private void loadConfig() {
        try {
            File file = new File(CONFIG_FILE);
            
            // Log the absolute path to help with debugging
            logger.info("Looking for config file at: {}", file.getAbsolutePath());
            
            if (!file.exists()) {
                // Create default config if it doesn't exist
                logger.warn("Configuration file not found. Creating default configuration.");
                config = createDefaultConfig();
                saveConfig(config);
            } else {
                config = objectMapper.readValue(file, Config.class);
                
                // Ensure server mappings exist
                if (config.getServerMappings() == null) {
                    config.setServerMappings(new HashMap<>());
                }
                
                // Check if we need to migrate the configuration
                if (needsMigration(config)) {
                    logger.info("Migrating configuration to new format...");
                    migrateConfig(config);
                    saveConfig(config);
                    logger.info("Configuration migration completed successfully");
                }
                
                // Log loaded configuration (without sensitive data)
                if (config.getDb() != null) {
                    logger.info("Loaded database configuration:");
                    logger.info("  Staging - Host: {}, Database: {}", 
                               config.getDb().getStagingHost(), 
                               config.getDb().getStagingDatabase());
                    logger.info("  Production - Host: {}, Database: {}", 
                               config.getDb().getProdHost(), 
                               config.getDb().getProdDatabase());
                } else {
                    logger.warn("Database configuration is missing in config.json");
                }
                
                Path path = Paths.get(CONFIG_FILE);
                lastModified = LocalDateTime.ofInstant(
                    Files.getLastModifiedTime(path).toInstant(), 
                    java.time.ZoneId.systemDefault()
                );
                logger.info("Configuration loaded successfully");
            }
        } catch (IOException e) {
            logger.error("Failed to read configuration: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to read configuration", e);
        }
    }

    /**
     * Check if the configuration needs to be migrated to the new format
     */
    private boolean needsMigration(Config config) {
        Config.DbConfig db = config.getDb();
        if (db == null) {
            return false;
        }
        
        // Check if any of the new fields are missing
        return (db.getStagingPort() == null || db.getStagingDatabase() == null || 
                db.getProdPort() == null || db.getProdDatabase() == null);
    }
    
    /**
     * Migrate the configuration from the old format to the new format
     */
    private void migrateConfig(Config config) {
        // Create backup of the old config
        try {
            File file = new File(CONFIG_FILE);
            if (file.exists()) {
                String backupFile = CONFIG_FILE + ".backup." + System.currentTimeMillis();
                Files.copy(file.toPath(), new File(backupFile).toPath());
                logger.info("Created backup of configuration file: {}", backupFile);
            }
        } catch (IOException e) {
            logger.warn("Failed to create backup of configuration file", e);
        }
        
        // No need to do anything else here since the @JsonSetter methods in the Config class
        // will handle the migration of old fields to new fields during deserialization
    }

    public Config getConfig() {
        if (config == null) {
            logger.warn("Configuration is null, attempting to reload");
            loadConfig();
        }
        return config;
    }

    public void saveConfig(Config config) {
        try {
            objectMapper.writeValue(new File(CONFIG_FILE), config);
            this.config = config;
            Path path = Paths.get(CONFIG_FILE);
            lastModified = LocalDateTime.ofInstant(
                Files.getLastModifiedTime(path).toInstant(), 
                java.time.ZoneId.systemDefault()
            );
            logger.info("Configuration saved successfully");
        } catch (IOException e) {
            logger.error("Failed to save configuration", e);
            throw new RuntimeException("Failed to save configuration", e);
        }
    }

    private Config createDefaultConfig() {
        Config config = new Config();
        Config.DbConfig dbConfig = new Config.DbConfig();
        Config.SftpConfig sftpConfig = new Config.SftpConfig();
        
        // Set default values for staging database
        dbConfig.setStagingHost("localhost");
        dbConfig.setStagingUser("postgres");
        dbConfig.setStagingPort("5432");
        dbConfig.setStagingDatabase("dbtools_staging");
        dbConfig.setStagingPassword("postgres");
        dbConfig.setStagingTenantPassword("postgres");
        
        // Set default values for production database
        dbConfig.setProdHost("prod-db-server");
        dbConfig.setProdUser("postgres");
        dbConfig.setProdPort("5432");
        dbConfig.setProdDatabase("dbtools_prod");
        dbConfig.setProdPassword("secure_password");
        dbConfig.setProdTenantPassword("secure_tenant_password");
        
        // Set default values for SFTP
        sftpConfig.setStagingHost("sftp-staging.example.com");
        sftpConfig.setStagingUsername("sftp_user");
        sftpConfig.setStagingPassword("sftp_password");
        sftpConfig.setStagingPath("/logs/staging");
        
        sftpConfig.setProdHost("sftp-prod.example.com");
        sftpConfig.setProdUsername("sftp_user");
        sftpConfig.setProdPassword("sftp_password");
        sftpConfig.setProdPath("/logs/production");
        
        config.setDb(dbConfig);
        config.setSftp(sftpConfig);
        
        // Add some default server mappings
        Map<String, String> serverMappings = new HashMap<>();
        serverMappings.put("mcm-pgb01@tradeedge.net", "192.168.1.101");
        serverMappings.put("mcm-pgb02@tradeedge.net", "192.168.1.102");
        config.setServerMappings(serverMappings);
        
        return config;
    }
    
    /**
     * Get the IP address for a server name
     * @param serverName The server name from tenant_datasource
     * @return The mapped IP address or the original server name if no mapping exists
     */
    public String getServerIp(String serverName) {
        if (config.getServerMappings().containsKey(serverName)) {
            String ip = config.getServerMappings().get(serverName);
            logger.debug("Mapped server {} to IP {}", serverName, ip);
            return ip;
        }
        
        logger.warn("No IP mapping found for server: {}. Using server name as is.", serverName);
        return serverName;
    }
}
