package com.example.app_gestion_presences.service;

import com.example.app_gestion_presences.entity.*;
import com.example.app_gestion_presences.repository.AttendanceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class attendanceService {

    private final AttendanceRepository attendanceRepository;

    public attendanceService(AttendanceRepository attendanceRepository) {
        this.attendanceRepository = attendanceRepository;
    }

    public void createAttendance(User user, Event event) {
        // Anti-doublon
        if (attendanceRepository.findByUserAndEvent(user, event).isPresent()) return;
        attendanceRepository.save(new Attendance(user, event));
    }

    public Event[] getAllEvents(User user) {
        List<Attendance> attendances = attendanceRepository.findAllByUser(user);
        return attendances.stream().map(Attendance::getEvent).toArray(Event[]::new);
    }
}
