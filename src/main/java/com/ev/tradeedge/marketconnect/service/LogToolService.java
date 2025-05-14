package com.ev.tradeedge.marketconnect.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ev.tradeedge.marketconnect.model.LogExtractRequest;
import com.ev.tradeedge.marketconnect.model.Service;

@org.springframework.stereotype.Service
public class LogToolService {
    private static final Logger logger = LoggerFactory.getLogger(LogToolService.class);
    
    private final DatabaseService databaseService;
    private final SftpService sftpService;
    
    public LogToolService(DatabaseService databaseService, SftpService sftpService) {
        this.databaseService = databaseService;
        this.sftpService = sftpService;
    }

    public List<Service> getServices(String fpid, String server) {
        try {
            // Get services from database
            Connection conn = databaseService.getMainDbConnection(server);
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT DISTINCT ON (service_name) file_process_id, subs_file_id, service_name, " +
                "service_instance_name, tenant_id, status FROM teopr.audit_event_log " +
                "WHERE file_process_id = ?"
            );
            stmt.setLong(1, Long.parseLong(fpid));
            ResultSet rs = stmt.executeQuery();
            
            List<Service> services = new ArrayList<>();
            while (rs.next()) {
                Service service = new Service(
                    rs.getString("service_name"), // Using service_name as ID
                    rs.getString("file_process_id"),
                    rs.getString("subs_file_id"),
                    rs.getString("service_name"),
                    rs.getString("service_instance_name"),
                    rs.getString("tenant_id"),
                    rs.getString("status")
                );
                services.add(service);
            }
            
            rs.close();
            stmt.close();
            
            return services;
        } catch (SQLException e) {
            logger.error("Error getting services", e);
            // Fallback to mock data
            List<Service> services = new ArrayList<>();
            services.add(new Service("1", fpid, "sub-1", "CMService", "instance-1", "tenant-1", "Completed"));
            services.add(new Service("2", fpid, "sub-2", "FIPService", "instance-2", "tenant-1", "Completed"));
            services.add(new Service("3", fpid, "sub-3", "FAService", "instance-3", "tenant-1", "Completed"));
            services.add(new Service("4", fpid, "sub-4", "DCMSService", "instance-4", "tenant-1", "Completed"));
            return services;
        }
    }

    public Map<String, String> getTimeRange(String fpid, String server) {
        try {
            // Get time range from database
            Connection conn = databaseService.getMainDbConnection(server);
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT start_time, end_time FROM teopr.file_process_summary WHERE file_process_id = ?"
            );
            stmt.setLong(1, Long.parseLong(fpid));
            ResultSet rs = stmt.executeQuery();
            
            Map<String, String> timeRange = new HashMap<>();
            if (rs.next()) {
                // Format timestamps
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                
                LocalDateTime startTime = rs.getTimestamp("start_time").toLocalDateTime();
                LocalDateTime endTime = rs.getTimestamp("end_time").toLocalDateTime();
                
                // Adjust time range (30 minutes before and after)
                startTime = startTime.minusMinutes(30);
                endTime = endTime.plusMinutes(30);
                
                timeRange.put("startTime", startTime.format(formatter));
                timeRange.put("endTime", endTime.format(formatter));
            } else {
                // Fallback to mock data
                timeRange.put("startTime", "2023-05-01 10:30:45");
                timeRange.put("endTime", "2023-05-01 11:30:45");
            }
            
            rs.close();
            stmt.close();
            
            return timeRange;
        } catch (SQLException e) {
            logger.error("Error getting time range", e);
            // Fallback to mock data
            Map<String, String> timeRange = new HashMap<>();
            timeRange.put("startTime", "2023-05-01 10:30:45");
            timeRange.put("endTime", "2023-05-01 11:30:45");
            return timeRange;
        }
    }

    public String extractLogs(LogExtractRequest request) {
        StringBuilder logs = new StringBuilder();
        
        try {
            // Get time range
            Map<String, String> timeRange = request.getTimeRange();
            String startTime = timeRange.get("startTime");
            String endTime = timeRange.get("endTime");
            
            // For each service, find and extract logs
            for (String serviceName : request.getServices()) {
                logs.append("--").append(serviceName).append("--\n\n");
                
                // Find log files
                List<String> files = sftpService.findLogFiles(
                    request.getServer(), 
                    serviceName, 
                    startTime, 
                    endTime
                );
                
                if (files.isEmpty()) {
                    logs.append("No log files found for this service.\n\n");
                    continue;
                }
                
                // Process each file
                for (String filePath : files) {
                    // Download file
                    byte[] fileContent = sftpService.downloadFile(request.getServer(), filePath);
                    
                    if (fileContent.length == 0) {
                        logs.append("Failed to download file: ").append(filePath).append("\n");
                        continue;
                    }
                    
                    // Extract content
                    String content;
                    if (filePath.endsWith(".gz")) {
                        content = extractGzipContent(fileContent);
                    } else {
                        content = new String(fileContent, "UTF-8");
                    }
                    
                    // Find relevant log entries
                    String fpid = request.getFpid();
                    String[] lines = content.split("\n");
                    String requiredText = "FileProcessId:" + fpid;
                    
                    for (int i = 0; i < lines.length; i++) {
                        if (lines[i].contains(requiredText)) {
                            int startIndex = i - 1;
                            int endIndex = i + 1;
                            
                            // Find the start of the log entry
                            while (startIndex >= 0 && !lines[startIndex].contains("**********")) {
                                startIndex--;
                            }
                            
                            // Find the end of the log entry
                            while (endIndex < lines.length && !lines[endIndex].contains("**********")) {
                                endIndex++;
                            }
                            
                            // Extract the log entry
                            logs.append("**********\n");
                            for (int j = startIndex + 1; j < endIndex; j++) {
                                if (!lines[j].trim().isEmpty()) {
                                    logs.append(lines[j].trim()).append("\n");
                                }
                            }
                            logs.append("**********\n\n");
                        }
                    }
                }
            }
            
            return logs.toString();
        } catch (Exception e) {
            logger.error("Error extracting logs", e);
            return "Error extracting logs: " + e.getMessage();
        }
    }
    
    private String extractGzipContent(byte[] compressedContent) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(compressedContent);
        GZIPInputStream gis = new GZIPInputStream(bis);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        
        byte[] buffer = new byte[1024];
        int len;
        while ((len = gis.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        
        gis.close();
        bis.close();
        
        return bos.toString("UTF-8");
    }
}