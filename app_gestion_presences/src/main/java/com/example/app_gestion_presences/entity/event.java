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
    private String date;
    private String place;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime lateTime;
    private Double latitude;
    private Double longitude;
    private Double allowedRadiusMeters;
    private boolean started;
    @ManyToOne
    private user secretary;
    @ManyToOne
    private user speaker;

    //Setters et getters

    private void setTitle(String title) {
        this.title=title;
    }

    private void setDate(String date) {
        this.date=date;
    }

    private void setPlace(String place){
        this.place=place;
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

    private void setEndTime(LocalDateTime time){
        this.endTime=time;
    }

    private void setLateTime(LocalDateTime time){
        this.lateTime=time;
    }

    public void setStarted(boolean b){
        this.started=b;
    }

    private void setSpeaker(user speaker) {
        this.speaker=speaker;
    }

    private void setSecretary(user secretary) {
        this.secretary=secretary;
    }

    private void setAllowedRadiusMeters(Double radius){
        this.allowedRadiusMeters=radius;
    }

    public Long getId(){
        return this.id;
    }

    public String getTitle(){
        return this.title;
    }

    public String getDate(){
        return this.date;
    }

    public String getPlace(){
        return this.place;
    }
    public boolean getStarted() {
        return this.started;
    }

    public LocalDateTime getStartTime() {
        return this.startTime;
    }

    public LocalDateTime getEndTime() {
        return this.endTime;
    }

    public LocalDateTime getLateTime() {
        return this.lateTime;
    }

    public Double getLatitude(){
        return this.latitude;
    }

    public Double getLongitude(){
        return this.longitude;
    }

    public Double getAllowedRadiusMeters(){
        return this.allowedRadiusMeters;
    }

    //Constructeurs

    public event(String title, String date, String place, Double latitude, Double longitude, Double allowedRadiusMeters, LocalDateTime startTime, LocalDateTime endTime, LocalDateTime lateTime , user[] participant_list,user speaker, user secretary) {
        setTitle(title);
        setDate(date);
        setPlace(place);
        setLatitude(latitude);
        setLongitude(longitude);
        setLateTime(lateTime);
        setStartTime(startTime);
        setEndTime(endTime);
        setAllowedRadiusMeters(allowedRadiusMeters);
        setSpeaker(speaker);
        setSecretary(secretary);
        this.started=false;
    }

    public event(){}

}
