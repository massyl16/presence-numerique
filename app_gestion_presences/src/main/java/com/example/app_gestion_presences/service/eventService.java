package com.example.app_gestion_presences.service;

import com.example.app_gestion_presences.entity.*;
import com.example.app_gestion_presences.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class eventService {

    private final EventRepository eventRepository;
    private final attendanceService attendanceService;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;

    public eventService(EventRepository eventRepository, attendanceService attendanceService,
                        UserRepository userRepository, GroupRepository groupRepository) {
        this.eventRepository = eventRepository;
        this.attendanceService = attendanceService;
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
    }

    public void createEvent(LocalDateTime scheduledTime, int lateThreshold,
                            Double latitude, Double longitude,
                            User enseignant, Promotion promotion, Group group) {
        Event event = new Event(scheduledTime, lateThreshold, latitude, longitude, enseignant, promotion, group);
        eventRepository.save(event);

        if (Objects.equals(group.getName(), "Complet")) {
            List<Group> groups = groupRepository.findAllByPromotion(promotion);
            for (Group g : groups) {
                for (User u : userRepository.findAllByPromotionAndGroup(promotion, g)) {
                    attendanceService.createAttendance(u, event);
                }
            }
        } else {
            for (User u : userRepository.findAllByPromotionAndGroup(promotion, group)) {
                attendanceService.createAttendance(u, event);
            }
        }
    }

    public Event[] getAllEvents(User user) {
        if (user.getRole() == Role.SECRETARIAT) {
            return eventRepository.findAll().toArray(new Event[0]);
        }
        List<Event> events = eventRepository.findAllByEnseignant(user);
        return events.toArray(new Event[0]);
    }
}
