package com.example.app_gestion_presences.repository;

import com.example.app_gestion_presences.entity.group;
import com.example.app_gestion_presences.entity.promotion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/*Interface utilisant JPARepository pour la base de données contenant
 les groupes d'une promotion.*/
public interface groupRepository extends JpaRepository<group,Long>{
    //Trouve tous les groupes d'une promotion
    List<group> findAllByPromotion(promotion promotion);
}
