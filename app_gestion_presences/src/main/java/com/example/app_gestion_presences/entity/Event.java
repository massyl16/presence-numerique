package com.example.app_gestion_presences.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    /** Date/heure prévue */
    private LocalDateTime scheduledTime;

    /** Date/heure réelle de démarrage par l'enseignant */
    private LocalDateTime startTime;

    private Double latitude;
    private Double longitude;
    private boolean started;
    private boolean closed;
    private String token;
    private int lateThreshold = 5;

    @ManyToOne
    @JoinColumn(name = "speaker_id")
    private User enseignant;

    @ManyToOne
    private Promotion promotion;

    @ManyToOne
    private Group group;

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public LocalDateTime getScheduledTime() { return scheduledTime; }
    public LocalDateTime getStartTime() { return startTime; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public boolean getStarted() { return started; }
    public boolean isClosed() { return closed; }
    public String getToken() { return token; }
    public int getLateThreshold() { return lateThreshold; }
    public User getEnseignant() { return enseignant; }
    public Promotion getPromotion() { return promotion; }
    public Group getGroup() { return group; }

    public void setTitle(String title) { this.title = title; }
    public void setScheduledTime(LocalDateTime scheduledTime) { this.scheduledTime = scheduledTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public void setStarted(boolean started) { this.started = started; }
    public void setClosed(boolean closed) { this.closed = closed; }
    public void setToken(String token) { this.token = token; }
    public void setLateThreshold(int lateThreshold) { this.lateThreshold = lateThreshold; }
    public void setEnseignant(User enseignant) { this.enseignant = enseignant; }
    public void setPromotion(Promotion promotion) { this.promotion = promotion; }
    public void setGroup(Group group) { this.group = group; }

    public Event(LocalDateTime scheduledTime, int lateThreshold, Double latitude, Double longitude,
                 User enseignant, Promotion promotion, Group group) {
        this.scheduledTime = scheduledTime;
        this.lateThreshold = lateThreshold;
        this.latitude = latitude;
        this.longitude = longitude;
        this.enseignant = enseignant;
        this.promotion = promotion;
        this.group = group;
        this.started = false;
        this.closed = false;
    }

    public Event() {}

    public void start(User user) {
        if (user.getRole() == Role.ENSEIGNANT && !this.started && !this.closed) {
            this.started = true;
            this.startTime = LocalDateTime.now();
            this.token = UUID.randomUUID().toString();
        }
    }

    public void close(User user) {
        if (user.getRole() == Role.ENSEIGNANT) {
            this.started = false;
            this.closed = true;
            this.token = null;
        }
    }
}
