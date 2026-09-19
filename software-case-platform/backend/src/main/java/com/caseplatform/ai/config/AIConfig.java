package com.caseplatform.ai.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class AIConfig {

    @Value("${ai.openai.api-key:}")
    private String apiKey;

    @Value("${ai.openai.model:gpt-4o-mini}")
    private String model = "gpt-4o-mini";

    @Value("${ai.openai.base-url:https://api.openai.com/v1}")
    private String baseUrl = "https://api.openai.com/v1";

    @Value("${ai.gemini.api-key:}")
    private String geminiApiKey;

    @Value("${ai.gemini.model:gemini-flash-lite-latest}")
    private String geminiModel = "gemini-flash-lite-latest";
}
