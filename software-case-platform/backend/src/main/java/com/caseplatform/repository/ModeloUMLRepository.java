package com.caseplatform.repository;

import com.caseplatform.model.ModeloUML;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModeloUMLRepository extends JpaRepository<ModeloUML, Long> {

    List<ModeloUML> findByProyectoId(Long proyectoId);

    Optional<ModeloUML> findByIdAndProyectoId(Long id, Long proyectoId);
}
