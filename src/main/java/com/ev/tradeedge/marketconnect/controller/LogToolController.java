package com.ev.tradeedge.marketconnect.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ev.tradeedge.marketconnect.model.LogExtractRequest;
import com.ev.tradeedge.marketconnect.model.Service;
import com.ev.tradeedge.marketconnect.service.LogToolService;

@RestController
@RequestMapping("/api/log-tool")
public class LogToolController {

    private final LogToolService logToolService;

  
    public LogToolController(LogToolService logToolService) {
        this.logToolService = logToolService;
    }

    @GetMapping("/services")
    public ResponseEntity<Map<String, Object>> getServices(
            @RequestParam String fpid, 
            @RequestParam String server) {
        List<Service> services = logToolService.getServices(fpid, server);
        Map<String, String> timeRange = logToolService.getTimeRange(fpid, server);
        
        Map<String, Object> response = new HashMap<>();
        response.put("services", services);
        response.put("startTime", timeRange.get("startTime"));
        response.put("endTime", timeRange.get("endTime"));
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logs")
    public ResponseEntity<Map<String, String>> extractLogs(@RequestBody LogExtractRequest request) {
        String logs = logToolService.extractLogs(request);
        
        Map<String, String> response = new HashMap<>();
        response.put("logs", logs);
        
        return ResponseEntity.ok(response);
    }
}