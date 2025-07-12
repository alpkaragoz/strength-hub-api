package com.strengthhub.strength_hub_api.repository;

import com.strengthhub.strength_hub_api.model.CoachCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CoachCodeRepository extends JpaRepository<CoachCode, UUID> {

    Optional<CoachCode> findByCode(String code);
    boolean existsByCode(String code);
}
