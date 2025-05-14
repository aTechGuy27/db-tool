package com.ev.tradeedge.marketconnect.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
            String path = "staging".equals(serverType) ? sftpConfig.getStagingPath() : sftpConfig.getProdPath();
            
            // Build find command
            String command = String.format(
                "find %s -type f -name \"%s*\" -newermt \"%s\" ! -newermt \"%s\"",
                path, serviceName, startTime, endTime
            );
            
            logger.info("Executing command: {}", command);
            
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            
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
            
            // Parse output
            String output = outputStream.toString();
            String[] lines = output.split("\n");
            for (String line : lines) {
                if (line.trim().endsWith(".log") || line.trim().endsWith(".gz")) {
                    files.add(line.trim());
                }
            }
            
            logger.info("Found {} log files", files.size());
            
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