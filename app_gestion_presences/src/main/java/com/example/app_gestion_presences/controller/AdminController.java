package com.example.app_gestion_presences.controller;

import com.example.app_gestion_presences.entity.*;
import com.example.app_gestion_presences.repository.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class AdminController {

    private final UserRepository userRepository;
    private final PromotionRepository promotionRepository;
    private final EventRepository eventRepository;
    private final AttendanceRepository attendanceRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(UserRepository userRepository,
                           PromotionRepository promotionRepository,
                           EventRepository eventRepository,
                           AttendanceRepository attendanceRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.promotionRepository = promotionRepository;
        this.eventRepository = eventRepository;
        this.attendanceRepository = attendanceRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private void addAdminAttrs(UserDetails ud, Model model) {
        model.addAttribute("email", ud.getUsername());
    }

    // ── Accueil ──────────────────────────────────────────────────────────────

    @GetMapping("/admin/accueil")
    public String accueil(@AuthenticationPrincipal UserDetails ud, Model model) {
        addAdminAttrs(ud, model);
        model.addAttribute("nbEtudiants",   userRepository.findAllByRole(Role.ETUDIANT).size());
        model.addAttribute("nbEnseignants", userRepository.findAllByRole(Role.ENSEIGNANT).size());
        model.addAttribute("nbSecretariat", userRepository.findAllByRole(Role.SECRETARIAT).size());
        model.addAttribute("users", userRepository.findAll());
        return "admin/accueil";
    }

    @PostMapping("/admin/users/{id}/delete")
    public String deleteUser(@PathVariable Long id,
                              @AuthenticationPrincipal UserDetails ud) {
        userRepository.findById(id).ifPresent(u -> {
            if (!u.getEmail().equals(ud.getUsername())) {
                userRepository.delete(u);
            }
        });
        return "redirect:/admin/accueil";
    }

    @PostMapping("/admin/users/{id}/reset-password")
    public String resetPassword(@PathVariable Long id) {
        userRepository.findById(id).ifPresent(u -> {
            String defaultPwd = switch (u.getRole()) {
                case ENSEIGNANT  -> "teacher123";
                case SECRETARIAT -> "secretary123";
                case ADMIN       -> "admin123";
                default          -> "password123";
            };
            u.setPassword(passwordEncoder.encode(defaultPwd));
            userRepository.save(u);
        });
        return "redirect:/admin/accueil";
    }

    // ── §7.2 Affectations ────────────────────────────────────────────────────

    @GetMapping("/admin/affectations")
    public String affectations(@AuthenticationPrincipal UserDetails ud, Model model) {
        addAdminAttrs(ud, model);
        model.addAttribute("promotions",   promotionRepository.findAll());
        model.addAttribute("secretaires",  userRepository.findAllByRole(Role.SECRETARIAT));
        return "admin/affectations";
    }

    @PostMapping("/admin/affectations/{promoId}/affecter")
    public String affecter(@PathVariable Long promoId,
                            @RequestParam(required = false) Long secretaireId) {
        promotionRepository.findById(promoId).ifPresent(promo -> {
            User secretaire = secretaireId != null
                    ? userRepository.findById(secretaireId).orElse(null)
                    : null;
            promo.setSecretaireResponsable(secretaire);
            promotionRepository.save(promo);
        });
        return "redirect:/admin/affectations";
    }

    // ── §7.3 Historique global ───────────────────────────────────────────────

    @GetMapping("/admin/historique")
    public String historique(@AuthenticationPrincipal UserDetails ud,
                              @RequestParam(required = false) Long promoId,
                              Model model) {
        addAdminAttrs(ud, model);
        model.addAttribute("promotions", promotionRepository.findAll());
        model.addAttribute("promoId",    promoId);

        List<Event> seances = (promoId != null)
                ? eventRepository.findAllByPromotionAndClosedOrderByStartTimeDesc(
                        promotionRepository.getReferenceById(promoId), true)
                : eventRepository.findAllByClosedOrderByStartTimeDesc(true);

        List<SeanceStats> stats = seances.stream().map(e -> {
            List<Attendance> att = attendanceRepository.findAllByEventId(e.getId());
            long p  = att.stream().filter(a -> a.getStatus() == AttendanceStatus.Present).count();
            long r  = att.stream().filter(a -> a.getStatus() == AttendanceStatus.Late).count();
            long ab = att.stream().filter(a -> a.getStatus() == AttendanceStatus.Absent).count();
            int taux = att.isEmpty() ? 0 : (int) Math.round((p + r) * 100.0 / att.size());
            return new SeanceStats(e, p, r, ab, taux);
        }).toList();

        model.addAttribute("stats",       stats);
        model.addAttribute("totalSeances", seances.size());
        return "admin/historique";
    }

    public record SeanceStats(Event seance, long presents, long retards, long absents, int taux) {}
}
