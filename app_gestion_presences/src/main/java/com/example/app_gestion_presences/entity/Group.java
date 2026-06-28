package com.example.app_gestion_presences.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "groups")
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    private Promotion promotion;

    public Long getId() { return id; }
    public String getName() { return name; }
    public Promotion getPromotion() { return promotion; }
    public void setName(String name) { this.name = name; }
    public void setPromotion(Promotion promotion) { this.promotion = promotion; }

    public Group(String name, Promotion promotion) {
        this.name = name;
        this.promotion = promotion;
    }

    public Group() {}
}
