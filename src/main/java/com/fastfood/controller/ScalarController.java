package com.fastfood.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
public class ScalarController {
    
    @GetMapping("/scalar")
    public ResponseEntity<Map<String, String>> scalar() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "scalar");
        response.put("time", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return ResponseEntity.ok(response);
    }
}
