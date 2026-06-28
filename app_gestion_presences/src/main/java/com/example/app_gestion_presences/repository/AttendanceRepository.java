package com.example.app_gestion_presences.repository;

import com.example.app_gestion_presences.entity.Attendance;
import com.example.app_gestion_presences.entity.Event;
import com.example.app_gestion_presences.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findAllByUser(User user);
    List<Attendance> findAllByEvent(Event event);
    List<Attendance> findAllByEventId(Long eventId);
    Optional<Attendance> findByUserAndEvent(User user, Event event);
}
