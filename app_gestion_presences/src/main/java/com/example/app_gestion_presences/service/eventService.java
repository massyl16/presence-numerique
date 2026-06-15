package com.example.app_gestion_presences.service;

import com.example.app_gestion_presences.entity.Role;
import com.example.app_gestion_presences.entity.event;
import com.example.app_gestion_presences.entity.user;

import com.example.app_gestion_presences.repository.eventRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class eventService {
    private final eventRepository event_repository;

    private final attendanceService attendance_service;


    public eventService(eventRepository event_repository, attendanceService attendance_service) {
        this.event_repository = event_repository;
        this.attendance_service = attendance_service;
    }

    public void createEvent(String title, String date, String place, LocalDateTime startTime, LocalDateTime endTime, LocalDateTime lateTime, Double latitude, Double longitude, Double allowedRadiusMeters, user[] participant_list, user speaker, user secretary){

        event event = new event(title,date,place,latitude,longitude,allowedRadiusMeters,startTime,endTime,lateTime,participant_list,speaker,secretary);

        event_repository.save(event);
        //creer attendances

        for (int i = 0; i < participant_list.length; i++) {
            attendance_service.createAttendance(participant_list[i],event);
        }
    }

    public void Start(user user,event event){
        if(user.getRole()== Role.Speaker){
            event.setStarted(true);
            //if (LocalDateTime.now().isAfter(lateTime)){
            //lateTime=LocalDateTime.now();
            //}
        }
    }

    public event[] getAllEvents(user user) {
        List<event> attendances;
        if(user.getRole()==Role.Secretary){
            attendances=event_repository.findAllBySecretary(user);
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
