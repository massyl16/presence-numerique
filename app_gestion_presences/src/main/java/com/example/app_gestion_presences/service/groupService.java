package com.example.app_gestion_presences.service;

import com.example.app_gestion_presences.entity.*;

import com.example.app_gestion_presences.repository.groupRepository;
import com.example.app_gestion_presences.repository.promotionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class groupService {
    private final groupRepository groupRepository;

    public groupService(groupRepository groupRepository){
        this.groupRepository=groupRepository;
    }

    public void createGroup(String name, promotion promotion){
        group group = new group(name, promotion);
        groupRepository.save(group);
    }

    public List<group> getAllGroups(promotion promotion){
        return groupRepository.findAllByPromotion(promotion);
    }
}
