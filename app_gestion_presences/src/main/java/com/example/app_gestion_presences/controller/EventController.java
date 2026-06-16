package com.example.app_gestion_presences.controller;

import com.example.app_gestion_presences.AttendanceUpdate;
import com.example.app_gestion_presences.entity.*;
import com.example.app_gestion_presences.repository.*;
import com.example.app_gestion_presences.service.*;

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

    private final userService userService;

    private final userRepository userRepository;

    private final eventRepository eventRepository;

    private final attendanceRepository attendanceRepository;

    private final AttendancePushService attendancePushService;

    private final promotionRepository promotionRepository;

    private final groupRepository groupRepository;

    private final promotionService promotionService;

    private final groupService groupService;

    public EventController(eventService eventService, attendanceService attendanceService, userRepository userRepository, userService userService ,eventRepository eventRepository, attendanceRepository attendanceRepository,AttendancePushService attendancePushService, promotionRepository promotionRepository, groupRepository groupRepository, promotionService promotionService, groupService groupService) {
        this.eventService = eventService;
        this.attendanceService = attendanceService;
        this.userRepository=userRepository;
        this.userService=userService;
        this.eventRepository=eventRepository;
        this.attendanceRepository=attendanceRepository;
        this.attendancePushService = attendancePushService;
        this.promotionRepository=promotionRepository;
        this.groupRepository=groupRepository;
        this.promotionService=promotionService;
        this.groupService=groupService;
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
        user connected_user = userRepository.findByEmailAndRole("cd@test.com",Role.Speaker);

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
                            u.getPromotion().getName(),
                            u.getGroup().getName(),
                            a.getStatus().name()
                    );
                })
                .toList();

        model.addAttribute("event", eventRepository.findById(eventId).orElseThrow());
        model.addAttribute("participants", participants);

        event event = eventRepository.getReferenceById(eventId);
        event.start(connected_user);
        eventRepository.save(event);

        attendancePushService.sendUpdateStarted(event);

        return "speaker/event-detail";
    }

    @GetMapping("/speaker/events/close/{eventId}")
    public String speakerGetEventClose(@PathVariable Long eventId, Model model) {
        user connected_user = userRepository.findByEmailAndRole("cd@test.com",Role.Speaker);

        event event = eventRepository.getReferenceById(eventId);

        event.close(connected_user);
        eventRepository.save(event);

        attendancePushService.sendUpdateClosed(event);

        //envoyer au secretariat

        return "redirect:/speaker/events";
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
                participant.getPromotion().getName(),
                participant.getGroup().getName(),
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
    @GetMapping("/speaker/events/new")
    public String getPromotion(Model model) {

        List<promotion> promotions = promotionService.getAllPromotions();
        model.addAttribute("promotions",promotions);

        return "speaker/create-event";
    }

    @PostMapping("/speaker/events/new/select")
    public String speakerGetInfos(Model model, @RequestParam Long promotionId) {

        promotion promotion = promotionRepository.getReferenceById(promotionId);
        List<group> groups = groupService.getAllGroups(promotion);
        model.addAttribute("promotion",promotion);
        model.addAttribute("groups",groups);

        return "speaker/create-event-bis";
    }

    @PostMapping("/speaker/events/new/select/create")
    public String createEvent(
            @RequestParam String title,
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam Long promotionId,
            @RequestParam Long groupId

    ) {


        user connected_user = userRepository.findByEmailAndRole("cd@test.com",Role.Speaker);

        eventService.createEvent(
                title,
                LocalDateTime.now(),
                latitude,
                longitude,
                userRepository.getReferenceById(connected_user.getId()),
                promotionRepository.getReferenceById(promotionId),
                groupRepository.getReferenceById(groupId)
        );

        return "redirect:/speaker/events";
    }

    @GetMapping("/secretary/events")
    public String secretaryEvents(Model model) {
        user connected_user = userRepository.findByEmailAndRole("ab@test.com",Role.Secretary);
        model.addAttribute(
                "events",
                eventService.getAllEvents(connected_user));
        return "secretary/events";
    }

    @GetMapping("/secretary/group_gestion")
    public String secretaryGestion(Model model){
        return "secretary/group_gestion";
    }

    @GetMapping("/secretary/group_gestion/promotion")
    public String secretaryGestionPromotion(Model model){
        return "secretary/group_gestion_promotion";
    }

    @PostMapping("/secretary/group_gestion/promotion/create")
    public String createPromotion(@RequestParam String name){
        //check si existe déjà
        List<promotion> promotions = promotionService.getAllPromotions();
        for (int i = 0; i < promotions.size(); i++) {
            if (Objects.equals(promotions.get(i).getName(), name)){
                //dire already exist à l'utilisateur
                return "redirect:/secretary/group_gestion/promotion";
            }
        }
        promotionService.createPromotion(name);
        return "redirect:/secretary/group_gestion";
    }

    @GetMapping("/secretary/group_gestion/group")
    public String secretaryGestionGroup(Model model){

        List<promotion> promotions = promotionService.getAllPromotions();
        model.addAttribute("promotions",promotions);

        return "secretary/group_gestion_group";
    }

    @PostMapping("/secretary/group_gestion/group/select")
    public String secretaryGetInfos(Model model,@RequestParam Long promotionId){

        promotion promotion = promotionRepository.getReferenceById(promotionId);
        List<group> groups = groupService.getAllGroups(promotion);
        model.addAttribute("promotion",promotion);
        model.addAttribute("groups",groups);

        return "secretary/group_gestion_group_select";
    }

    @PostMapping("/secretary/group_gestion/group/select/create")
    public String createGroup(Model model,@RequestParam Long promotionId,@RequestParam String name){
        //check si existe déjà
        promotion promotion = promotionRepository.getReferenceById(promotionId);
        List<group> groups = groupService.getAllGroups(promotion);
        for (int i = 0; i < groups.size(); i++) {
            if (Objects.equals(groups.get(i).getName(), name)){
                //dire already exist à l'utilisateur
                return "redirect:/secretary/group_gestion/group/select";
            }
        }
        groupService.createGroup(name,promotion);
        return "redirect:/secretary/group_gestion";
    }

    @GetMapping("/secretary/group_gestion/participant")
    public String secretaryGestionParticipant(Model model){

        List<promotion> promotions = promotionService.getAllPromotions();
        model.addAttribute("promotions",promotions);

        return "secretary/group_gestion_participant";
    }

    @PostMapping("/secretary/group_gestion/participant/select")
    public String secretaryGetInfosParticipant(Model model,@RequestParam Long promotionId){

        promotion promotion = promotionRepository.getReferenceById(promotionId);
        List<group> groups = groupService.getAllGroups(promotion);
        model.addAttribute("promotion",promotion);
        model.addAttribute("groups",groups);

        return "secretary/group_gestion_participant_select";
    }

    @PostMapping("/secretary/group_gestion/participant/select/create")
    public String createParticipant(Model model,
                                    @RequestParam String firstname,
                                    @RequestParam String lastname,
                                    @RequestParam String email,
                                    @RequestParam Long promotionId,
                                    @RequestParam Long groupId
                                    ){
        //check si existe déjà
        promotion promotion = promotionRepository.getReferenceById(promotionId);
        group group = groupRepository.getReferenceById(groupId);
        List<user> users = userRepository.findByEmailAndPromotion(email,promotion);
        for (int i = 0; i < users.size(); i++) {
            if (Objects.equals(users.get(i).getEmail(), email)){
                //dire already exist à l'utilisateur
                return "redirect:/secretary/group_gestion/participant";
            }
        }
        userService.createUserParticipant(firstname,lastname,email,promotion,group);
        return "redirect:/secretary/group_gestion";
    }
}
