package com.example.app_gestion_presences.service;

import com.example.app_gestion_presences.entity.Promotion;
import com.example.app_gestion_presences.repository.PromotionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class promotionService {

    private final PromotionRepository promotionRepository;

    public promotionService(PromotionRepository promotionRepository) {
        this.promotionRepository = promotionRepository;
    }

    public void createPromotion(String name) {
        promotionRepository.save(new Promotion(name));
    }

    public List<Promotion> getAllPromotions() {
        return promotionRepository.findAll();
    }
}
