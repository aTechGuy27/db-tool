package com.ev.tradeedge.marketconnect.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ev.tradeedge.marketconnect.model.Config;
import com.ev.tradeedge.marketconnect.service.ConfigService;

@RestController
@RequestMapping("/api/server-mappings")
public class ServerMappingController {

    private final ConfigService configService;

    public ServerMappingController(ConfigService configService) {
        this.configService = configService;
    }

    @GetMapping
    public ResponseEntity<Map<String, String>> getServerMappings() {
        return ResponseEntity.ok(configService.getConfig().getServerMappings());
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> saveServerMappings(@RequestBody Map<String, String> mappings) {
        Config config = configService.getConfig();
        config.setServerMappings(mappings);
        configService.saveConfig(config);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Server mappings saved successfully");
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{serverName}")
    public ResponseEntity<Map<String, Object>> addServerMapping(
            @PathVariable String serverName,
            @RequestBody Map<String, String> body) {
        
        String ipAddress = body.get("ipAddress");
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "IP address is required");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        Config config = configService.getConfig();
        config.getServerMappings().put(serverName, ipAddress);
        configService.saveConfig(config);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Server mapping added successfully");
        
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{serverName}")
    public ResponseEntity<Map<String, Object>> deleteServerMapping(@PathVariable String serverName) {
        Config config = configService.getConfig();
        config.getServerMappings().remove(serverName);
        configService.saveConfig(config);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Server mapping deleted successfully");
        
        return ResponseEntity.ok(response);
    }
}