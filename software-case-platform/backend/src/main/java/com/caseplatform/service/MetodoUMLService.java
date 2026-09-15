package com.caseplatform.service;

import com.caseplatform.dto.uml.MetodoUMLDTO;

import java.util.List;

public interface MetodoUMLService {

    MetodoUMLDTO agregarMetodo(Long claseId, MetodoUMLDTO dto);

    List<MetodoUMLDTO> listarPorClase(Long claseId);

    void eliminar(Long id);
}
