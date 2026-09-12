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

    @ManyToOne
    @JoinColumn(name = "responsable_id")
    private User responsableFormation;

    public Long getId() { return id; }
    public String getName() { return name; }
    public User getSecretaireResponsable() { return secretaireResponsable; }
    public User getResponsableFormation() { return responsableFormation; }

    public void setName(String name) { this.name = name; }
    public void setSecretaireResponsable(User secretaireResponsable) { this.secretaireResponsable = secretaireResponsable; }
    public void setResponsableFormation(User responsableFormation) { this.responsableFormation = responsableFormation; }

    public Promotion(String name) { this.name = name; }
    public Promotion() {}
}
