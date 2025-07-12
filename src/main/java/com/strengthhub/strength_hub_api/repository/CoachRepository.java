package com.strengthhub.strength_hub_api.repository;

import com.strengthhub.strength_hub_api.model.Coach;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CoachRepository extends JpaRepository<Coach, UUID> {

    @Query("SELECT c FROM Coach c WHERE " +
            "LOWER(c.app_user.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(c.app_user.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(c.app_user.username) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Coach> findByNameContaining(@Param("search") String search);

    @Query("SELECT c FROM Coach c ORDER BY SIZE(c.lifters) DESC")
    List<Coach> findAllOrderByLifterCountDesc();

    @Query("SELECT c FROM Coach c WHERE SIZE(c.lifters) = 0")
    List<Coach> findCoachesWithoutLifters();
}
