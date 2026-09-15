package com.caseplatform.repository;

import com.caseplatform.model.ProyectoUML;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProyectoUMLRepository extends JpaRepository<ProyectoUML, Long> {

    List<ProyectoUML> findByUsuarioPropietarioId(Long usuarioId);

    List<ProyectoUML> findByUsuarioPropietarioEmailIgnoreCase(String email);

    boolean existsByNombreIgnoreCaseAndUsuarioPropietarioId(String nombre, Long usuarioId);
}
