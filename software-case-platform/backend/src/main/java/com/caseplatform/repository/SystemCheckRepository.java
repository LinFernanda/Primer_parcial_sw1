package com.caseplatform.repository;

import com.caseplatform.model.SystemCheck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SystemCheckRepository extends JpaRepository<SystemCheck, UUID> {
    Optional<SystemCheck> findByCheckName(String checkName);
}
