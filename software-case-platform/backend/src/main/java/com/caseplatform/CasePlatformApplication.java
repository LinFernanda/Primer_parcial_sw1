package com.caseplatform;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;

@SpringBootApplication
public class CasePlatformApplication {

    private static final Logger log = LoggerFactory.getLogger(CasePlatformApplication.class);

    public static void main(String[] args) {
        loadEnvironmentVariables();
        SpringApplication.run(CasePlatformApplication.class, args);
        log.info("=================================================================");
        log.info("  CASE Platform Backend - Phase 1 Initialized Successfully       ");
        log.info("=================================================================");
    }

    private static void loadEnvironmentVariables() {
        try {
            // Check current directory, then parent directory for .env file
            Dotenv dotenv = null;
            if (new File(".env").exists()) {
                dotenv = Dotenv.configure().ignoreIfMissing().load();
            } else if (new File("../.env").exists()) {
                dotenv = Dotenv.configure().directory("../").ignoreIfMissing().load();
            }

            if (dotenv != null) {
                dotenv.entries().forEach(entry -> {
                    if (System.getProperty(entry.getKey()) == null && System.getenv(entry.getKey()) == null) {
                        System.setProperty(entry.getKey(), entry.getValue());
                    }
                });
                log.info(".env configuration loaded successfully.");
            }
        } catch (Exception e) {
            log.warn("Notice: Could not load .env file directly, relying on system environment: {}", e.getMessage());
        }
    }
}
