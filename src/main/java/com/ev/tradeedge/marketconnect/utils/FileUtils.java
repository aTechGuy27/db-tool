package com.ev.tradeedge.marketconnect.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Utility class for file operations
 */
@Component
public class FileUtils {
    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);
    
    /**
     * Read a text file into a string
     * @param filePath Path to the file
     * @return File content as string
     */
    public String readFileToString(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        return Files.readString(path, StandardCharsets.UTF_8);
    }
    
    /**
     * Write a string to a text file
     * @param content Content to write
     * @param filePath Path to the file
     */
    public void writeStringToFile(String content, String filePath) throws IOException {
        Path path = Paths.get(filePath);
        Files.writeString(path, content, StandardCharsets.UTF_8);
    }
    
    /**
     * Create a backup of a file
     * @param filePath Path to the file
     * @return Path to the backup file
     */
    public String createBackup(String filePath) throws IOException {
        Path source = Paths.get(filePath);
        if (!Files.exists(source)) {
            return null;
        }
        
        String backupPath = filePath + ".backup." + System.currentTimeMillis();
        Path target = Paths.get(backupPath);
        
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        logger.info("Created backup of {} at {}", filePath, backupPath);
        
        return backupPath;
    }
    
    /**
     * Extract content from a GZIP file
     * @param gzipFile Path to the GZIP file
     * @return Extracted content as string
     */
    public String extractGzipContent(String gzipFile) throws IOException {
        try (GZIPInputStream gis = new GZIPInputStream(new FileInputStream(gzipFile));
             BufferedReader br = new BufferedReader(new InputStreamReader(gis, StandardCharsets.UTF_8))) {
            
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n");
            }
            
            return content.toString();
        }
    }
    
    /**
     * Extract content from a GZIP byte array
     * @param compressedContent GZIP compressed content
     * @return Extracted content as string
     */
    public String extractGzipContent(byte[] compressedContent) throws IOException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(compressedContent);
             GZIPInputStream gis = new GZIPInputStream(bis);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gis.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            
            return bos.toString(StandardCharsets.UTF_8.name());
        }
    }
    
    /**
     * Find files in a directory matching a pattern
     * @param directory Directory to search
     * @param pattern File name pattern (glob)
     * @return List of matching file paths
     */
    public List<String> findFiles(String directory, String pattern) throws IOException {
        List<String> result = new ArrayList<>();
        
        Files.walk(Paths.get(directory))
            .filter(Files::isRegularFile)
            .filter(path -> path.getFileName().toString().matches(pattern))
            .forEach(path -> result.add(path.toString()));
        
        return result;
    }
    
    /**
     * Ensure a directory exists, creating it if necessary
     * @param directory Directory path
     */
    public void ensureDirectoryExists(String directory) throws IOException {
        Path path = Paths.get(directory);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
            logger.info("Created directory: {}", directory);
        }
    }
    
    /**
     * Delete a file
     * @param filePath Path to the file
     * @return true if the file was deleted, false otherwise
     */
    public boolean deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            logger.error("Error deleting file: {}", filePath, e);
            return false;
        }
    }
}