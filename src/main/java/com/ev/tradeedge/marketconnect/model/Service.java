package com.ev.tradeedge.marketconnect.model;

public class Service {
    private String id;
    private String fileProcessId;
    private String subsFileId;
    private String name;
    private String instanceName;
    private String tenantId;
    private String status;

    // Constructors
    public Service() {
    }

    public Service(String id, String fileProcessId, String subsFileId, String name, 
                  String instanceName, String tenantId, String status) {
        this.id = id;
        this.fileProcessId = fileProcessId;
        this.subsFileId = subsFileId;
        this.name = name;
        this.instanceName = instanceName;
        this.tenantId = tenantId;
        this.status = status;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFileProcessId() {
        return fileProcessId;
    }

    public void setFileProcessId(String fileProcessId) {
        this.fileProcessId = fileProcessId;
    }

    public String getSubsFileId() {
        return subsFileId;
    }

    public void setSubsFileId(String subsFileId) {
        this.subsFileId = subsFileId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}