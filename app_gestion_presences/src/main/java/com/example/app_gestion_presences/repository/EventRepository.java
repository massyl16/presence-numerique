package com.example.app_gestion_presences.repository;

import com.example.app_gestion_presences.entity.Event;
import com.example.app_gestion_presences.entity.Promotion;
import com.example.app_gestion_presences.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findAllByEnseignant(User enseignant);
    List<Event> findAllByPromotion(Promotion promotion);

    /** Séance active pour une promotion (commencée, non clôturée) */
    @Query("SELECT e FROM Event e WHERE e.promotion = :promo AND e.started = true AND e.closed = false")
    Optional<Event> findActiveByPromotion(@Param("promo") Promotion promo);

    /** Séances passées d'un enseignant (clôturées) */
    List<Event> findAllByEnseignantAndClosedOrderByStartTimeDesc(User enseignant, boolean closed);

    /** Séances passées d'une promotion (clôturées) */
    List<Event> findAllByPromotionAndClosedOrderByStartTimeDesc(Promotion promotion, boolean closed);

    /** Toutes les séances clôturées (admin — toutes promotions) */
    List<Event> findAllByClosedOrderByStartTimeDesc(boolean closed);
}
