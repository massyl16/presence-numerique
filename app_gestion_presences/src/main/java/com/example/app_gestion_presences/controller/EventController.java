package com.example.app_gestion_presences.controller;

import com.example.app_gestion_presences.AttendanceUpdate;
import com.example.app_gestion_presences.entity.*;
import com.example.app_gestion_presences.service.AttendancePushService;
import com.example.app_gestion_presences.service.attendanceService;
import com.example.app_gestion_presences.service.eventService;

import com.example.app_gestion_presences.repository.userRepository;
import com.example.app_gestion_presences.repository.eventRepository;
import com.example.app_gestion_presences.repository.attendanceRepository;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Controller
public class EventController {

    private final eventService eventService;

    private final attendanceService attendanceService;

    private final userRepository userRepository;

    private final eventRepository eventRepository;

    private final attendanceRepository attendanceRepository;

    private final AttendancePushService attendancePushService;

    public EventController(eventService eventService, attendanceService attendanceService, userRepository userRepository, eventRepository eventRepository, attendanceRepository attendanceRepository,AttendancePushService attendancePushService) {
        this.eventService = eventService;
        this.attendanceService = attendanceService;
        this.userRepository=userRepository;
        this.eventRepository=eventRepository;
        this.attendanceRepository=attendanceRepository;
        this.attendancePushService = attendancePushService;
    }

    @GetMapping("speaker/events")
    public String speakerEvents(Model model) {
        user connected_user = userRepository.findByEmailAndRole("cd@test.com",Role.Speaker);
        if(connected_user.getRole()==Role.Participant){
            model.addAttribute(
                    "events",
                    attendanceService.getAllEvents(connected_user));
        }
        else{
            model.addAttribute(
                    "events",
                    eventService.getAllEvents(connected_user));
        }
        return "speaker/events";
    }

    /*
        List<Participant> participants = Arrays.stream(participants_array).toList()
                .stream()
                .map(p -> new Participant(
                        p.getFirstname(),
                        p.getLastname(),
                        p.getEmail(),
                        p.getPhoto(),
                        attendances.stream().filter(a->a.getUser()==p).toString()
                ))
                .toList();*/
    @GetMapping("/speaker/events/{eventId}")
    public String speakerGetEventDetail(@PathVariable Long eventId, Model model) {

        List<attendance> attendances = attendanceRepository.findAllByEventId(eventId);

        List<Participant> participants = attendances.stream()
                .map(a -> {
                    user u = a.getUser();

                    return new Participant(
                            u.getId(),
                            u.getFirstname(),
                            u.getLastname(),
                            u.getEmail(),
                            u.getPhoto(),
                            a.getStatus().name()
                    );
                })
                .toList();

        model.addAttribute("event", eventRepository.findById(eventId).orElseThrow());
        model.addAttribute("participants", participants);

        attendances.getFirst().getEvent().setStarted(true);
        eventRepository.save(attendances.getFirst().getEvent());

        attendancePushService.sendUpdateStarted(attendances.getFirst());

        return "speaker/event-detail";
    }

    @GetMapping("participant/events")
    public String participantEvents(Model model) {
        user connected_user = userRepository.findByEmailAndRole("ef@test.com",Role.Participant);
        if(connected_user.getRole()==Role.Participant){
            model.addAttribute(
                    "events",
                    attendanceService.getAllEvents(connected_user));
        }
        else{
            model.addAttribute(
                    "events",
                    eventService.getAllEvents(connected_user));
        }
        return "participant/events";
    }

    /*
        List<Participant> participants = Arrays.stream(participants_array).toList()
                .stream()
                .map(p -> new Participant(
                        p.getFirstname(),
                        p.getLastname(),
                        p.getEmail(),
                        p.getPhoto(),
                        attendances.stream().filter(a->a.getUser()==p).toString()
                ))
                .toList();*/
    @GetMapping("/participant/events/{eventId}")
    public String participantGetEventDetail(@PathVariable Long eventId, Model model) {

        user connected_user = userRepository.findByEmailAndRole("ef@test.com",Role.Participant);
        List<attendance> attendances = attendanceRepository.findAllByEventId(eventId);

        attendance attendance = attendanceRepository
                .findAllByEventId(eventId)
                .stream()
                .filter(a -> a.getUser().getId().equals(connected_user.getId()))
                .findFirst()
                .orElseThrow();

        user participant = attendance.getUser();

        Participant participant_res = new Participant(
                participant.getId(),
                participant.getFirstname(),
                participant.getLastname(),
                participant.getEmail(),
                participant.getPhoto(),
                attendance.getStatus().name()
        );

        model.addAttribute("event", eventRepository.findById(eventId).orElseThrow());
        model.addAttribute("participant", participant_res);
        model.addAttribute("attendanceId",attendance.getId());

        return "participant/event-detail";
    }

    @PostMapping("/participant/checkin/{attendanceId}/{latitude}/{longitude}")
    @ResponseBody
    public void checkIn(@PathVariable Long attendanceId, @PathVariable Double latitude, @PathVariable Double longitude) {

        attendance a = attendanceRepository.findById(attendanceId)
                .orElseThrow();


        String text = a.verification(latitude,longitude);


        attendanceRepository.save(a);

        attendancePushService.sendUpdate(a);

        if(!Objects.equals(text, "Ok")){
            attendancePushService.sendUpdateResponse(a,text);
        }
    }

    @GetMapping("/secretary/events")
    public String secretaryEvents(Model model) {
        user connected_user = userRepository.findByEmailAndRole("ab@test.com",Role.Secretary);
        if(connected_user.getRole()==Role.Participant){
            model.addAttribute(
                    "events",
                    attendanceService.getAllEvents(connected_user));
        }
        else{
            model.addAttribute(
                    "events",
                    eventService.getAllEvents(connected_user));
        }
        return "secretary/events";
    }
    @GetMapping("/secretary/events/new")
    public String newEvent(Model model) {
        user connected_user = userRepository.findByEmailAndRole("ab@test.com",Role.Secretary);

        List<user> speakers = userRepository.findAllByRole(Role.Speaker);
        model.addAttribute("speakers",speakers);

        List<user> participants = userRepository.findAllByRole(Role.Participant);
        model.addAttribute("participants",participants);

        return "secretary/create-event";
    }

    @PostMapping("/secretary/events/new/create")
    public String createEvent(
            @RequestParam String title,
            @RequestParam String location,
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime,
            @RequestParam int lateTime,
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam Double radius,
            @RequestParam Long[] participant_list,
            @RequestParam Long speakerId

    ) {
        user[] participant_list_ids = new user[participant_list.length];
        for (int i = 0; i < participant_list.length; i++) {
            participant_list_ids[i]=userRepository.getReferenceById(participant_list[i]);
        }

        user connected_user = userRepository.findByEmailAndRole("ab@test.com",Role.Secretary);

        LocalDateTime tmp_lateTime = startTime.plusMinutes(lateTime);

        if (tmp_lateTime.isAfter(endTime)){
            tmp_lateTime = startTime.plusMinutes(15);
        }

        eventService.createEvent(
                title,
                startTime.toLocalDate().toString(),
                location,
                startTime,
                endTime,
                tmp_lateTime,
                latitude,
                longitude,
                radius,
                participant_list_ids,
                userRepository.getReferenceById(speakerId),
                userRepository.getReferenceById(connected_user.getId())
        );

        return "redirect:/secretary/events";
    }
}
