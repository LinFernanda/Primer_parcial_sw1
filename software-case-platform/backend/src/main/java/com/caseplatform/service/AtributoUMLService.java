package com.caseplatform.service;

import com.caseplatform.dto.uml.AtributoUMLDTO;

import java.util.List;

public interface AtributoUMLService {

    AtributoUMLDTO agregarAtributo(Long claseId, AtributoUMLDTO dto);

    List<AtributoUMLDTO> listarPorClase(Long claseId);

    void eliminar(Long id);
}
