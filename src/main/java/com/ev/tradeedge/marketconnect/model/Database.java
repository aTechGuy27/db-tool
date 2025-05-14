package com.ev.tradeedge.marketconnect.model;

public class Database {
    private String id;
    private String tenantId;
    private String serverName;
    private String port;
    private String name;
    private String username;

    // Constructors
    public Database() {
    }

    public Database(String id, String tenantId, String serverName, String port, String name, String username) {
        this.id = id;
        this.tenantId = tenantId;
        this.serverName = serverName;
        this.port = port;
        this.name = name;
        this.username = username;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}