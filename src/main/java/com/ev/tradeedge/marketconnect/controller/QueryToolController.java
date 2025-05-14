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

import com.ev.tradeedge.marketconnect.model.Database;
import com.ev.tradeedge.marketconnect.model.FileProcessEvent;
import com.ev.tradeedge.marketconnect.model.KillQueryRequest;
import com.ev.tradeedge.marketconnect.model.Query;
import com.ev.tradeedge.marketconnect.service.QueryToolService;

@RestController
@RequestMapping("/api/query-tool")
public class QueryToolController {

    private final QueryToolService queryToolService;

 
    public QueryToolController(QueryToolService queryToolService) {
        this.queryToolService = queryToolService;
    }

    @GetMapping("/servers")
    public ResponseEntity<Map<String, List<String>>> getServers(@RequestParam String type) {
        return ResponseEntity.ok(Map.of("servers", queryToolService.getServers(type)));
    }

    @GetMapping("/fpid")
    public ResponseEntity<Map<String, List<Database>>> getDatabasesByFpid(
            @RequestParam String id, 
            @RequestParam String server) {
        return ResponseEntity.ok(Map.of("databases", queryToolService.getDatabasesByFpid(id, server)));
    }

    @GetMapping("/server")
    public ResponseEntity<Map<String, List<Database>>> getDatabasesByServer(
            @RequestParam String name, 
            @RequestParam String type) {
        return ResponseEntity.ok(Map.of("databases", queryToolService.getDatabasesByServer(name, type)));
    }

    @GetMapping("/queries")
    public ResponseEntity<Map<String, List<Query>>> getQueries(
            @RequestParam String database, 
            @RequestParam String server) {
        return ResponseEntity.ok(Map.of("queries", queryToolService.getQueries(database, server)));
    }
    
    @GetMapping("/events")
    public ResponseEntity<Map<String, List<FileProcessEvent>>> getFileProcessEvents(
            @RequestParam String fileProcessId, 
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        
        List<FileProcessEvent> events = queryToolService.getFileProcessEvents(
            Long.parseLong(fileProcessId), startTime, endTime);
        
        return ResponseEntity.ok(Map.of("events", events));
    }

    @PostMapping("/kill")
    public ResponseEntity<Map<String, Object>> killQueries(@RequestBody KillQueryRequest request) {
        queryToolService.killQueries(request);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("killedQueries", request.getQueryIds());
        
        return ResponseEntity.ok(response);
    }
}