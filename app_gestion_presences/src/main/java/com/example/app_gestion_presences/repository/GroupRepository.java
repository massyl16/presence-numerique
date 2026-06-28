package com.example.app_gestion_presences.repository;

import com.example.app_gestion_presences.entity.Group;
import com.example.app_gestion_presences.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupRepository extends JpaRepository<Group, Long> {
    List<Group> findAllByPromotion(Promotion promotion);
}
