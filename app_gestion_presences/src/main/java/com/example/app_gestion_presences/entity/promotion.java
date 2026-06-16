package com.example.app_gestion_presences.entity;

import jakarta.persistence.*;
@Entity
public class promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private void setName(String name){
        this.name=name;
    }

    public Long getId(){
        return this.id;
    }

    public String getName(){
        return this.name;
    }

    public promotion(String name){
        setName(name);
    }

    public promotion(){}
}
