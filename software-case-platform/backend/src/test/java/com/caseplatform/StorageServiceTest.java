package com.caseplatform;

import com.caseplatform.storage.AwsS3StorageService;
import com.caseplatform.storage.LocalStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class StorageServiceTest {

    private LocalStorageService localStorageService;
    private AwsS3StorageService awsS3StorageService;

    @BeforeEach
    void setUp() {
        localStorageService = new LocalStorageService();
        // Instanciar S3 con fallback local para testing
        awsS3StorageService = new AwsS3StorageService(
                "case-platform-test-bucket",
                "us-east-1",
                "",
                "",
                "",
                localStorageService
        );
    }

    @Test
    @DisplayName("LocalStorageService guarda, recupera y elimina archivos binarios")
    void testLocalStorageLifecycle() {
        String folder = "test-folder";
        String filename = "sample.txt";
        byte[] content = "Contenido de prueba UML".getBytes(StandardCharsets.UTF_8);

        String path = localStorageService.uploadFile(folder, filename, content, "text/plain");
        assertNotNull(path);
        assertTrue(localStorageService.exists(folder + "/" + filename));

        byte[] downloaded = localStorageService.downloadFile(folder + "/" + filename);
        assertArrayEquals(content, downloaded);

        localStorageService.deleteFile(folder + "/" + filename);
        assertFalse(localStorageService.exists(folder + "/" + filename));
    }

    @Test
    @DisplayName("AwsS3StorageService opera en modo fallback resiliente cuando no hay credenciales AWS")
    void testAwsS3StorageFallback() {
        assertFalse(awsS3StorageService.isAwsConfigured());
        assertEquals("LOCAL_FALLBACK", awsS3StorageService.getStorageType());

        String folder = "uml-diagrams";
        String filename = "diagram1.png";
        byte[] content = new byte[]{1, 2, 3, 4, 5};

        String urlOrPath = awsS3StorageService.uploadFile(folder, filename, content, "image/png");
        assertNotNull(urlOrPath);

        byte[] downloaded = awsS3StorageService.downloadFile(folder + "/" + filename);
        assertArrayEquals(content, downloaded);

        awsS3StorageService.deleteFile(folder + "/" + filename);
    }

    @Test
    @DisplayName("AwsS3StorageService genera URLs S3 bien formadas con region y bucket")
    void testS3UrlGeneration() {
        AwsS3StorageService s3Configured = new AwsS3StorageService(
                "production-bucket",
                "us-west-2",
                "MOCK_KEY",
                "MOCK_SECRET",
                "",
                localStorageService
        );

        assertTrue(s3Configured.isAwsConfigured());
        assertEquals("AWS_S3", s3Configured.getStorageType());

        String url = s3Configured.getS3ObjectUrl("xmi/export.xml");
        assertEquals("https://production-bucket.s3.us-west-2.amazonaws.com/xmi/export.xml", url);
    }
}
