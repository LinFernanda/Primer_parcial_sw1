package com.caseplatform.integration.enterprisearchitect.exporter;

import com.caseplatform.model.ModeloUML;

/**
 * Contrato para exportar modelos conceptuales UML al formato XMI compatible con Enterprise Architect.
 */
public interface XMIExporter {

    /**
     * Genera la representación textual XMI 2.1 estándar a partir de la entidad ModeloUML.
     *
     * @param modelo entidad ModeloUML con clases, atributos, métodos y relaciones cargadas
     * @return documento XML/XMI en formato String
     */
    String exportToXMI(ModeloUML modelo);
}
