package com.example.app_gestion_presences.repository;

import com.example.app_gestion_presences.entity.user;
import com.example.app_gestion_presences.entity.event;
import com.example.app_gestion_presences.entity.attendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/*Interface utilisant JPARepository pour la base de données contenant
 les attendances.*/

public interface attendanceRepository extends JpaRepository<attendance, Long>{
    //Trouve toutes les attendances associées à un participant
    List<attendance> findAllByUser(user user);
    //Trouve toutes les attendances associées à un évênement
    List<attendance> findAllByEvent(event event);
    //Trouve toutes les attendances associées à un évênement (par son Id)

    List<attendance> findAllByEventId(Long event_id);
}
