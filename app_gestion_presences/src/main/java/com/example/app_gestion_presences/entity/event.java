package com.example.app_gestion_presences.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/*Class event, qui décrit un évênement, avec des informations
 sur l'évênement, des LocalDateTime indiquant les dates et heures
 de début, fin ou de retard, ainsi que les coordonnées GPS pour vérifier
 la présence des participant à l'évênement. Les champs speaker et secretary
 servent à savoir l'intervenant et le secrétaire associés (utilisés
 par eventRepository (findAllBySpeaker et findAllBySecretary) pour retrouver
 les events liés à ce user).*/

@Entity
public class event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private LocalDateTime startTime;
    private Double latitude;
    private Double longitude;
    private boolean started;
    @ManyToOne
    private user speaker;

    @ManyToOne
    private promotion promotion;

    @ManyToOne
    private group group;

    //Setters et getters

    private void setTitle(String title) {
        this.title=title;
    }

    private void setLatitude(Double latitude){
        this.latitude=latitude;
    }

    private void setLongitude(Double longitude){
        this.longitude=longitude;
    }

    private void setStartTime(LocalDateTime time){
        this.startTime=time;
    }

    private void setStarted(boolean b){
        this.started=b;
    }

    private void setSpeaker(user speaker) {
        this.speaker=speaker;
    }

    private void setPromotion(promotion promotion){
        this.promotion=promotion;
    }

    private void setGroup(group group){
        this.group=group;
    }

    public Long getId(){
        return this.id;
    }

    public String getTitle(){
        return this.title;
    }

    public boolean getStarted() {
        return this.started;
    }

    public LocalDateTime getStartTime() {
        return this.startTime;
    }

    public Double getLatitude(){
        return this.latitude;
    }

    public Double getLongitude(){
        return this.longitude;
    }

    //Constructeurs

    public event(String title, Double latitude, Double longitude, LocalDateTime startTime,user speaker, promotion promotion, group group) {
        setTitle(title);
        setLatitude(latitude);
        setLongitude(longitude);
        setStartTime(startTime);
        setSpeaker(speaker);
        setPromotion(promotion);
        setGroup(group);
        setStarted(false);
    }

    public event(){}

    //Méthode pour démarrer l'évênement par un intervenant
    public void start(user user){
        if(user.getRole()== Role.Speaker){
            setStarted(true);
        }
    }

    public void close(user user) {
        if(user.getRole()== Role.Speaker){
            setStarted(false);
        }
    }
}
