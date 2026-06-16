package com.example.app_gestion_presences.service;

import com.example.app_gestion_presences.entity.*;

import com.example.app_gestion_presences.repository.eventRepository;
import com.example.app_gestion_presences.repository.groupRepository;
import com.example.app_gestion_presences.repository.promotionRepository;
import com.example.app_gestion_presences.repository.userRepository;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class eventService {
    private final eventRepository event_repository;

    private final attendanceService attendance_service;

    private final userRepository userRepository;

    private final promotionRepository promotionRepository;

    private final groupRepository groupRepository;


    public eventService(eventRepository event_repository, attendanceService attendance_service, userRepository userRepository, promotionRepository promotionRepository, groupRepository groupRepository) {
        this.event_repository = event_repository;
        this.attendance_service = attendance_service;
        this.userRepository=userRepository;
        this.promotionRepository=promotionRepository;
        this.groupRepository=groupRepository;
    }

    public void createEvent(String title, LocalDateTime startTime, Double latitude, Double longitude, user speaker, promotion promotion, group group){

        event event = new event(title,latitude,longitude,startTime,speaker,promotion,group);

        event_repository.save(event);
        //creer attendances

        if (Objects.equals(group.getName(), "Complet")){
            List<group> groups = groupRepository.findAllByPromotion(promotion);
            for (int i = 0; i < groups.size(); i++) {
                List<user> participant_list = userRepository.findAllByPromotionAndGroup(promotion,groups.get(i));

                for (int j = 0; j < participant_list.size(); j++) {
                    attendance_service.createAttendance(participant_list.get(j),event);
                }
            }
        }else{
            List<user> participant_list = userRepository.findAllByPromotionAndGroup(promotion,group);

            for (int i = 0; i < participant_list.size(); i++) {
                attendance_service.createAttendance(participant_list.get(i),event);
            }
        }
    }

    //getAllEvents pour les intervenants
    public event[] getAllEvents(user user) {
        List<event> attendances;
        // A changer
        if(user.getRole()==Role.Secretary){
            //attendances=event_repository.findAllBySecretary(user);
            attendances=event_repository.findAllBySpeaker(user);
        }else{
            attendances=event_repository.findAllBySpeaker(user);
        }
        event[] events = new event[attendances.size()];
        for (int i = 0; i < events.length; i++) {
            events[i]=(attendances.get(i));
        }
        return events;
    }
}
