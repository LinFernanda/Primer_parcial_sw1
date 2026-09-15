package com.caseplatform.service;

import com.caseplatform.dto.uml.ProyectoUMLDTO;

import java.util.List;

public interface ProyectoUMLService {

    ProyectoUMLDTO crearProyecto(ProyectoUMLDTO dto, String usuarioEmail);

    List<ProyectoUMLDTO> listarProyectos(String usuarioEmail);

    ProyectoUMLDTO obtenerPorId(Long id, String usuarioEmail);

    ProyectoUMLDTO actualizar(Long id, ProyectoUMLDTO dto, String usuarioEmail);

    void eliminar(Long id, String usuarioEmail);
}
