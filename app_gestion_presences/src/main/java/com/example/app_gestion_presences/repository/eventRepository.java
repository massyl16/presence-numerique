package com.example.app_gestion_presences.repository;

import com.example.app_gestion_presences.entity.event;
import com.example.app_gestion_presences.entity.user;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/*Interface utilisant JPARepository pour la base de données contenant
 les évênements.*/

public interface eventRepository extends JpaRepository<event, Long> {
    //Trouve l'évênement à partir de son titre
    event findByTitle(String title);

    //Trouve tout les évênements associés à un secrétaire
    List<event> findAllBySecretary(user secretary);
    //Trouve tout les évênements associés à un intervenant
    List<event> findAllBySpeaker(user speaker);
}
