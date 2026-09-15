package com.caseplatform.repository;

import com.caseplatform.model.AtributoUML;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AtributoUMLRepository extends JpaRepository<AtributoUML, Long> {

    List<AtributoUML> findByClaseUMLId(Long claseId);

    boolean existsByClaseUMLIdAndNombreIgnoreCase(Long claseId, String nombre);
}
