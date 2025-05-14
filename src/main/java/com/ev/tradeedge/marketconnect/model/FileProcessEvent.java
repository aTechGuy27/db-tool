package com.ev.tradeedge.marketconnect.model;


public class FileProcessEvent {
 private Long fileProcessEventId;
 private Integer parentProcessEventId;
 private Long fileProcessId;
 private Integer subsFileId;
 private String eventCode;
 private String startTime;
 private String endTime;
 private String createdTime;
 private String status;
 private Boolean isSuccess;
 private String failureReason;
 private String remarks;
 
 // Getters and setters
 public Long getFileProcessEventId() {
     return fileProcessEventId;
 }
 
 public void setFileProcessEventId(Long fileProcessEventId) {
     this.fileProcessEventId = fileProcessEventId;
 }
 
 public Integer getParentProcessEventId() {
     return parentProcessEventId;
 }
 
 public void setParentProcessEventId(Integer parentProcessEventId) {
     this.parentProcessEventId = parentProcessEventId;
 }
 
 public Long getFileProcessId() {
     return fileProcessId;
 }
 
 public void setFileProcessId(Long fileProcessId) {
     this.fileProcessId = fileProcessId;
 }
 
 public Integer getSubsFileId() {
     return subsFileId;
 }
 
 public void setSubsFileId(Integer subsFileId) {
     this.subsFileId = subsFileId;
 }
 
 public String getEventCode() {
     return eventCode;
 }
 
 public void setEventCode(String eventCode) {
     this.eventCode = eventCode;
 }
 
 public String getStartTime() {
     return startTime;
 }
 
 public void setStartTime(String startTime) {
     this.startTime = startTime;
 }
 
 public String getEndTime() {
     return endTime;
 }
 
 public void setEndTime(String endTime) {
     this.endTime = endTime;
 }
 
 public String getCreatedTime() {
     return createdTime;
 }
 
 public void setCreatedTime(String createdTime) {
     this.createdTime = createdTime;
 }
 
 public String getStatus() {
     return status;
 }
 
 public void setStatus(String status) {
     this.status = status;
 }
 
 public Boolean getIsSuccess() {
     return isSuccess;
 }
 
 public void setIsSuccess(Boolean isSuccess) {
     this.isSuccess = isSuccess;
 }
 
 public String getFailureReason() {
     return failureReason;
 }
 
 public void setFailureReason(String failureReason) {
     this.failureReason = failureReason;
 }
 
 public String getRemarks() {
     return remarks;
 }
 
 public void setRemarks(String remarks) {
     this.remarks = remarks;
 }
}
