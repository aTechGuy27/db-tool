package com.ev.tradeedge.marketconnect.model;

import java.util.List;
import java.util.Map;

public class LogExtractRequest {
    private String fpid;
    private List<String> services;
    private String server;
    private Map<String, String> timeRange;
    private boolean includeRelatedThreads = true; // Default to true for backward compatibility

    // Getters and setters
    public String getFpid() {
        return fpid;
    }

    public void setFpid(String fpid) {
        this.fpid = fpid;
    }

    public List<String> getServices() {
        return services;
    }

    public void setServices(List<String> services) {
        this.services = services;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public Map<String, String> getTimeRange() {
        return timeRange;
    }

    public void setTimeRange(Map<String, String> timeRange) {
        this.timeRange = timeRange;
    }
    
    public boolean isIncludeRelatedThreads() {
        return includeRelatedThreads;
    }
    
    public void setIncludeRelatedThreads(boolean includeRelatedThreads) {
        this.includeRelatedThreads = includeRelatedThreads;
    }
}