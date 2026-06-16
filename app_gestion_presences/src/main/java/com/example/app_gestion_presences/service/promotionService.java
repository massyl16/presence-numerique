package com.example.app_gestion_presences.service;

import com.example.app_gestion_presences.entity.*;

import com.example.app_gestion_presences.repository.promotionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class promotionService {

    private final promotionRepository promotionRepository;

    public promotionService(promotionRepository promotionRepository){
        this.promotionRepository=promotionRepository;
    }

    public void createPromotion(String name){
        promotion promotion = new promotion(name);
        promotionRepository.save(promotion);
    }

    public List<promotion> getAllPromotions(){
        return promotionRepository.findAll();
    }
}
