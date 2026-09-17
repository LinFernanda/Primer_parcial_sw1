package com.caseplatform.versioning.repository;

import com.caseplatform.versioning.model.HistorialCambio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistorialCambioRepository extends JpaRepository<HistorialCambio, Long> {

    List<HistorialCambio> findByModeloUMLIdOrderByFechaCambioDesc(Long modeloId);

    List<HistorialCambio> findByVersionModeloIdOrderByFechaCambioDesc(Long versionModeloId);
}
