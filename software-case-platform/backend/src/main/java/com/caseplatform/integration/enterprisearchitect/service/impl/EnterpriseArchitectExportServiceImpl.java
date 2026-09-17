package com.caseplatform.integration.enterprisearchitect.service.impl;

import com.caseplatform.exception.ResourceNotFoundException;
import com.caseplatform.integration.enterprisearchitect.dto.XMIExportResponseDTO;
import com.caseplatform.integration.enterprisearchitect.exporter.XMIExporter;
import com.caseplatform.integration.enterprisearchitect.service.EnterpriseArchitectExportService;
import com.caseplatform.model.ClaseUML;
import com.caseplatform.model.ModeloUML;
import com.caseplatform.model.RelacionUML;
import com.caseplatform.repository.ClaseUMLRepository;
import com.caseplatform.repository.ModeloUMLRepository;
import com.caseplatform.repository.RelacionUMLRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Implementación del servicio de exportación de modelos UML al formato XMI de Enterprise Architect.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EnterpriseArchitectExportServiceImpl implements EnterpriseArchitectExportService {

    private final ModeloUMLRepository modeloUMLRepository;
    private final ClaseUMLRepository claseUMLRepository;
    private final RelacionUMLRepository relacionUMLRepository;
    private final XMIExporter xmiExporter;

    @Override
    @Transactional(readOnly = true)
    public XMIExportResponseDTO exportModel(Long modeloId, String usuarioEmail) {
        log.info("Generando exportación XMI para el modelo ID: {} por usuario: {}", modeloId, usuarioEmail);

        ModeloUML modelo = modeloUMLRepository.findById(modeloId)
                .orElseThrow(() -> new ResourceNotFoundException("Modelo UML no encontrado con ID: " + modeloId));

        List<ClaseUML> clases = claseUMLRepository.findByModeloUMLId(modeloId);
        List<RelacionUML> relaciones = relacionUMLRepository.findByModeloUMLId(modeloId);

        modelo.setClases(clases);
        modelo.setRelaciones(relaciones);

        String xmlContent = xmiExporter.exportToXMI(modelo);
        byte[] bytes = xmlContent.getBytes(StandardCharsets.UTF_8);

        String sanitizedModelName = modelo.getNombre() != null
                ? modelo.getNombre().replaceAll("[^a-zA-Z0-9_\\-]", "_")
                : "modelo_ea";

        return XMIExportResponseDTO.builder()
                .nombreArchivo(sanitizedModelName + "_ea_xmi21.xml")
                .versionXMI("2.1")
                .totalClases(clases.size())
                .totalRelaciones(relaciones.size())
                .tamanoBytes(bytes.length)
                .contenidoXML(xmlContent)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportModelAsBytes(Long modeloId, String usuarioEmail) {
        XMIExportResponseDTO dto = exportModel(modeloId, usuarioEmail);
        return dto.getContenidoXML().getBytes(StandardCharsets.UTF_8);
    }
}
