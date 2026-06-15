package com.example.app_gestion_presences.entity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/*Class attendance, qui décrit l'attente d'une présence de la part d'un
 participant (user) à un évênement (event). On a ainsi comme champs l'heure
 de validation ainsi que les coordonnées GPS récupérées par le navigateur qui
 servent à valider la présence (ou retard selon l'heure) de ce participant. Le statut
 est alors mis à jour dans le champ status, de type énuméré AttendanceStatus
 (Present/Absent/Late).*/

@Entity
public class attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private user user;
    @ManyToOne
    private event event;
    private LocalDateTime validationTime;
    private Double latitude;
    private Double longitude;
    @Enumerated(EnumType.STRING)
    private AttendanceStatus status;

    //Setters et getters

    private void setParticipant(user participant){
        this.user=participant;
    }

    private void setEvent(event event){
        this.event=event;
    }
    private void setValidationTime(LocalDateTime validationTime){
        this.validationTime=validationTime;
    }

    private void setLatitudeLongitude(Double latitude, Double longitude){
        this.latitude=latitude;
        this.longitude=longitude;
    }

    private void setStatus(AttendanceStatus status){
        this.status=status;
    }

    public Long getId(){
        return this.id;
    }

    public user getUser(){
        return this.user;
    }

    public event getEvent(){
        return this.event;
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    //Constructeurs

    public attendance(user participant, event event){
        setParticipant(participant);
        setEvent(event);
        setStatus(AttendanceStatus.Absent);
    }

    public attendance(){}

    /*Méthode verification qui, après avoir démarré sa vérification
    sur l'application, vérifie si les conditions de validation de la présence
    sont bien remplies, à savoir que l'évênement est démarré par l'intervenant,
    que le participant est suffisamment proche des coordonnées GPS du lieu de
    l'évênement, et que l'heure de fin de l'évênement n'est pas dépassée (vérifie
    égalemment si il y a retard ou non). De plus, la méthode renvoie un string qui
    s'affiche sur l'application pour informer de la cause éventuelle de l'échec de
    la vérification.*/

    public String verification(Double latitude, Double longitude){
        if(event.getStarted()){
            setValidationTime(LocalDateTime.now());
            setLatitudeLongitude(latitude,longitude);

            if (Math.sqrt(Math.pow(event.getLatitude()-latitude,2)+Math.pow(event.getLongitude()-longitude,2))<=event.getAllowedRadiusMeters()){
                if(validationTime.isAfter(event.getStartTime()) & validationTime.isBefore(event.getLateTime())){
                    setStatus(AttendanceStatus.Present);
                } else if (validationTime.isAfter(event.getStartTime()) & validationTime.isAfter(event.getLateTime())) {
                    setStatus(AttendanceStatus.Late);
                } else if (validationTime.isAfter(event.getEndTime())) {
                    setStatus(AttendanceStatus.Absent);
                }
                else {
                    return "L'évênement a été démarré par l'intervenant mais vous devez patienter jusqu'à l'heure de début réel de l'évênement";
                }
                return "Ok";
            }
            else{
                return "Vous êtes trop loin de l'emplacement de l'évênement";
            }
        }
        else {
            return "L'évênement n'a pas démarré";
        }
    }

}
