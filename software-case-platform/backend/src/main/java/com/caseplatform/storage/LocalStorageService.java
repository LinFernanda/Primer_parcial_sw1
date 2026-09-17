package com.caseplatform.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Implementación de almacenamiento en sistema de archivos local.
 * Utilizada para entornos de desarrollo, tests y como fallback resiliente.
 */
@Slf4j
@Service("localStorageService")
public class LocalStorageService implements StorageService {

    private final Path baseStoragePath;

    public LocalStorageService() {
        this.baseStoragePath = Paths.get("data", "storage").toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.baseStoragePath);
        } catch (IOException e) {
            log.warn("No se pudo crear el directorio base de almacenamiento local: {}", e.getMessage());
        }
    }

    @Override
    public String uploadFile(String folder, String filename, byte[] content, String contentType) {
        try {
            Path targetFolder = baseStoragePath.resolve(folder);
            Files.createDirectories(targetFolder);

            Path targetFile = targetFolder.resolve(filename);
            Files.write(targetFile, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            log.info("Archivo local guardado exitosamente: {}", targetFile);
            return folder + "/" + filename;
        } catch (IOException e) {
            log.error("Error al guardar archivo local {}: {}", filename, e.getMessage());
            throw new RuntimeException("Error guardando archivo en almacenamiento local", e);
        }
    }

    @Override
    public byte[] downloadFile(String pathOrKey) {
        try {
            Path filePath = baseStoragePath.resolve(pathOrKey).normalize();
            if (!Files.exists(filePath)) {
                throw new IllegalArgumentException("El archivo no existe: " + pathOrKey);
            }
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            log.error("Error al leer archivo local {}: {}", pathOrKey, e.getMessage());
            throw new RuntimeException("Error leyendo archivo local", e);
        }
    }

    @Override
    public void deleteFile(String pathOrKey) {
        try {
            Path filePath = baseStoragePath.resolve(pathOrKey).normalize();
            Files.deleteIfExists(filePath);
            log.info("Archivo local eliminado: {}", pathOrKey);
        } catch (IOException e) {
            log.error("Error al eliminar archivo local {}: {}", pathOrKey, e.getMessage());
        }
    }

    @Override
    public boolean exists(String pathOrKey) {
        Path filePath = baseStoragePath.resolve(pathOrKey).normalize();
        return Files.exists(filePath);
    }

    @Override
    public String getStorageType() {
        return "LOCAL_FILESYSTEM";
    }
}
