package com.caseplatform.controller;

import com.caseplatform.dto.ApiResponse;
import com.caseplatform.dto.HealthResponseDto;
import com.caseplatform.service.HealthService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class HealthController {

    private static final Logger log = LoggerFactory.getLogger(HealthController.class);

    private final HealthService healthService;

    @GetMapping("/ping")
    public ResponseEntity<ApiResponse<String>> ping() {
        log.info("Received ping request");
        return ResponseEntity.ok(ApiResponse.ok("pong", "Server is responsive"));
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<HealthResponseDto>> getHealth() {
        log.info("Received health status query");
        HealthResponseDto health = healthService.getSystemHealth();
        return ResponseEntity.ok(ApiResponse.ok(health, "CASE Platform Backend is healthy"));
    }

    @GetMapping("/info")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getInfo() {
        log.info("Received system metadata query");
        Map<String, Object> metadata = healthService.getSystemMetadata();
        return ResponseEntity.ok(ApiResponse.ok(metadata, "System metadata retrieved"));
    }
}
