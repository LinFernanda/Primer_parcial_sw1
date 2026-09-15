package com.caseplatform.service;

import com.caseplatform.dto.uml.ClaseUMLDTO;

import java.util.List;

public interface ClaseUMLService {

    ClaseUMLDTO crearClase(Long modeloId, ClaseUMLDTO dto);

    List<ClaseUMLDTO> listarPorModelo(Long modeloId);

    ClaseUMLDTO obtenerPorId(Long id);

    ClaseUMLDTO actualizar(Long id, ClaseUMLDTO dto);

    void eliminar(Long id);
}
