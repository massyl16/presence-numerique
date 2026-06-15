package com.example.app_gestion_presences;

import com.example.app_gestion_presences.entity.attendance;
import com.example.app_gestion_presences.entity.event;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import com.example.app_gestion_presences.entity.user;
import com.example.app_gestion_presences.entity.Role;
import com.example.app_gestion_presences.repository.userRepository;
import com.example.app_gestion_presences.repository.eventRepository;
import com.example.app_gestion_presences.repository.attendanceRepository;

import java.time.LocalDateTime;

@Component
public class DataInitializer {

    private final userRepository userRepository;

    private final eventRepository eventRepository;

    private final attendanceRepository attendanceRepository;

    public DataInitializer(userRepository userRepository, eventRepository eventRepository, attendanceRepository attendanceRepository) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.attendanceRepository = attendanceRepository;
    }

    @PostConstruct
    public void init() {

        if (userRepository.count() == 0) {

            user part1 = new user("e","f","ef@test.com", Role.Participant);

            user part2 = new user("g","h","gh@test.com", Role.Participant);

            userRepository.save(part1);
            userRepository.save(part2);

            user[] participant_list = new user[]{part1, part2};

            user secretary = new user("a", "b", "ab@test.com", Role.Secretary);

            userRepository.save(secretary);

            user teacher = new user("c", "d", "cd@test.com", Role.Speaker);

            userRepository.save(teacher);

            event event1 = new event("event1", "01-01-2000", "here", 20.0, 20.0, 25.0,
                    LocalDateTime.of(2000, 1, 1, 10, 0),
                    LocalDateTime.of(2000, 1, 1, 12, 0),
                    LocalDateTime.of(2000, 1, 1, 10, 10),
                    participant_list,teacher, secretary);

            eventRepository.save(event1);

            for (int i = 0; i < participant_list.length; i++) {
                attendance attendance = new attendance(participant_list[i], event1);

                attendanceRepository.save(attendance);
            }
        }
    }
}