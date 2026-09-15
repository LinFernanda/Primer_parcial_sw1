package com.caseplatform.repository;

import com.caseplatform.model.RelacionUML;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RelacionUMLRepository extends JpaRepository<RelacionUML, Long> {

    List<RelacionUML> findByModeloUMLId(Long modeloId);

    @Query("SELECT r FROM RelacionUML r WHERE r.claseOrigen.id = :claseId OR r.claseDestino.id = :claseId")
    List<RelacionUML> findByClaseInvolucrada(@Param("claseId") Long claseId);

    List<RelacionUML> findByClaseOrigenIdOrClaseDestinoId(Long claseOrigenId, Long claseDestinoId);
}
