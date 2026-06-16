package com.example.app_gestion_presences.repository;

import com.example.app_gestion_presences.entity.promotion;
import org.springframework.data.jpa.repository.JpaRepository;

/*Interface utilisant JPARepository pour la base de données contenant
 les promotions.*/

public interface promotionRepository extends JpaRepository<promotion,Long> {
    promotion findByName(String name);
}
