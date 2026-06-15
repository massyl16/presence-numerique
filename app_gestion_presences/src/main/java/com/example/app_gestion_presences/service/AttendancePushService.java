package com.example.app_gestion_presences.service;

import com.example.app_gestion_presences.AttendanceUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.example.app_gestion_presences.entity.attendance;

@Service
public class AttendancePushService {


    private final SimpMessagingTemplate messagingTemplate;

    public AttendancePushService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendUpdate(attendance attendance) {

        AttendanceUpdate dto = new AttendanceUpdate(
                attendance.getEvent().getId(),
                attendance.getUser().getId(),
                attendance.getStatus().name()
        );

        messagingTemplate.convertAndSend(
                "/topic/event/" + dto.eventId(),
                dto
        );

    }

    public void sendUpdateStarted(attendance attendance) {

        messagingTemplate.convertAndSend(
                "/topic/event/" + attendance.getEvent().getId() + "/start",
                true
        );

    }

    public void sendUpdateResponse(attendance attendance, String text) {

        messagingTemplate.convertAndSend(
                "/topic/event/" + attendance.getEvent().getId() + "/start/" + attendance.getUser().getId(),
                text
        );

    }
}
