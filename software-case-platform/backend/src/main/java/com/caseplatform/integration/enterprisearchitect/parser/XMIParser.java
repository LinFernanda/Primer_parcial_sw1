package com.caseplatform.integration.enterprisearchitect.parser;

import com.caseplatform.integration.enterprisearchitect.model.UMLImportModel;
import com.caseplatform.integration.enterprisearchitect.model.UMLImportValidationResult;

import java.io.InputStream;

/**
 * Contrato para el analizador de archivos XML/XMI de Enterprise Architect.
 */
public interface XMIParser {

    /**
     * Analiza un flujo de entrada XML/XMI y construye el modelo intermedio UMLImportModel.
     *
     * @param inputStream flujo de bytes del archivo XML
     * @return UMLImportModel con clases, atributos, métodos y relaciones
     * @throws Exception si el archivo XML es inválido o no contiene estructura XMI reconocible
     */
    UMLImportModel parse(InputStream inputStream) throws Exception;

    /**
     * Analiza una cadena con el contenido XML/XMI.
     *
     * @param xmlContent contenido textual XML
     * @return UMLImportModel
     * @throws Exception si el archivo XML es inválido
     */
    UMLImportModel parse(String xmlContent) throws Exception;

    /**
     * Valida la estructura y consistencia del modelo XMI sin persistirlo.
     *
     * @param model modelo intermedio analizado
     * @return resultado de validación con errores, advertencias y métricas
     */
    UMLImportValidationResult validate(UMLImportModel model);
}
