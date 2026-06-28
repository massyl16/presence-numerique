package com.example.app_gestion_presences.service;

import com.example.app_gestion_presences.AttendanceUpdate;
import com.example.app_gestion_presences.entity.Attendance;
import com.example.app_gestion_presences.entity.Event;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AttendancePushService {

    private final SimpMessagingTemplate messagingTemplate;

    public AttendancePushService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /** Mise à jour statut d'une présence → trombinoscope enseignant */
    public void sendUpdate(Attendance attendance) {
        AttendanceUpdate dto = new AttendanceUpdate(
                attendance.getEvent().getId(),
                attendance.getUser().getId(),
                attendance.getStatus().name()
        );
        messagingTemplate.convertAndSend("/topic/event/" + dto.eventId(), dto);
    }

    /** Démarrage de séance → étudiant recharge sa page */
    public void sendUpdateStarted(Event event) {
        messagingTemplate.convertAndSend("/topic/event/" + event.getId() + "/start", true);
    }

    /** Clôture → le bouton étudiant se désactive */
    public void sendUpdateClosed(Event event) {
        messagingTemplate.convertAndSend("/topic/event/" + event.getId() + "/close", false);
    }

    /** Message d'erreur individuel (trop loin, etc.) */
    public void sendUpdateResponse(Attendance attendance, String text) {
        messagingTemplate.convertAndSend(
                "/topic/event/" + attendance.getEvent().getId() + "/start/" + attendance.getUser().getId(),
                text
        );
    }

    /**
     * Notifie TOUS les étudiants d'une promotion qu'un appel vient de démarrer.
     * Chaque étudiant est abonné à /topic/promo/{promoId}.
     * Le payload contient l'ID de la séance, la promotion et l'enseignant.
     */
    public void sendAppelStart(Event event) {
        Map<String, Object> payload = Map.of(
                "seanceId",    event.getId(),
                "promotionId", event.getPromotion().getId(),
                "promotion",   event.getPromotion().getName(),
                "enseignant",  event.getEnseignant().getFirstname() + " " + event.getEnseignant().getLastname(),
                "startTime",   event.getStartTime() != null ? event.getStartTime().toString() : ""
        );
        messagingTemplate.convertAndSend("/topic/promo/" + event.getPromotion().getId(), (Object) payload);
    }

    /** Notifie les étudiants de la clôture de la séance */
    public void sendAppelClose(Event event) {
        messagingTemplate.convertAndSend("/topic/promo/" + event.getPromotion().getId() + "/close", true);
    }
}
