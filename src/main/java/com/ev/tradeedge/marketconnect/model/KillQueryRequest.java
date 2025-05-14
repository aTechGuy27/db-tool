package com.ev.tradeedge.marketconnect.model;


import java.util.List;

public class KillQueryRequest {
    private List<String> queryIds;
    private Database database;
    private String server;

    // Getters and setters
    public List<String> getQueryIds() {
        return queryIds;
    }

    public void setQueryIds(List<String> queryIds) {
        this.queryIds = queryIds;
    }

    public Database getDatabase() {
        return database;
    }

    public void setDatabase(Database database) {
        this.database = database;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }
}