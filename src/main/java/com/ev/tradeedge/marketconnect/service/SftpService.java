package com.ev.tradeedge.marketconnect.service;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ev.tradeedge.marketconnect.model.Config;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

@Service
public class SftpService {
    private static final Logger logger = LoggerFactory.getLogger(SftpService.class);
    
    private final ConfigService configService;
    
    @Autowired
    public SftpService(ConfigService configService) {
        this.configService = configService;
    }
    
    /**
     * Create an SFTP session
     * @param serverType "staging" or "production"
     * @return A JSch Session
     */
    private Session createSftpSession(String serverType) throws JSchException {
        Config config = configService.getConfig();
        Config.SftpConfig sftpConfig = config.getSftp();
        
        String host = "staging".equals(serverType) ? sftpConfig.getStagingHost() : sftpConfig.getProdHost();
        String username = "staging".equals(serverType) ? sftpConfig.getStagingUsername() : sftpConfig.getProdUsername();
        String password = "staging".equals(serverType) ? sftpConfig.getStagingPassword() : sftpConfig.getProdPassword();
        
        logger.info("Connecting to SFTP server at {}@{}", username, host);
        
        JSch jsch = new JSch();
        Session session = jsch.getSession(username, host, 22);
        session.setPassword(password);
        
        // Skip host key checking (not recommended for production)
        Properties config2 = new Properties();
        config2.put("StrictHostKeyChecking", "no");
        session.setConfig(config2);
        
        session.connect();
        return session;
    }
    
    /**
     * Find log files in the SFTP server
     * @param serverType "staging" or "production"
     * @param serviceName Service name to filter files
     * @param startTime Start time for file filtering
     * @param endTime End time for file filtering
     * @return List of file paths
     */
    public List<String> findLogFiles(String serverType, String serviceName, String startTime, String endTime) {
        Session session = null;
        ChannelExec channel = null;
        List<String> files = new ArrayList<>();
        
        try {
            session = createSftpSession(serverType);
            
            Config config = configService.getConfig();
            Config.SftpConfig sftpConfig = config.getSftp();
            String rootPath = "staging".equals(serverType) ? sftpConfig.getStagingPath() : sftpConfig.getProdPath();
            
            // Try with 5-minute buffer first
            int bufferMinutes = 5;
            files = findLogFilesWithBuffer(session, rootPath, serviceName, startTime, endTime, bufferMinutes);
            
            // If no files found, try with 10-minute buffer
            if (files.isEmpty()) {
                bufferMinutes = 10;
                logger.info("No files found with 5-minute buffer. Trying with 10-minute buffer...");
                files = findLogFilesWithBuffer(session, rootPath, serviceName, startTime, endTime, bufferMinutes);
                
                // If still no files, try with 15-minute buffer as last resort
                if (files.isEmpty()) {
                    bufferMinutes = 15;
                    logger.info("No files found with 10-minute buffer. Trying with 15-minute buffer...");
                    files = findLogFilesWithBuffer(session, rootPath, serviceName, startTime, endTime, bufferMinutes);
                }
            }
            
            logger.info("Found {} log files for service {} with {} minute buffer", files.size(), serviceName, bufferMinutes);
            
        } catch (JSchException e) {
            logger.error("Error finding log files", e);
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }
        
        return files;
    }
    
