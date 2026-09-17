package com.caseplatform.ai.repository;

import com.caseplatform.ai.model.AICommandHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AICommandHistoryRepository extends JpaRepository<AICommandHistory, Long> {

    List<AICommandHistory> findByModeloUMLIdOrderByFechaDesc(Long modeloId);

    long countByModeloUMLId(Long modeloId);
}
