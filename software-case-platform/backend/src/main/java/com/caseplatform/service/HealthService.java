package com.caseplatform.service;

import com.caseplatform.dto.HealthResponseDto;

import java.util.Map;

public interface HealthService {

    HealthResponseDto getSystemHealth();

    Map<String, Object> getSystemMetadata();
}
