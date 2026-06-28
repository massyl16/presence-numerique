package com.example.app_gestion_presences.repository;

import com.example.app_gestion_presences.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmailAndRole(String email, Role role);
    List<User> findAllByRole(Role role);
    List<User> findAllByPromotion(Promotion promotion);
    List<User> findAllByPromotionAndGroup(Promotion promotion, Group group);
    List<User> findByEmailAndPromotion(String email, Promotion promotion);
    Optional<User> findByEmail(String email);
}
