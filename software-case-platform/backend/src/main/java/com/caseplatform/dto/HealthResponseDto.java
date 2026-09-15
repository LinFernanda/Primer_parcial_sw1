package com.caseplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthResponseDto {

    private String status;
    private String application;
    private String version;
    private String environment;
    private String databaseStatus;
    private Map<String, Object> systemDetails;
}
