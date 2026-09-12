package com.example.app_gestion_presences.controller;

import com.example.app_gestion_presences.entity.*;
import com.example.app_gestion_presences.repository.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class ResponsableController {

    private final UserRepository userRepository;
    private final PromotionRepository promotionRepository;
    private final EventRepository eventRepository;
    private final AttendanceRepository attendanceRepository;

    public ResponsableController(UserRepository userRepository,
                                  PromotionRepository promotionRepository,
                                  EventRepository eventRepository,
                                  AttendanceRepository attendanceRepository) {
        this.userRepository = userRepository;
        this.promotionRepository = promotionRepository;
        this.eventRepository = eventRepository;
        this.attendanceRepository = attendanceRepository;
    }

    private User getCurrentUser(UserDetails ud) {
        return userRepository.findByEmail(ud.getUsername()).orElseThrow();
    }

    @GetMapping("/responsable/feuilles")
    public String feuilles(@AuthenticationPrincipal UserDetails ud,
                            @RequestParam(required = false) Long promoId,
                            Model model) {
        User currentUser = getCurrentUser(ud);
        model.addAttribute("responsable", currentUser);

        // Scope : uniquement les formations affectées à ce responsable
        List<Promotion> myPromos = promotionRepository.findAll().stream()
                .filter(p -> p.getResponsableFormation() != null
                          && p.getResponsableFormation().getId().equals(currentUser.getId()))
                .toList();
        model.addAttribute("promotions", myPromos);
        model.addAttribute("promoId", promoId);

        if (promoId != null) {
            Promotion promo = myPromos.stream()
                    .filter(p -> p.getId().equals(promoId)).findFirst().orElse(null);
            if (promo != null) {
                model.addAttribute("promoNom", promo.getName());
                List<Event> seances = eventRepository
                        .findAllByPromotionAndClosedOrderByStartTimeDesc(promo, true);
                model.addAttribute("seances", seances);

                // Attendances par séance pour aperçu inline (Priorité 3)
                Map<Long, List<Attendance>> attendancesParSeance = seances.stream()
                        .collect(Collectors.toMap(Event::getId,
                                e -> attendanceRepository.findAllByEventId(e.getId())));
                model.addAttribute("attendancesParSeance", attendancesParSeance);

                // Cumul par étudiant (onglet 2)
                List<EtudiantStats> etudiantsStats = userRepository.findAllByPromotion(promo)
                        .stream()
                        .filter(u -> u.getRole() == Role.ETUDIANT)
                        .map(u -> {
                            List<Attendance> att = attendanceRepository.findAllByUser(u).stream()
                                    .filter(a -> seances.stream()
                                            .anyMatch(e -> e.getId().equals(a.getEvent().getId())))
                                    .toList();
                            long p  = att.stream().filter(a -> a.getStatus() == AttendanceStatus.Present).count();
                            long r  = att.stream().filter(a -> a.getStatus() == AttendanceStatus.Late).count();
                            long ab = att.stream().filter(a -> a.getStatus() == AttendanceStatus.Absent).count();
                            int taux = att.isEmpty() ? 0 : (int) Math.round((p + r) * 100.0 / att.size());
                            return new EtudiantStats(u, (int) p, (int) r, (int) ab, taux);
                        })
                        .sorted((a, b) -> Integer.compare(a.taux(), b.taux()))
                        .toList();
                model.addAttribute("etudiantsStats", etudiantsStats);
            }
        }
        return "responsable/feuilles";
    }

    public record EtudiantStats(User etudiant, int presents, int retards, int absents, int taux) {}
}
