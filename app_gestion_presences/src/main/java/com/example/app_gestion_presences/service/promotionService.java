package com.example.app_gestion_presences.service;

import com.example.app_gestion_presences.entity.Promotion;
import com.example.app_gestion_presences.entity.User;
import com.example.app_gestion_presences.repository.PromotionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class promotionService {

    private final PromotionRepository promotionRepository;

    public promotionService(PromotionRepository promotionRepository) {
        this.promotionRepository = promotionRepository;
    }

    public void createPromotion(String name, User secretaire) {
        Promotion promotion = new Promotion(name);
        promotion.setSecretaireResponsable(secretaire);
        promotionRepository.save(promotion);
    }

    public List<Promotion> getAllPromotions() {
        return promotionRepository.findAll();
    }
}
