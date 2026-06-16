package com.example.app_gestion_presences;

import com.example.app_gestion_presences.entity.*;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import com.example.app_gestion_presences.repository.userRepository;
import com.example.app_gestion_presences.repository.eventRepository;
import com.example.app_gestion_presences.repository.attendanceRepository;
import com.example.app_gestion_presences.repository.promotionRepository;
import com.example.app_gestion_presences.repository.groupRepository;

import java.time.LocalDateTime;

@Component
public class DataInitializer {

    private final userRepository userRepository;

    private final eventRepository eventRepository;

    private final attendanceRepository attendanceRepository;

    private final promotionRepository promotionRepository;

    private final groupRepository groupRepository;

    public DataInitializer(userRepository userRepository, eventRepository eventRepository, attendanceRepository attendanceRepository, promotionRepository promotionRepository, groupRepository groupRepository) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.attendanceRepository = attendanceRepository;
        this.promotionRepository=promotionRepository;
        this.groupRepository=groupRepository;
    }

    @PostConstruct
    public void init() {

        if (promotionRepository.count() == 0) {

            promotion promotion1 = new promotion("M1Test");

            promotion promotion2 = new promotion("M2Test");

            promotionRepository.save(promotion1);
            promotionRepository.save(promotion2);

            group group1_all = new group("Complet",promotion1);

            group group2_all = new group("Complet",promotion2);

            group group1_1 = new group("Groupe 1",promotion1);

            group group1_2 = new group("Groupe 2",promotion1);

            group group2_1 = new group("Groupe 1",promotion2);

            group group2_2 = new group("Groupe 2",promotion2);

            groupRepository.save(group1_all);
            groupRepository.save(group2_all);
            groupRepository.save(group1_1);
            groupRepository.save(group1_2);
            groupRepository.save(group2_1);
            groupRepository.save(group2_2);

            user part1 = new user("e","f","ef@test.com",promotion1,group1_1);

            user part2 = new user("g","h","gh@test.com",promotion1,group1_2);

            user part3 = new user("i","j","ij@test.com",promotion2,group2_1);

            user part4 = new user("k","l","kl@test.com",promotion2,group2_2);

            userRepository.save(part1);
            userRepository.save(part2);
            userRepository.save(part3);
            userRepository.save(part4);

            user secretary = new user("a", "b", "ab@test.com", Role.Secretary);

            userRepository.save(secretary);

            user teacher = new user("c", "d", "cd@test.com", Role.Speaker);

            userRepository.save(teacher);


        }
    }
}