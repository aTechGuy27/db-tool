package com.ev.tradeedge.marketconnect.model;

public class Query {
    private String database;
    private String pid;
    private String application;
    private String username;
    private String startTime;
    private String executionTime;
    private String state;
    private String query;

    // Constructors
    public Query() {
    }

    public Query(String database, String pid, String application, String username, 
                String startTime, String executionTime, String state, String query) {
        this.database = database;
        this.pid = pid;
        this.application = application;
        this.username = username;
        this.startTime = startTime;
        this.executionTime = executionTime;
        this.state = state;
        this.query = query;
    }

    // Getters and setters
    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(String executionTime) {
        this.executionTime = executionTime;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}