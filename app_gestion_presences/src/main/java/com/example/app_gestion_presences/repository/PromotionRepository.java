package com.example.app_gestion_presences.repository;

import com.example.app_gestion_presences.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    Promotion findByName(String name);
}
