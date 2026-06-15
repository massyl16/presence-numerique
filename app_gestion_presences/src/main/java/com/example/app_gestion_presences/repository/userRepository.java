package com.example.app_gestion_presences.repository;

import com.example.app_gestion_presences.entity.Role;
import com.example.app_gestion_presences.entity.user;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/*Interface utilisant JPARepository pour la base de données contenant
 les utilisateurs de l'application.*/

public interface userRepository extends JpaRepository<user, Long> {
    //Trouve un utilisateur via son mail et son rôle (un même mail peut être utilisé pour plusieurs rôles).
    user findByEmailAndRole(String email, Role Role);

    //Trouve tout les utilisateurs ayant un certain rôle
    List<user> findAllByRole(Role role);
}
