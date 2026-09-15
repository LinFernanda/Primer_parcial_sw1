package com.caseplatform.service;

import com.caseplatform.dto.uml.ModeloUMLDTO;

import java.util.List;

public interface ModeloUMLService {

    ModeloUMLDTO crearModelo(Long proyectoId, ModeloUMLDTO dto, String usuarioEmail);

    List<ModeloUMLDTO> listarPorProyecto(Long proyectoId);

    ModeloUMLDTO obtenerPorId(Long id);

    void eliminar(Long id, String usuarioEmail);
}
