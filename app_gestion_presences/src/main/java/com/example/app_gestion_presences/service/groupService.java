package com.example.app_gestion_presences.service;

import com.example.app_gestion_presences.entity.Group;
import com.example.app_gestion_presences.entity.Promotion;
import com.example.app_gestion_presences.repository.GroupRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class groupService {

    private final GroupRepository groupRepository;

    public groupService(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    public void createGroup(String name, Promotion promotion) {
        groupRepository.save(new Group(name, promotion));
    }

    public List<Group> getAllGroups(Promotion promotion) {
        return groupRepository.findAllByPromotion(promotion);
    }
}
