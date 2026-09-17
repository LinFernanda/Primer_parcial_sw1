package com.caseplatform.versioning.repository;

import com.caseplatform.versioning.model.VersionModelo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VersionModeloRepository extends JpaRepository<VersionModelo, Long> {

    List<VersionModelo> findByModeloUMLIdOrderByFechaCreacionDesc(Long modeloId);

    Optional<VersionModelo> findByIdAndModeloUMLId(Long id, Long modeloId);

    long countByModeloUMLId(Long modeloId);
}
