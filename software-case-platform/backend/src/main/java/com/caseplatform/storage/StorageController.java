package com.caseplatform.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

/**
 * Controlador REST para operaciones de almacenamiento en AWS S3 y Local.
 * Permite subir diagramas UML, exportaciones XMI y artefactos empaquetados.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/storage")
@RequiredArgsConstructor
public class StorageController {

    private final StorageService storageService;

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStorageStatus() {
        return ResponseEntity.ok(Map.of(
                "status", "ACTIVE",
                "provider", storageService.getStorageType(),
                "timestamp", System.currentTimeMillis()
        ));
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "general") String folder
    ) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        String uniqueFilename = UUID.randomUUID().toString() + extension;

        String fileUrlOrPath = storageService.uploadFile(
                folder,
                uniqueFilename,
                file.getBytes(),
                file.getContentType() != null ? file.getContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE
        );

        return ResponseEntity.ok(Map.of(
                "success", true,
                "url", fileUrlOrPath,
                "filename", uniqueFilename,
                "folder", folder,
                "size", file.getSize(),
                "provider", storageService.getStorageType()
        ));
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadFile(@RequestParam("key") String key) {
        byte[] data = storageService.downloadFile(key);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + key.substring(key.lastIndexOf("/") + 1) + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }
}
