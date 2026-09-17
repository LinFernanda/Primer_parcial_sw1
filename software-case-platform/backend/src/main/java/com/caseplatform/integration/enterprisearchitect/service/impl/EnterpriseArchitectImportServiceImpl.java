package com.caseplatform.integration.enterprisearchitect.service.impl;

import com.caseplatform.exception.ResourceNotFoundException;
import com.caseplatform.integration.enterprisearchitect.dto.XMIImportResponseDTO;
import com.caseplatform.integration.enterprisearchitect.dto.XMIValidationResponseDTO;
import com.caseplatform.integration.enterprisearchitect.model.UMLImportAttribute;
import com.caseplatform.integration.enterprisearchitect.model.UMLImportClass;
import com.caseplatform.integration.enterprisearchitect.model.UMLImportMethod;
import com.caseplatform.integration.enterprisearchitect.model.UMLImportModel;
import com.caseplatform.integration.enterprisearchitect.model.UMLImportRelation;
import com.caseplatform.integration.enterprisearchitect.model.UMLImportValidationResult;
import com.caseplatform.integration.enterprisearchitect.parser.XMIParser;
import com.caseplatform.integration.enterprisearchitect.service.EnterpriseArchitectImportService;
import com.caseplatform.model.AtributoUML;
import com.caseplatform.model.ClaseUML;
import com.caseplatform.model.MetodoUML;
import com.caseplatform.model.ModeloUML;
import com.caseplatform.model.ProyectoUML;
import com.caseplatform.model.RelacionUML;
import com.caseplatform.model.TipoRelacionUML;
import com.caseplatform.model.VisibilidadUML;
import com.caseplatform.repository.AtributoUMLRepository;
import com.caseplatform.repository.ClaseUMLRepository;
import com.caseplatform.repository.MetodoUMLRepository;
import com.caseplatform.repository.ModeloUMLRepository;
import com.caseplatform.repository.ProyectoUMLRepository;
import com.caseplatform.repository.RelacionUMLRepository;
import com.caseplatform.versioning.dto.CreateVersionDTO;
import com.caseplatform.versioning.dto.VersionDTO;
import com.caseplatform.versioning.model.TipoOperacionHistorial;
import com.caseplatform.versioning.service.VersionService;
import com.caseplatform.websocket.model.TipoOperacionUML;
import com.caseplatform.websocket.model.UMLEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementación del servicio de importación XMI desde Enterprise Architect.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EnterpriseArchitectImportServiceImpl implements EnterpriseArchitectImportService {

    private final XMIParser xmiParser;
    private final ModeloUMLRepository modeloUMLRepository;
    private final ProyectoUMLRepository proyectoUMLRepository;
    private final ClaseUMLRepository claseUMLRepository;
    private final AtributoUMLRepository atributoUMLRepository;
    private final MetodoUMLRepository metodoUMLRepository;
    private final RelacionUMLRepository relacionUMLRepository;

    @Autowired(required = false)
    private VersionService versionService;

    @Autowired(required = false)
    private ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    public XMIValidationResponseDTO validateXMI(InputStream xmiStream) {
        try {
            UMLImportModel model = xmiParser.parse(xmiStream);
            UMLImportValidationResult validation = xmiParser.validate(model);

            return XMIValidationResponseDTO.builder()
                    .valido(validation.isValido())
                    .mensaje(validation.isValido() ? "Archivo XMI validado exitosamente" : "Se detectaron inconsistencias en el archivo XMI")
                    .validacion(validation)
                    .preview(model)
                    .build();
        } catch (Exception e) {
            log.error("Error al analizar y validar el archivo XMI: {}", e.getMessage(), e);
            List<String> errores = List.of("Error de lectura XMI: " + e.getMessage());
            return XMIValidationResponseDTO.builder()
                    .valido(false)
                    .mensaje("El archivo no posee un formato XML/XMI válido o compatible")
                    .validacion(UMLImportValidationResult.builder()
                            .valido(false)
                            .errores(errores)
                            .build())
                    .build();
        }
    }

    @Override
    @Transactional
    public XMIImportResponseDTO importToExistingModel(Long modeloId, InputStream xmiStream, String fileName, boolean limpiarExistente, String usuarioEmail) {
        log.info("Iniciando importación XMI en modelo ID: {} por usuario: {}", modeloId, usuarioEmail);

        ModeloUML modelo = modeloUMLRepository.findById(modeloId)
                .orElseThrow(() -> new ResourceNotFoundException("Modelo UML no encontrado con ID: " + modeloId));

        UMLImportModel parsedModel;
        try {
            parsedModel = xmiParser.parse(xmiStream);
        } catch (Exception e) {
            log.error("Fallo al parsear archivo XMI: {}", e.getMessage());
            throw new IllegalArgumentException("El archivo proporcionado no pudo ser interpretado como XMI: " + e.getMessage());
        }

        UMLImportValidationResult validation = xmiParser.validate(parsedModel);
        if (!validation.isValido()) {
            String errorMsg = "Error de importación. Elemento inválido: " + String.join(", ", validation.getErrores());
            log.warn("Rechazando importación XMI: {}", errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }

        if (limpiarExistente) {
            log.info("Limpiando clases y relaciones existentes en el modelo ID: {}", modeloId);
            relacionUMLRepository.deleteAll(relacionUMLRepository.findByModeloUMLId(modeloId));
            claseUMLRepository.deleteAll(claseUMLRepository.findByModeloUMLId(modeloId));
        }

        int[] metrics = applyImportedModel(modelo, parsedModel);

        // 10. Integración con Versiones e Historial (Fase 6)
        Long versionId = null;
        String versionNumero = "v1.0";
        if (versionService != null) {
            try {
                String desc = String.format("Importada desde Enterprise Architect (%s). Clases: %d, Atributos: %d, Métodos: %d, Relaciones: %d",
                        fileName != null ? fileName : "archivo.xmi", metrics[0], metrics[1], metrics[2], metrics[3]);

                CreateVersionDTO versionDTO = CreateVersionDTO.builder()
                        .modeloId(modeloId)
                        .nombreVersion("Versión Importada desde Enterprise Architect (" + (fileName != null ? fileName : "XMI") + ")")
                        .descripcion(desc)
                        .build();

                VersionDTO vResp = versionService.crearVersion(versionDTO, usuarioEmail);
                if (vResp != null) {
                    versionId = vResp.getId();
                    versionNumero = vResp.getNumeroVersion();
                }

                versionService.registrarCambio(modeloId, TipoOperacionHistorial.CREATE,
                        "Modelo UML Importado", modeloId.toString(),
                        null, desc, usuarioEmail);
            } catch (Exception e) {
                log.warn("No se pudo registrar la versión de importación XMI: {}", e.getMessage());
            }
        }

        // 11. Integración con Colaboración en Tiempo Real WebSocket (Fase 5)
        if (eventPublisher != null) {
            try {
                UMLEvent event = UMLEvent.builder()
                        .modeloUMLId(modeloId)
                        .tipoOperacion(TipoOperacionUML.UPDATE)
                        .usuario(usuarioEmail)
                        .datosCambio(Map.of(
                                "accion", "IMPORT_ENTERPRISE_ARCHITECT",
                                "archivo", fileName != null ? fileName : "archivo.xmi",
                                "clasesImportadas", metrics[0],
                                "relacionesImportadas", metrics[3]
                        ))
                        .build();
                eventPublisher.publishEvent(event);
            } catch (Exception e) {
                log.warn("No se pudo notificar el evento WebSocket de importación: {}", e.getMessage());
            }
        }

        return XMIImportResponseDTO.builder()
                .exito(true)
                .mensaje("Modelo UML importado exitosamente desde Enterprise Architect")
                .modeloId(modelo.getId())
                .proyectoId(modelo.getProyecto() != null ? modelo.getProyecto().getId() : null)
                .nombreModelo(modelo.getNombre())
                .versionId(versionId)
                .versionNumero(versionNumero)
                .clasesImportadas(metrics[0])
                .atributosImportados(metrics[1])
                .metodosImportados(metrics[2])
                .relacionesImportadas(metrics[3])
                .archivoOrigen(fileName)
                .fecha(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional
    public XMIImportResponseDTO importAsNewModel(Long proyectoId, InputStream xmiStream, String fileName, String nombreModelo, String usuarioEmail) {
        log.info("Iniciando importación XMI como nuevo modelo en proyecto ID: {} por usuario: {}", proyectoId, usuarioEmail);

        ProyectoUML proyecto = proyectoUMLRepository.findById(proyectoId)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto UML no encontrado con ID: " + proyectoId));

        UMLImportModel parsedModel;
        try {
            parsedModel = xmiParser.parse(xmiStream);
        } catch (Exception e) {
            throw new IllegalArgumentException("El archivo proporcionado no pudo ser interpretado como XMI: " + e.getMessage());
        }

        UMLImportValidationResult validation = xmiParser.validate(parsedModel);
        if (!validation.isValido()) {
            throw new IllegalArgumentException("Error de importación. Elemento inválido: " + String.join(", ", validation.getErrores()));
        }

        String finalName = (nombreModelo != null && !nombreModelo.isBlank())
                ? nombreModelo
                : (parsedModel.getNombre() != null && !parsedModel.getNombre().isBlank())
                ? parsedModel.getNombre()
                : ("Modelo_EA_" + System.currentTimeMillis());

        ModeloUML nuevoModelo = ModeloUML.builder()
                .nombre(finalName)
                .version("1.0")
                .proyecto(proyecto)
                .clases(new ArrayList<>())
                .relaciones(new ArrayList<>())
                .build();

        nuevoModelo = modeloUMLRepository.save(nuevoModelo);

        int[] metrics = applyImportedModel(nuevoModelo, parsedModel);

        Long versionId = null;
        String versionNumero = "v1.0";
        if (versionService != null) {
            try {
                String desc = String.format("Nuevo modelo creado a partir de Enterprise Architect (%s). Clases: %d, Relaciones: %d",
                        fileName != null ? fileName : "archivo.xmi", metrics[0], metrics[3]);

                CreateVersionDTO vDto = CreateVersionDTO.builder()
                        .modeloId(nuevoModelo.getId())
                        .nombreVersion("Versión 1.0 Inicial (Importada de EA)")
                        .descripcion(desc)
                        .build();

                VersionDTO vResp = versionService.crearVersion(vDto, usuarioEmail);
                if (vResp != null) {
                    versionId = vResp.getId();
                    versionNumero = vResp.getNumeroVersion();
                }
            } catch (Exception e) {
                log.warn("Aviso al crear versión inicial: {}", e.getMessage());
            }
        }

        return XMIImportResponseDTO.builder()
                .exito(true)
                .mensaje("Nuevo modelo UML creado e importado exitosamente desde Enterprise Architect")
                .modeloId(nuevoModelo.getId())
                .proyectoId(proyecto.getId())
                .nombreModelo(nuevoModelo.getNombre())
                .versionId(versionId)
                .versionNumero(versionNumero)
                .clasesImportadas(metrics[0])
                .atributosImportados(metrics[1])
                .metodosImportados(metrics[2])
                .relacionesImportadas(metrics[3])
                .archivoOrigen(fileName)
                .fecha(LocalDateTime.now())
                .build();
    }

    private int[] applyImportedModel(ModeloUML modelo, UMLImportModel parsedModel) {
        Map<String, ClaseUML> clasesGuardadasPorId = new HashMap<>();
        Map<String, ClaseUML> clasesGuardadasPorNombre = new HashMap<>();

        int totalAtributos = 0;
        int totalMetodos = 0;

        // 1. Guardar Clases, Atributos y Métodos
        for (UMLImportClass cDto : parsedModel.getClases()) {
            ClaseUML clase = claseUMLRepository.findByModeloUMLIdAndNombreIgnoreCase(modelo.getId(), cDto.getNombre())
                    .orElse(null);

            if (clase == null) {
                clase = ClaseUML.builder()
                        .nombre(cDto.getNombre())
                        .visibilidad(cDto.getVisibilidad() != null ? cDto.getVisibilidad() : VisibilidadUML.PUBLIC)
                        .descripcion(cDto.getDescripcion())
                        .posicionX(cDto.getPosicionX() != null ? cDto.getPosicionX() : 100.0)
                        .posicionY(cDto.getPosicionY() != null ? cDto.getPosicionY() : 100.0)
                        .modeloUML(modelo)
                        .atributos(new ArrayList<>())
                        .metodos(new ArrayList<>())
                        .build();
                clase = claseUMLRepository.save(clase);
            } else {
                clase.setVisibilidad(cDto.getVisibilidad() != null ? cDto.getVisibilidad() : clase.getVisibilidad());
                if (cDto.getPosicionX() != null) clase.setPosicionX(cDto.getPosicionX());
                if (cDto.getPosicionY() != null) clase.setPosicionY(cDto.getPosicionY());
                clase = claseUMLRepository.save(clase);
            }

            // Atributos
            if (cDto.getAtributos() != null) {
                for (UMLImportAttribute aDto : cDto.getAtributos()) {
                    final ClaseUML finalClase = clase;
                    boolean attrExiste = finalClase.getAtributos().stream()
                            .anyMatch(a -> a.getNombre().equalsIgnoreCase(aDto.getNombre()));
                    if (!attrExiste) {
                        AtributoUML attr = AtributoUML.builder()
                                .nombre(aDto.getNombre())
                                .tipoDato(aDto.getTipoDato() != null ? aDto.getTipoDato() : "String")
                                .visibilidad(aDto.getVisibilidad() != null ? aDto.getVisibilidad() : VisibilidadUML.PRIVATE)
                                .valorInicial(aDto.getValorInicial())
                                .claseUML(clase)
                                .build();
                        atributoUMLRepository.save(attr);
                        clase.getAtributos().add(attr);
                        totalAtributos++;
                    }
                }
            }

            // Métodos
            if (cDto.getMetodos() != null) {
                for (UMLImportMethod mDto : cDto.getMetodos()) {
                    final ClaseUML finalClase = clase;
                    boolean metodoExiste = finalClase.getMetodos().stream()
                            .anyMatch(m -> m.getNombre().equalsIgnoreCase(mDto.getNombre()));
                    if (!metodoExiste) {
                        MetodoUML metodo = MetodoUML.builder()
                                .nombre(mDto.getNombre())
                                .tipoRetorno(mDto.getTipoRetorno() != null ? mDto.getTipoRetorno() : "void")
                                .visibilidad(mDto.getVisibilidad() != null ? mDto.getVisibilidad() : VisibilidadUML.PUBLIC)
                                .parametros(mDto.getParametros())
                                .claseUML(clase)
                                .build();
                        metodoUMLRepository.save(metodo);
                        clase.getMetodos().add(metodo);
                        totalMetodos++;
                    }
                }
            }

            if (cDto.getXmiId() != null) {
                clasesGuardadasPorId.put(cDto.getXmiId(), clase);
            }
            clasesGuardadasPorNombre.put(clase.getNombre().toLowerCase().trim(), clase);
        }

        // 2. Guardar Relaciones
        int totalRelaciones = 0;
        if (parsedModel.getRelaciones() != null) {
            for (UMLImportRelation rDto : parsedModel.getRelaciones()) {
                ClaseUML origen = null;
                if (rDto.getClaseOrigenId() != null) {
                    origen = clasesGuardadasPorId.get(rDto.getClaseOrigenId());
                }
                if (origen == null && rDto.getClaseOrigenNombre() != null) {
                    origen = clasesGuardadasPorNombre.get(rDto.getClaseOrigenNombre().toLowerCase().trim());
                }

                ClaseUML destino = null;
                if (rDto.getClaseDestinoId() != null) {
                    destino = clasesGuardadasPorId.get(rDto.getClaseDestinoId());
                }
                if (destino == null && rDto.getClaseDestinoNombre() != null) {
                    destino = clasesGuardadasPorNombre.get(rDto.getClaseDestinoNombre().toLowerCase().trim());
                }

                if (origen != null && destino != null && !origen.getId().equals(destino.getId())) {
                    final Long oId = origen.getId();
                    final Long dId = destino.getId();
                    final TipoRelacionUML tipo = rDto.getTipoRelacion() != null ? rDto.getTipoRelacion() : TipoRelacionUML.ASOCIACION;

                    boolean relExiste = relacionUMLRepository.findByModeloUMLId(modelo.getId()).stream()
                            .anyMatch(r -> r.getClaseOrigen().getId().equals(oId) &&
                                           r.getClaseDestino().getId().equals(dId) &&
                                           r.getTipoRelacion() == tipo);

                    if (!relExiste) {
                        RelacionUML rel = RelacionUML.builder()
                                .tipoRelacion(tipo)
                                .claseOrigen(origen)
                                .claseDestino(destino)
                                .cardinalidadOrigen(rDto.getCardinalidadOrigen() != null ? rDto.getCardinalidadOrigen() : "1")
                                .cardinalidadDestino(rDto.getCardinalidadDestino() != null ? rDto.getCardinalidadDestino() : "1")
                                .descripcion(rDto.getDescripcion())
                                .modeloUML(modelo)
                                .build();
                        relacionUMLRepository.save(rel);
                        totalRelaciones++;
                    }
                }
            }
        }

        return new int[]{clasesGuardadasPorNombre.size(), totalAtributos, totalMetodos, totalRelaciones};
    }
}
