package com.example.app_gestion_presences.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Event event;

    private LocalDateTime validationTime;
    private int lateMinutes;

    @Enumerated(EnumType.STRING)
    private AttendanceStatus status;

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Event getEvent() { return event; }
    public AttendanceStatus getStatus() { return status; }
    public LocalDateTime getValidationTime() { return validationTime; }
    public int getLateMinutes() { return lateMinutes; }

    public void setUser(User user) { this.user = user; }
    public void setEvent(Event event) { this.event = event; }
    public void setValidationTime(LocalDateTime validationTime) { this.validationTime = validationTime; }
    public void setStatus(AttendanceStatus status) { this.status = status; }
    public void setLateMinutes(int lateMinutes) { this.lateMinutes = lateMinutes; }

    public Attendance(User user, Event event) {
        this.user = user;
        this.event = event;
        this.status = AttendanceStatus.Absent;
    }

    public Attendance() {}

    public String verification(Double latitude, Double longitude) {
        if (!event.getStarted() || event.getToken() == null) {
            return "L'appel n'est pas actif.";
        }

        setValidationTime(LocalDateTime.now());

        // Si l'enseignant a démarré sans GPS (lat=0, lon=0), on saute la vérification
        boolean gpsDisabled = event.getLatitude() == null
                || event.getLongitude() == null
                || (event.getLatitude() == 0.0 && event.getLongitude() == 0.0);

        if (!gpsDisabled) {
            // Formule de Haversine — distance réelle en mètres
            double R = 6371000;
            double dLat = Math.toRadians(latitude - event.getLatitude());
            double dLon = Math.toRadians(longitude - event.getLongitude());
            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                    + Math.cos(Math.toRadians(event.getLatitude()))
                    * Math.cos(Math.toRadians(latitude))
                    * Math.sin(dLon / 2) * Math.sin(dLon / 2);
            double distance = R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

            if (distance > 150) {
                return "Vous êtes trop loin de l'emplacement de l'évênement";
            }
        }

        int threshold = event.getLateThreshold();
        if (validationTime.isBefore(event.getStartTime())) {
            return "L'évênement a été démarré mais vous devez patienter jusqu'à l'heure de début réel";
        } else if (validationTime.isBefore(event.getStartTime().plusMinutes(threshold))) {
            setStatus(AttendanceStatus.Present);
            setLateMinutes(0);
        } else {
            setStatus(AttendanceStatus.Late);
            long minutes = java.time.Duration.between(event.getStartTime(), validationTime).toMinutes();
            setLateMinutes((int) minutes);
        }
        return "Ok";
    }
}
