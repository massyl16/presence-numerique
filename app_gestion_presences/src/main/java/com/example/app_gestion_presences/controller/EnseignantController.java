package com.example.app_gestion_presences.controller;

import com.example.app_gestion_presences.entity.*;
import com.example.app_gestion_presences.repository.*;
import com.example.app_gestion_presences.service.AttendancePushService;
import com.example.app_gestion_presences.service.ExcelExportService;
import com.example.app_gestion_presences.service.attendanceService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class EnseignantController {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final PromotionRepository promotionRepository;
    private final GroupRepository groupRepository;
    private final AttendanceRepository attendanceRepository;
    private final attendanceService attendanceService;
    private final AttendancePushService pushService;
    private final ExcelExportService excelExportService;

    public EnseignantController(UserRepository userRepository,
                                 EventRepository eventRepository,
                                 PromotionRepository promotionRepository,
                                 GroupRepository groupRepository,
                                 AttendanceRepository attendanceRepository,
                                 attendanceService attendanceService,
                                 AttendancePushService pushService,
                                 ExcelExportService excelExportService) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.promotionRepository = promotionRepository;
        this.groupRepository = groupRepository;
        this.attendanceRepository = attendanceRepository;
        this.attendanceService = attendanceService;
        this.pushService = pushService;
        this.excelExportService = excelExportService;
    }

    private User getCurrentUser(UserDetails ud) {
        return userRepository.findByEmail(ud.getUsername()).orElseThrow();
    }

    // ── Accueil ──────────────────────────────────────────────────────────────

    @GetMapping("/enseignant/accueil")
    public String accueil(@AuthenticationPrincipal UserDetails ud, Model model) {
        User enseignant = getCurrentUser(ud);
        model.addAttribute("enseignant", enseignant);
        // Séance active en cours ?
        List<Event> sesActives = eventRepository.findAllByEnseignant(enseignant)
                .stream().filter(e -> e.getStarted() && !e.isClosed()).toList();
        model.addAttribute("seanceActive", sesActives.isEmpty() ? null : sesActives.get(0));
        return "enseignant/accueil";
    }

    // ── Faire l'appel : formulaire de sélection ──────────────────────────────

    @GetMapping("/enseignant/appel")
    public String appelForm(@AuthenticationPrincipal UserDetails ud, Model model) {
        User enseignant = getCurrentUser(ud);
        model.addAttribute("enseignant", enseignant);
        model.addAttribute("promotions", promotionRepository.findAll());
        model.addAttribute("groupes", groupRepository.findAll());

        // Si une séance est déjà ouverte, affiche directement le trombi
        List<Event> actives = eventRepository.findAllByEnseignant(enseignant)
                .stream().filter(e -> e.getStarted() && !e.isClosed()).toList();
        if (!actives.isEmpty()) {
            return "redirect:/enseignant/appel/" + actives.get(0).getId();
        }
        return "enseignant/appel";
    }

    /**
     * Démarre l'appel : crée la séance ET la démarre immédiatement.
     * Le GPS de l'enseignant est capturé par le navigateur et envoyé ici.
     * La séance est ouverte, les enregistrements de présence sont créés,
     * puis on pousse la notification WebSocket aux étudiants.
     */
    @PostMapping("/enseignant/appel/start")
    public String appelStart(@RequestParam Long promotionId,
                              @RequestParam(required = false) Long groupeId,
                              @RequestParam Double latitude,
                              @RequestParam Double longitude,
                              @RequestParam(defaultValue = "15") int lateThreshold,
                              @RequestParam(required = false) String title,
                              @AuthenticationPrincipal UserDetails ud) {
        User enseignant = getCurrentUser(ud);
        Promotion promotion = promotionRepository.getReferenceById(promotionId);

        // Groupe cible (null = tous les groupes de la promo)
        Group groupe = (groupeId != null) ? groupRepository.getReferenceById(groupeId) : null;

        // Crée la séance
        Event seance = new Event(LocalDateTime.now(), lateThreshold, latitude, longitude,
                enseignant, promotion, groupe);
        if (title != null && !title.isBlank()) seance.setTitle(title);
        seance.start(enseignant); // started=true, startTime=now, token généré
        eventRepository.save(seance);

        // Crée les enregistrements de présence pour les étudiants concernés
        List<User> etudiants;
        if (groupe == null) {
            etudiants = userRepository.findAllByPromotion(promotion)
                    .stream().filter(u -> u.getRole() == Role.ETUDIANT).toList();
        } else if (groupe.getName().equals("Complet")) {
            etudiants = userRepository.findAllByPromotion(promotion)
                    .stream().filter(u -> u.getRole() == Role.ETUDIANT).toList();
        } else {
            etudiants = userRepository.findAllByPromotionAndGroup(promotion, groupe)
                    .stream().filter(u -> u.getRole() == Role.ETUDIANT).toList();
        }
        for (User e : etudiants) {
            attendanceService.createAttendance(e, seance);
        }

        // Notifie les étudiants de la promotion via WebSocket
        pushService.sendAppelStart(seance);

        return "redirect:/enseignant/appel/" + seance.getId();
    }

    // ── Trombi d'une séance ouverte ───────────────────────────────────────────

    @GetMapping("/enseignant/appel/{seanceId}")
    public String appelTrombi(@PathVariable Long seanceId,
                               @AuthenticationPrincipal UserDetails ud, Model model) {
        User enseignant = getCurrentUser(ud);
        Event seance = eventRepository.findById(seanceId).orElseThrow();
        List<Attendance> attendances = attendanceRepository.findAllByEventId(seanceId);

        long nbPresent = attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.Present).count();
        long nbRetard  = attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.Late).count();
        long nbAbsent  = attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.Absent).count();

        model.addAttribute("enseignant",  enseignant);
        model.addAttribute("seance",      seance);
        model.addAttribute("attendances", attendances);
        model.addAttribute("nbPresent",   nbPresent);
        model.addAttribute("nbRetard",    nbRetard);
        model.addAttribute("nbAbsent",    nbAbsent);
        return "enseignant/appel-detail";
    }

    // ── Clôture de l'appel ────────────────────────────────────────────────────

    @PostMapping("/enseignant/appel/close/{seanceId}")
    public String appelClose(@PathVariable Long seanceId,
                              @AuthenticationPrincipal UserDetails ud) {
        User enseignant = getCurrentUser(ud);
        Event seance = eventRepository.findById(seanceId).orElseThrow();
        seance.close(enseignant);
        // Efface les coords GPS (RGPD)
        seance.setLatitude(null);
        seance.setLongitude(null);
        eventRepository.save(seance);
        pushService.sendUpdateClosed(seance);
        pushService.sendAppelClose(seance);
        return "redirect:/enseignant/accueil";
    }

    // ── Historique ────────────────────────────────────────────────────────────

    @GetMapping("/enseignant/historique")
    public String historique(@AuthenticationPrincipal UserDetails ud,
                              @RequestParam(required = false) Long promotionId,
                              Model model) {
        User enseignant = getCurrentUser(ud);
        List<Event> seances = eventRepository
                .findAllByEnseignantAndClosedOrderByStartTimeDesc(enseignant, true);

        if (promotionId != null) {
            seances = seances.stream()
                    .filter(e -> e.getPromotion().getId().equals(promotionId))
                    .toList();
        }

        // Statistiques globales
        int totalAppels = seances.size();
        double tauxMoyen = seances.isEmpty() ? 0 : seances.stream().mapToDouble(e -> {
            List<Attendance> att = attendanceRepository.findAllByEventId(e.getId());
            if (att.isEmpty()) return 0;
            long p = att.stream().filter(a -> a.getStatus() != AttendanceStatus.Absent).count();
            return (double) p / att.size() * 100;
        }).average().orElse(0);

        model.addAttribute("enseignant",  enseignant);
        model.addAttribute("seances",     seances);
        model.addAttribute("promotions",  promotionRepository.findAll());
        model.addAttribute("promotionId", promotionId);
        model.addAttribute("totalAppels", totalAppels);
        model.addAttribute("tauxMoyen",   Math.round(tauxMoyen));

        // Enrichit chaque séance avec ses stats
        List<SeanceStats> stats = seances.stream().map(e -> {
            List<Attendance> att = attendanceRepository.findAllByEventId(e.getId());
            long p = att.stream().filter(a -> a.getStatus() == AttendanceStatus.Present).count();
            long r = att.stream().filter(a -> a.getStatus() == AttendanceStatus.Late).count();
            long ab = att.stream().filter(a -> a.getStatus() == AttendanceStatus.Absent).count();
            int taux = att.isEmpty() ? 0 : (int) Math.round((p + r) * 100.0 / att.size());
            return new SeanceStats(e, p, r, ab, taux);
        }).toList();
        model.addAttribute("stats", stats);

        return "enseignant/historique";
    }

    // ── Détail d'une séance clôturée ─────────────────────────────────────────

    @GetMapping("/enseignant/historique/{seanceId}")
    public String historiqueDetail(@PathVariable Long seanceId,
                                    @AuthenticationPrincipal UserDetails ud, Model model) {
        User enseignant = getCurrentUser(ud);
        Event seance = eventRepository.findById(seanceId).orElseThrow();
        List<Attendance> attendances = attendanceRepository.findAllByEventId(seanceId);

        long nbPresent = attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.Present).count();
        long nbRetard  = attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.Late).count();
        long nbAbsent  = attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.Absent).count();
        int taux = attendances.isEmpty() ? 0 : (int) Math.round((nbPresent + nbRetard) * 100.0 / attendances.size());

        model.addAttribute("enseignant",  enseignant);
        model.addAttribute("seance",      seance);
        model.addAttribute("attendances", attendances);
        model.addAttribute("nbPresent",   nbPresent);
        model.addAttribute("nbRetard",    nbRetard);
        model.addAttribute("nbAbsent",    nbAbsent);
        model.addAttribute("taux",        taux);
        return "enseignant/seance-detail";
    }

    // ── Export Excel d'une séance ─────────────────────────────────────────────

    @GetMapping("/enseignant/historique/{seanceId}/csv")
    public void exportExcel(@PathVariable Long seanceId,
                             @AuthenticationPrincipal UserDetails ud,
                             HttpServletResponse response) throws IOException {
        Event seance = eventRepository.findById(seanceId).orElseThrow();
        List<Attendance> attendances = attendanceRepository.findAllByEventId(seanceId);
        excelExportService.exportPresences(seance, attendances, response);
    }

    /** DTO interne pour les stats de séance dans l'historique */
    public record SeanceStats(Event seance, long presents, long retards, long absents, int taux) {}
}
