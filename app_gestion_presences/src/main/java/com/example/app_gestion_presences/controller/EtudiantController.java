package com.example.app_gestion_presences.controller;

import com.example.app_gestion_presences.entity.*;
import com.example.app_gestion_presences.repository.*;
import com.example.app_gestion_presences.service.AttendancePushService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
public class EtudiantController {

    private static final Logger log = LoggerFactory.getLogger(EtudiantController.class);

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final AttendanceRepository attendanceRepository;
    private final AttendancePushService pushService;

    public EtudiantController(UserRepository userRepository,
                               EventRepository eventRepository,
                               AttendanceRepository attendanceRepository,
                               AttendancePushService pushService) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.attendanceRepository = attendanceRepository;
        this.pushService = pushService;
    }

    private User getCurrentUser(UserDetails ud) {
        return userRepository.findByEmail(ud.getUsername()).orElseThrow();
    }

    // ── Accueil : 2 états (aucun appel / appel en cours) ──────────────────────

    @GetMapping("/etudiant/accueil")
    public String accueil(@AuthenticationPrincipal UserDetails ud, Model model) {
        User etudiant = getCurrentUser(ud);
        model.addAttribute("etudiant", etudiant);

        // Cherche une séance active pour la promotion ET le groupe de l'étudiant
        if (etudiant.getPromotion() != null) {
            Optional<Event> seanceActive = eventRepository.findActiveByPromotion(etudiant.getPromotion());
            seanceActive.ifPresent(e -> {
                // N'affiche le bouton que si la séance concerne bien ce groupe
                boolean concerneEtudiant = e.getGroup() == null
                        || e.getGroup().getName().equals("Complet")
                        || (etudiant.getGroup() != null
                            && e.getGroup().getId().equals(etudiant.getGroup().getId()));
                if (concerneEtudiant) {
                    model.addAttribute("seanceActive", e);
                }
            });
        }
        return "etudiant/accueil";
    }

    // ── Check-in anti-fraude ───────────────────────────────────────────────────
    // §5.2 : le serveur identifie l'étudiant UNIQUEMENT via la session Spring Security.
    // Aucun identifiant étudiant ne vient du client — impossible de valider pour quelqu'un d'autre.

    @PostMapping("/etudiant/checkin")
    @ResponseBody
    public ResponseEntity<String> checkin(@RequestParam Double latitude,
                                           @RequestParam Double longitude,
                                           @AuthenticationPrincipal UserDetails ud) {
        User etudiant = getCurrentUser(ud);

        if (etudiant.getPromotion() == null) {
            return refus(etudiant, "Vous n'êtes rattaché à aucune promotion.");
        }

        // Trouve la séance active pour la promotion de l'étudiant
        Optional<Event> seanceOpt = eventRepository.findActiveByPromotion(etudiant.getPromotion());
        if (seanceOpt.isEmpty()) {
            return refus(etudiant, "Aucun appel en cours pour votre promotion.");
        }
        Event seance = seanceOpt.get();

        // Vérifie que la séance concerne bien le groupe de l'étudiant (ou "Complet")
        if (seance.getGroup() != null
                && !seance.getGroup().getName().equals("Complet")
                && !seance.getGroup().getId().equals(etudiant.getGroup() != null ? etudiant.getGroup().getId() : -1L)) {
            return refus(etudiant, "Cet appel ne concerne pas votre groupe.");
        }

        // Trouve l'enregistrement de présence pour cet étudiant (créé à l'ouverture de la séance)
        Optional<Attendance> attOpt = attendanceRepository.findByUserAndEvent(etudiant, seance);
        if (attOpt.isEmpty()) {
            return refus(etudiant, "Aucune feuille de présence trouvée pour cette séance.");
        }
        Attendance attendance = attOpt.get();

        // Anti-doublon : déjà validé
        if (attendance.getStatus() == AttendanceStatus.Present
                || attendance.getStatus() == AttendanceStatus.Late) {
            return refus(etudiant, "Votre présence est déjà enregistrée.");
        }

        // Validation Haversine + calcul retard
        String result = attendance.verification(latitude, longitude);
        attendanceRepository.save(attendance);
        pushService.sendUpdate(attendance);

        if (!result.equals("Ok")) {
            pushService.sendUpdateResponse(attendance, result);
            return refus(etudiant, result);
        }
        log.info("CHECKIN_OK seance={} etudiant={} statut={} retard={}min",
                seance.getId(), etudiant.getEmail(), attendance.getStatus(), attendance.getLateMinutes());
        return ResponseEntity.ok("Ok");
    }

    /** Journalise le refus (jamais les coordonnées GPS — RGPD) et renvoie le motif à l'étudiant. */
    private ResponseEntity<String> refus(User etudiant, String motif) {
        log.info("CHECKIN_REFUSE etudiant={} motif=\"{}\"", etudiant.getEmail(), motif);
        return ResponseEntity.status(400).body(motif);
    }

    // ── Historique des présences ───────────────────────────────────────────────

    @GetMapping("/etudiant/presences")
    public String presences(@AuthenticationPrincipal UserDetails ud, Model model) {
        User etudiant = getCurrentUser(ud);
        List<Attendance> liste = attendanceRepository.findAllByUser(etudiant);

        long nbPresent = liste.stream().filter(a -> a.getStatus() == AttendanceStatus.Present).count();
        long nbRetard  = liste.stream().filter(a -> a.getStatus() == AttendanceStatus.Late).count();
        long nbAbsent  = liste.stream().filter(a -> a.getStatus() == AttendanceStatus.Absent).count();
        int total      = liste.size();
        int pct        = total > 0 ? (int) Math.round((nbPresent + nbRetard) * 100.0 / total) : 0;

        model.addAttribute("etudiant",  etudiant);
        model.addAttribute("presences", liste);
        model.addAttribute("nbPresent", nbPresent);
        model.addAttribute("nbRetard",  nbRetard);
        model.addAttribute("nbAbsent",  nbAbsent);
        model.addAttribute("pctPresence", pct);
        return "etudiant/presences";
    }
}
