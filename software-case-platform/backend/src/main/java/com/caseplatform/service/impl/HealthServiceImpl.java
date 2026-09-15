package com.caseplatform.service.impl;

import com.caseplatform.dto.HealthResponseDto;
import com.caseplatform.model.SystemCheck;
import com.caseplatform.repository.SystemCheckRepository;
import com.caseplatform.service.HealthService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HealthServiceImpl implements HealthService {

    private static final Logger log = LoggerFactory.getLogger(HealthServiceImpl.class);

    private final DataSource dataSource;
    private final SystemCheckRepository systemCheckRepository;

    @Value("${spring.application.name:case-platform-backend}")
    private String applicationName;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @Override
    @Transactional
    public HealthResponseDto getSystemHealth() {
        String dbStatus = checkDatabaseHealth();

        // Record a heartbeat check in the database
        try {
            SystemCheck check = systemCheckRepository.findByCheckName("SYSTEM_INIT_PHASE_1")
                    .orElse(SystemCheck.builder()
                            .checkName("SYSTEM_INIT_PHASE_1")
                            .status("HEALTHY")
                            .createdAt(OffsetDateTime.now())
                            .build());
            check.setStatus("HEALTHY");
            check.setCreatedAt(OffsetDateTime.now());
            systemCheckRepository.save(check);
        } catch (Exception e) {
            log.warn("Could not persist system check heartbeat: {}", e.getMessage());
        }

        Map<String, Object> details = new HashMap<>();
        details.put("javaVersion", System.getProperty("java.version"));
        details.put("javaVendor", System.getProperty("java.vendor"));
        details.put("osName", System.getProperty("os.name"));
        details.put("totalMemoryMB", Runtime.getRuntime().totalMemory() / (1024 * 1024));
        details.put("freeMemoryMB", Runtime.getRuntime().freeMemory() / (1024 * 1024));

        return HealthResponseDto.builder()
                .status("UP")
                .application(applicationName)
                .version("1.0.0-SNAPSHOT")
                .environment(activeProfile)
                .databaseStatus(dbStatus)
                .systemDetails(details)
                .build();
    }

    @Override
    public Map<String, Object> getSystemMetadata() {
        Map<String, Object> meta = new HashMap<>();
        meta.put("name", "CASE Platform Backend");
        meta.put("phase", "FASE 1 - Preparacion de Proyecto y Arquitectura Base");
        meta.put("status", "ACTIVE");
        meta.put("capabilities", new String[]{
                "UML 2.5 Modeling Ready",
                "Realtime Engine Ready",
                "Code Generator Architecture Ready",
                "RESTful API Ready",
                "PostgreSQL Persistence Active"
        });
        return meta;
    }

    private String checkDatabaseHealth() {
        try (Connection conn = dataSource.getConnection()) {
            if (conn.isValid(2)) {
                return "CONNECTED (" + conn.getMetaData().getDatabaseProductName() + " " +
                        conn.getMetaData().getDatabaseProductVersion() + ")";
            }
            return "UNKNOWN";
        } catch (Exception e) {
            log.error("Database connection failure: {}", e.getMessage());
            return "DISCONNECTED: " + e.getMessage();
        }
    }
}
