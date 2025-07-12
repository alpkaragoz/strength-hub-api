package com.strengthhub.strength_hub_api.repository;

import com.strengthhub.strength_hub_api.model.Lifter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LifterRepository extends JpaRepository<Lifter, UUID> {

    List<Lifter> findByCoachIsNull();

    List<Lifter> findByCoach_CoachId(UUID coachId);

    @Query("SELECT l FROM Lifter l WHERE " +
            "LOWER(l.app_user.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(l.app_user.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(l.app_user.username) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Lifter> findByNameContaining(@Param("search") String search);

    @Query("SELECT COUNT(l) FROM Lifter l WHERE l.coach.coachId = :coachId")
    Long countByCoachId(@Param("coachId") UUID coachId);
}