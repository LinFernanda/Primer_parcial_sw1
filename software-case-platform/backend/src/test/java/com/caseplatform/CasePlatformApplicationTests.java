package com.caseplatform;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:postgresql://localhost:5432/case_platform_db",
        "spring.datasource.username=postgres",
        "spring.datasource.password=password",
        "spring.jpa.hibernate.ddl-auto=none"
})
class CasePlatformApplicationTests {

    @Test
    void contextLoads() {
        // Verifies that the Spring Boot ApplicationContext starts up without errors
    }
}
