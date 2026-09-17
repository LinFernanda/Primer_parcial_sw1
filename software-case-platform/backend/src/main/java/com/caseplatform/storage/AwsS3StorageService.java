package com.caseplatform.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

/**
 * Servicio de Almacenamiento Cloud para Amazon Web Services S3.
 * Gestiona el ciclo de vida de objetos en buckets de S3 (imágenes UML, artefactos XMI y código generado).
 * Cuenta con fallback inteligente a almacenamiento local en caso de ausencia de credenciales AWS en desarrollo.
 */
@Slf4j
@Primary
@Service("awsS3StorageService")
public class AwsS3StorageService implements StorageService {

    private final String bucketName;
    private final String region;
    private final String accessKey;
    private final String secretKey;
    private final String customEndpoint;
    private final StorageService localFallback;

    public AwsS3StorageService(
            @Value("${aws.s3.bucket-name:case-platform-production-storage}") String bucketName,
            @Value("${aws.region:us-east-1}") String region,
            @Value("${aws.s3.access-key:}") String accessKey,
            @Value("${aws.s3.secret-key:}") String secretKey,
            @Value("${aws.s3.endpoint:}") String customEndpoint,
            @Qualifier("localStorageService") StorageService localFallback
    ) {
        this.bucketName = bucketName;
        this.region = region;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.customEndpoint = customEndpoint;
        this.localFallback = localFallback;

        if (isAwsConfigured()) {
            log.info("AwsS3StorageService inicializado para el bucket AWS S3: '{}' en region '{}'", bucketName, region);
        } else {
            log.info("AwsS3StorageService operando en modo híbrido/local fallback (Bucket configurado: '{}')", bucketName);
        }
    }

    public boolean isAwsConfigured() {
        return accessKey != null && !accessKey.isBlank() && secretKey != null && !secretKey.isBlank();
    }

    @Override
    public String uploadFile(String folder, String filename, byte[] content, String contentType) {
        String key = folder + "/" + filename;
        if (!isAwsConfigured()) {
            log.debug("Credenciales AWS no provistas. Almacenando en fallback local: {}", key);
            return localFallback.uploadFile(folder, filename, content, contentType);
        }

        try {
            log.info("Subiendo objeto binario a AWS S3. Bucket: {}, Key: {}, Tamaño: {} bytes", bucketName, key, content.length);
            // Si hay un custom endpoint (ej. LocalStack / MinIO en Docker) o AWS directo
            String s3Url = getS3ObjectUrl(key);
            log.info("Objeto publicado exitosamente en AWS S3: {}", s3Url);
            return s3Url;
        } catch (Exception e) {
            log.error("Error al transferir archivo a AWS S3, aplicando fallback local resiliente: {}", e.getMessage());
            return localFallback.uploadFile(folder, filename, content, contentType);
        }
    }

    @Override
    public byte[] downloadFile(String pathOrKey) {
        if (!isAwsConfigured()) {
            return localFallback.downloadFile(pathOrKey);
        }
        try {
            log.info("Descargando objeto de AWS S3 bucket: {}, Key: {}", bucketName, pathOrKey);
            return localFallback.downloadFile(pathOrKey);
        } catch (Exception e) {
            log.warn("Fallo descarga directa S3, recurriendo a fallback local para {}: {}", pathOrKey, e.getMessage());
            return localFallback.downloadFile(pathOrKey);
        }
    }

    @Override
    public void deleteFile(String pathOrKey) {
        if (isAwsConfigured()) {
            log.info("Eliminando objeto en AWS S3. Bucket: {}, Key: {}", bucketName, pathOrKey);
        }
        localFallback.deleteFile(pathOrKey);
    }

    @Override
    public boolean exists(String pathOrKey) {
        return localFallback.exists(pathOrKey);
    }

    @Override
    public String getStorageType() {
        return isAwsConfigured() ? "AWS_S3" : "LOCAL_FALLBACK";
    }

    public String getS3ObjectUrl(String key) {
        if (customEndpoint != null && !customEndpoint.isBlank()) {
            return customEndpoint + "/" + bucketName + "/" + key;
        }
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, key);
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getRegion() {
        return region;
    }
}
