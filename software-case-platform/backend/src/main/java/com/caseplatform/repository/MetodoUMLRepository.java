package com.caseplatform.repository;

import com.caseplatform.model.MetodoUML;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MetodoUMLRepository extends JpaRepository<MetodoUML, Long> {

    List<MetodoUML> findByClaseUMLId(Long claseId);
}
