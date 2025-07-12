package com.strengthhub.strength_hub_api.repository;

import com.strengthhub.strength_hub_api.model.Lifter;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface LifterRepository extends JpaRepository<Lifter, UUID> {

}