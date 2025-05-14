package com.ev.tradeedge.marketconnect.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import java.util.HashMap;
import java.util.Map;

public class Config {
    private DbConfig db;
    private SftpConfig sftp;
    private Map<String, String> serverMappings;

    public Config() {
        // Initialize with empty map
        this.serverMappings = new HashMap<>();
    }

    // Getters and setters
    public DbConfig getDb() {
        return db;
    }

    public void setDb(DbConfig db) {
        this.db = db;
    }

    public SftpConfig getSftp() {
        return sftp;
    }

    public void setSftp(SftpConfig sftp) {
        this.sftp = sftp;
    }

    public Map<String, String> getServerMappings() {
        return serverMappings;
    }

    public void setServerMappings(Map<String, String> serverMappings) {
        this.serverMappings = serverMappings;
    }

    // Nested classes
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DbConfig {
        // Staging environment
        private String stagingHost;
        private String stagingUser;
        private String stagingPort;
        private String stagingDatabase;
        private String stagingPassword;
        private String stagingTenantPassword;
        
        // Production environment
        private String prodHost;
        private String prodUser;
        private String prodPort;
        private String prodDatabase;
        private String prodPassword;
        private String prodTenantPassword;

        // Getters and setters for staging
        public String getStagingHost() {
            return stagingHost;
        }

        public void setStagingHost(String stagingHost) {
            this.stagingHost = stagingHost;
        }

        public String getStagingUser() {
            return stagingUser;
        }

        public void setStagingUser(String stagingUser) {
            this.stagingUser = stagingUser;
        }
        
        public String getStagingPort() {
            return stagingPort;
        }

        public void setStagingPort(String stagingPort) {
            this.stagingPort = stagingPort;
        }

        public String getStagingDatabase() {
            return stagingDatabase;
        }

        public void setStagingDatabase(String stagingDatabase) {
            this.stagingDatabase = stagingDatabase;
        }

        public String getStagingPassword() {
            return stagingPassword;
        }

        public void setStagingPassword(String stagingPassword) {
            this.stagingPassword = stagingPassword;
        }

        public String getStagingTenantPassword() {
            return stagingTenantPassword;
        }

        public void setStagingTenantPassword(String stagingTenantPassword) {
            this.stagingTenantPassword = stagingTenantPassword;
        }

        // Getters and setters for production
        public String getProdHost() {
            return prodHost;
        }

        public void setProdHost(String prodHost) {
            this.prodHost = prodHost;
        }

        public String getProdUser() {
            return prodUser;
        }

        public void setProdUser(String prodUser) {
            this.prodUser = prodUser;
        }
        
        public String getProdPort() {
            return prodPort;
        }

        public void setProdPort(String prodPort) {
            this.prodPort = prodPort;
        }

        public String getProdDatabase() {
            return prodDatabase;
        }

        public void setProdDatabase(String prodDatabase) {
            this.prodDatabase = prodDatabase;
        }

        public String getProdPassword() {
            return prodPassword;
        }

        public void setProdPassword(String prodPassword) {
            this.prodPassword = prodPassword;
        }

        public String getProdTenantPassword() {
            return prodTenantPassword;
        }

        public void setProdTenantPassword(String prodTenantPassword) {
            this.prodTenantPassword = prodTenantPassword;
        }
        
        // Special setters for backward compatibility
        @JsonSetter("port")
        public void setPort(String port) {
            // Set both staging and production ports to the same value
            this.stagingPort = port;
            this.prodPort = port;
        }
        
        @JsonSetter("database")
        public void setDatabase(String database) {
            // Set both staging and production databases to the same value
            this.stagingDatabase = database;
            this.prodDatabase = database;
        }
        
        @JsonSetter("password")
        public void setPassword(String password) {
            // Set both staging and production passwords to the same value
            this.stagingPassword = password;
            this.prodPassword = password;
        }
        
        @JsonSetter("tenantPassword")
        public void setTenantPassword(String tenantPassword) {
            // Set both staging and production tenant passwords to the same value
            this.stagingTenantPassword = tenantPassword;
            this.prodTenantPassword = tenantPassword;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SftpConfig {
        private String stagingHost;
        private String stagingUsername;
        private String stagingPassword;
        private String stagingPath;
        private String prodHost;
        private String prodUsername;
        private String prodPassword;
        private String prodPath;

        // Getters and setters
        public String getStagingHost() {
            return stagingHost;
        }

        public void setStagingHost(String stagingHost) {
            this.stagingHost = stagingHost;
        }

        public String getStagingUsername() {
            return stagingUsername;
        }

        public void setStagingUsername(String stagingUsername) {
            this.stagingUsername = stagingUsername;
        }

        public String getStagingPassword() {
            return stagingPassword;
        }

        public void setStagingPassword(String stagingPassword) {
            this.stagingPassword = stagingPassword;
        }

        public String getStagingPath() {
            return stagingPath;
        }

        public void setStagingPath(String stagingPath) {
            this.stagingPath = stagingPath;
        }

        public String getProdHost() {
            return prodHost;
        }

        public void setProdHost(String prodHost) {
            this.prodHost = prodHost;
        }

        public String getProdUsername() {
            return prodUsername;
        }

        public void setProdUsername(String prodUsername) {
            this.prodUsername = prodUsername;
        }

        public String getProdPassword() {
            return prodPassword;
        }

        public void setProdPassword(String prodPassword) {
            this.prodPassword = prodPassword;
        }

        public String getProdPath() {
            return prodPath;
        }

        public void setProdPath(String prodPath) {
            this.prodPath = prodPath;
        }
    }
}
