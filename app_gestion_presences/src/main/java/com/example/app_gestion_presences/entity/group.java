package com.example.app_gestion_presences.entity;
import jakarta.persistence.*;
@Entity

@Table(name = "groups")
public class group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    @ManyToOne
    private promotion promotion;

    private void setName(String name){
        this.name=name;
    }

    private void setPromotion(promotion promotion){
        this.promotion=promotion;
    }

    public Long getId(){
        return this.id;
    }

    public String getName(){
        return this.name;
    }

    public promotion getPromotion(){
        return this.promotion;
    }

    public group(String name, promotion promotion){
        setName(name);
        setPromotion(promotion);
    }

    public group(){}
}
