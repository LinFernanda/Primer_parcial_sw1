package com.caseplatform.repository;

import com.caseplatform.model.ClaseUML;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClaseUMLRepository extends JpaRepository<ClaseUML, Long> {

    List<ClaseUML> findByModeloUMLId(Long modeloId);

    boolean existsByModeloUMLIdAndNombreIgnoreCase(Long modeloId, String nombre);

    Optional<ClaseUML> findByModeloUMLIdAndNombreIgnoreCase(Long modeloId, String nombre);
}