    /**
     * Find log files with a specific time buffer
     * @param session SFTP session
     * @param rootPath Root path to search in
     * @param serviceName Service name to filter files
     * @param startTime Start time for file filtering
     * @param endTime End time for file filtering
     * @param bufferMinutes Minutes to add as buffer before and after the time range
     * @return List of file paths
     */
    private List<String> findLogFilesWithBuffer(Session session, String rootPath, String serviceName, 
                                           String startTime, String endTime, int bufferMinutes) {
        ChannelExec channel = null;
        List<String> files = new ArrayList<>();
        
        try {
            // Add buffer to the time range
            String adjustedStartTime = adjustTimeWithBuffer(startTime, -bufferMinutes);
            String adjustedEndTime = adjustTimeWithBuffer(endTime, bufferMinutes);
            
            logger.info("Searching with {} minute buffer - Time range: {} to {}", 
                   bufferMinutes, adjustedStartTime, adjustedEndTime);
            
            // First find service directories matching the pattern
            String findDirsCommand = String.format(
                "find %s -type d -name \"%s*_Logs\"",
                rootPath, serviceName
            );
            
            logger.info("Finding service directories: {}", findDirsCommand);
            
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(findDirsCommand);
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            channel.setOutputStream(outputStream);
            channel.connect();
            
            // Wait for command to complete
            while (channel.isConnected()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            
            // Parse service directories
            String output = outputStream.toString();
            String[] serviceDirs = output.split("\n");
            
            if (serviceDirs.length == 0 || (serviceDirs.length == 1 && serviceDirs[0].trim().isEmpty())) {
                logger.warn("No service directories found for {}. Falling back to direct file search.", serviceName);
                
                // Fall back to direct file search if no directories found
                channel.disconnect();
                
                // Build find command for direct file search
                String findFilesCommand = String.format(
                    "find %s -type f -name \"%s*.log*\" -newermt \"%s\" ! -newermt \"%s\"",
                    rootPath, serviceName, adjustedStartTime, adjustedEndTime
                );
                
                logger.info("Executing direct file search: {}", findFilesCommand);
                
                channel = (ChannelExec) session.openChannel("exec");
                channel.setCommand(findFilesCommand);
                
                outputStream = new ByteArrayOutputStream();
                channel.setOutputStream(outputStream);
                channel.connect();
                
                // Wait for command to complete
                while (channel.isConnected()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                
                // Parse output
                output = outputStream.toString();
                String[] lines = output.split("\n");
                for (String line : lines) {
                    if (line.trim().endsWith(".log") || line.trim().endsWith(".gz")) {
                        files.add(line.trim());
                    }
                }
                
                return files;
            }
            
            // For each service directory, find log files
            for (String serviceDir : serviceDirs) {
                if (serviceDir.trim().isEmpty()) continue;
                
                channel.disconnect();
                
                // Build find command for log files in this directory
                // Using proper grouping with $$ and $$ for the find command
                String findFilesCommand = String.format(
                	    "find %s -type f -name \"*.log\" -o -name \"*.log.gz\" -newermt \"%s\" ! -newermt \"%s\"",
                	    serviceDir.trim(), startTime, endTime
                	);
                
                logger.info("Finding log files in directory {}: {}", serviceDir, findFilesCommand);
                
                channel = (ChannelExec) session.openChannel("exec");
                channel.setCommand(findFilesCommand);
                
                outputStream = new ByteArrayOutputStream();
                channel.setOutputStream(outputStream);
                channel.connect();
                
                // Wait for command to complete
                while (channel.isConnected()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                
                // Parse log files
                output = outputStream.toString();
                String[] logFiles = output.split("\n");
                
                for (String logFile : logFiles) {
                    if (!logFile.trim().isEmpty()) {
                        files.add(logFile.trim());
                    }
                }
            }
        } catch (JSchException e) {
            logger.error("Error finding log files with buffer {}", bufferMinutes, e);
        } catch (ParseException e) {
            logger.error("Error parsing date for time buffer adjustment", e);
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
        }
        
        return files;
    }
    
    /**
     * Adjust time with buffer (add or subtract minutes)
     * @param timeStr Time string in format "yyyy-MM-dd HH:mm:ss"
     * @param minutes Minutes to add (positive) or subtract (negative)
     * @return Adjusted time string
     */
    private String adjustTimeWithBuffer(String timeStr, int minutes) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = format.parse(timeStr);
        
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MINUTE, minutes);
        
        return format.format(calendar.getTime());
    }
    
    /**
     * Download a file from the SFTP server
     * @param serverType "staging" or "production"
     * @param remotePath Remote file path
     * @return File content as byte array
     */
    public byte[] downloadFile(String serverType, String remotePath) {
        Session session = null;
        ChannelSftp channel = null;
        
        try {
            session = createSftpSession(serverType);
            
            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();
            
            logger.info("Downloading file: {}", remotePath);
            
            // Download file
            InputStream inputStream = channel.get(remotePath);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            
            inputStream.close();
            return outputStream.toByteArray();
            
        } catch (JSchException | SftpException | IOException e) {
            logger.error("Error downloading file", e);
            return new byte[0];
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }
    }
}
