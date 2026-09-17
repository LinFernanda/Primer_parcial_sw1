package com.caseplatform.storage;

/**
 * Interfaz unificada de almacenamiento para la plataforma CASE.
 * Permite almacenar y recuperar imágenes UML, archivos XMI y código generado
 * de manera agnóstica entre Amazon AWS S3 y almacenamiento local persistente.
 */
public interface StorageService {

    /**
     * Sube un archivo al almacenamiento (S3 o Local).
     *
     * @param folder      Directorio o prefijo S3 (ej. "uml-diagrams", "xmi-exports", "generated-code")
     * @param filename    Nombre único del archivo
     * @param content     Bytes del archivo
     * @param contentType Tipo MIME (ej. "image/png", "application/xml", "application/zip")
     * @return Clave/URI pública o ruta de acceso del archivo almacenado
     */
    String uploadFile(String folder, String filename, byte[] content, String contentType);

    /**
     * Descarga el contenido binario de un archivo.
     *
     * @param pathOrKey Clave S3 o ruta local del archivo
     * @return Bytes del archivo
     */
    byte[] downloadFile(String pathOrKey);

    /**
     * Elimina un archivo del almacenamiento.
     *
     * @param pathOrKey Clave S3 o ruta local
     */
    void deleteFile(String pathOrKey);

    /**
     * Verifica la existencia de un archivo.
     *
     * @param pathOrKey Clave S3 o ruta local
     * @return true si existe, false en caso contrario
     */
    boolean exists(String pathOrKey);

    /**
     * Retorna el tipo de proveedor de almacenamiento activo.
     *
     * @return "AWS_S3" o "LOCAL_FILESYSTEM"
     */
    String getStorageType();
}
