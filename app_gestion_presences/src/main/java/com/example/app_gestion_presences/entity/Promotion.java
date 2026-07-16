package com.example.app_gestion_presences.entity;

import jakarta.persistence.*;

@Entity
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "secretaire_id")
    private User secretaireResponsable;

    public Long getId() { return id; }
    public String getName() { return name; }
    public User getSecretaireResponsable() { return secretaireResponsable; }

    public void setName(String name) { this.name = name; }
    public void setSecretaireResponsable(User secretaireResponsable) { this.secretaireResponsable = secretaireResponsable; }

    public Promotion(String name) { this.name = name; }
    public Promotion() {}
}
