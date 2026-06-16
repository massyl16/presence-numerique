package com.example.app_gestion_presences.entity;

import jakarta.persistence.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/*Classe user, qui décrit un utilisateur de l'application,
 et permet l'authentification des participants à un évênement.
 Un user possède un id unique, des informations sur lui, un email
 et un rôle (Participant/Speaker/Secretary) ainsi qu'une photo (peut
 être null si Secretary ou Speaker)*/

@Entity
@Table(name = "users")
public class user {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstname;
    private String lastname;
    private String email;
    @Enumerated(EnumType.STRING)
    private Role role;
    private String photo_path;
    @ManyToOne
    private promotion promotion;
    @ManyToOne
    private group group;

    //Getters et setters

    private void setFirstname(String firstname) {
        this.firstname=firstname;
    }

    private void setLastname(String lastname) {
        this.lastname=lastname;
    }

    private void setEmail(String email) {
        this.email=email;
    }

    private void setRole(Role role){
        this.role=role;
    }

    private void setPhoto(String photo_path) {
        this.photo_path=photo_path;
    }

    private void setPromotion(promotion promotion){
        this.promotion=promotion;
    }

    private void setGroup(group group){
        this.group=group;
    }

    public Long getId() {
        return this.id;
    }

    public String getFirstname() {
        return this.firstname;
    }

    public String getLastname() {
        return this.lastname;
    }

    public String getEmail() {
        return this.email;
    }

    public Role getRole(){
        return this.role;
    }

    public String getPhoto() {
        return this.photo_path;
    }

    public promotion getPromotion(){
        return this.promotion;
    }

    public group getGroup(){
        return this.group;
    }

    //temporaire pour le DataInitializer

    @PrePersist
    public void init_photo(){
        String fileName = "user_" + email + ".jpg";
        setPhoto("/uploads/photos/" + fileName);
    }

    //Constructeurs

    public user(String firstname,String lastname, String email, promotion promotion, group group) {
        setFirstname(firstname);
        setLastname(lastname);
        setEmail(email);
        setRole(Role.Participant);
        String fileName = "user_" + email + ".jpg";
        setPhoto("/uploads/photos/" + fileName);
        setPromotion(promotion);
        setGroup(group);
    }

    public user(String firstname,String lastname, String email, Role role) {
        setFirstname(firstname);
        setLastname(lastname);
        setEmail(email);
        setRole(role);
    }

    public user(){}
}

