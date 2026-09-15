package com.caseplatform.service;

import com.caseplatform.dto.uml.RelacionUMLDTO;

import java.util.List;

public interface RelacionUMLService {

    RelacionUMLDTO crearRelacion(RelacionUMLDTO dto);

    List<RelacionUMLDTO> listarPorModelo(Long modeloId);

    RelacionUMLDTO obtenerPorId(Long id);

    void eliminar(Long id);
}
