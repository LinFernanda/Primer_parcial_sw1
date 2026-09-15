package com.caseplatform.exception;

/**
 * Excepción para errores de validación de negocio (ej. email duplicado, credenciales inválidas, reglas de negocio).
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
