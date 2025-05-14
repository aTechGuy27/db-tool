package com.ev.tradeedge.marketconnect.controller;


import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ev.tradeedge.marketconnect.model.Config;
import com.ev.tradeedge.marketconnect.service.ConfigService;

@RestController
@RequestMapping("/api/config")
public class ConfigController {

    private final ConfigService configService;

    public ConfigController(ConfigService configService) {
        this.configService = configService;
    }

    @GetMapping
    public ResponseEntity<Config> getConfig() {
        return ResponseEntity.ok(configService.getConfig());
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> saveConfig(@RequestBody Config config) {
        configService.saveConfig(config);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        
        return ResponseEntity.ok(response);
    }
}