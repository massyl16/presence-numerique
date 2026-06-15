package com.example.app_gestion_presences.service;

import com.example.app_gestion_presences.entity.AttendanceStatus;
import com.example.app_gestion_presences.entity.attendance;
import com.example.app_gestion_presences.entity.event;
import com.example.app_gestion_presences.entity.user;
import com.example.app_gestion_presences.repository.attendanceRepository;
import com.example.app_gestion_presences.repository.eventRepository;
import com.example.app_gestion_presences.repository.userRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class attendanceService {
    private final attendanceRepository attendance_repository;


    public attendanceService(attendanceRepository attendance_repository) {
        this.attendance_repository = attendance_repository;
    }

    public void createAttendance(user user, event event){

        attendance attendance = new attendance(user, event);

        attendance_repository.save(attendance);
    }

    public event[] getAllEvents(user user) {
        List<attendance> attendances=attendance_repository.findAllByUser(user);
        event[] events = new event[attendances.size()];
        for (int i = 0; i < events.length; i++) {
            events[i]=(attendances.get(i)).getEvent();
        }
        return events;
    }
}
