package com.example.app_gestion_presences.controller;

import com.example.app_gestion_presences.entity.*;
import com.example.app_gestion_presences.repository.*;
import com.example.app_gestion_presences.service.ExcelExportService;
import com.example.app_gestion_presences.service.promotionService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
public class SecretariatController {

    private final UserRepository userRepository;
    private final PromotionRepository promotionRepository;
    private final GroupRepository groupRepository;
    private final EventRepository eventRepository;
    private final AttendanceRepository attendanceRepository;
    private final promotionService promotionService;
    private final PasswordEncoder passwordEncoder;
    private final ExcelExportService excelExportService;

    public SecretariatController(UserRepository userRepository,
                                  PromotionRepository promotionRepository,
                                  GroupRepository groupRepository,
                                  EventRepository eventRepository,
                                  AttendanceRepository attendanceRepository,
                                  promotionService promotionService,
                                  PasswordEncoder passwordEncoder,
                                  ExcelExportService excelExportService) {
        this.userRepository = userRepository;
        this.promotionRepository = promotionRepository;
        this.groupRepository = groupRepository;
        this.eventRepository = eventRepository;
        this.attendanceRepository = attendanceRepository;
        this.promotionService = promotionService;
        this.passwordEncoder = passwordEncoder;
        this.excelExportService = excelExportService;
    }

    private User getCurrentUser(UserDetails ud) {
        return userRepository.findByEmail(ud.getUsername()).orElseThrow();
    }

    @GetMapping("/secretariat/accueil")
    public String accueil(@AuthenticationPrincipal UserDetails ud, Model model) {
        model.addAttribute("secretariat", getCurrentUser(ud));
        model.addAttribute("promotions", promotionRepository.findAll());
        model.addAttribute("nbEtudiants", userRepository.findAllByRole(Role.ETUDIANT).size());
        model.addAttribute("nbEnseignants", userRepository.findAllByRole(Role.ENSEIGNANT).size());
        return "secretariat/accueil";
    }

    // ── Gestion des promotions ──────────────────────────────────────────

    @GetMapping("/secretariat/promotions")
    public String promotions(@AuthenticationPrincipal UserDetails ud, Model model) {
        model.addAttribute("secretariat", getCurrentUser(ud));
        List<Promotion> promos = promotionRepository.findAll();
        model.addAttribute("promotions", promos);
        model.addAttribute("groupes", groupRepository.findAll());
        return "secretariat/promotions";
    }

    @PostMapping("/secretariat/promotions/create")
    public String createPromotion(@RequestParam String name) {
        if (promotionService.getAllPromotions().stream().noneMatch(p -> p.getName().equals(name))) {
            promotionService.createPromotion(name);
        }
        return "redirect:/secretariat/promotions";
    }

    // ── Gestion des groupes ─────────────────────────────────────────────

    @PostMapping("/secretariat/groupes/create")
    public String createGroupe(@RequestParam String name,
                                @RequestParam Long promotionId) {
        Promotion promotion = promotionRepository.getReferenceById(promotionId);
        boolean exists = groupRepository.findAllByPromotion(promotion)
                .stream().anyMatch(g -> g.getName().equals(name));
        if (!exists) {
            groupRepository.save(new Group(name, promotion));
        }
        return "redirect:/secretariat/promotions";
    }

    @PostMapping("/secretariat/groupes/{id}/delete")
    public String deleteGroupe(@PathVariable Long id) {
        groupRepository.deleteById(id);
        return "redirect:/secretariat/promotions";
    }

    @PostMapping("/secretariat/groupes/import-excel")
    public String importGroupesExcel(@RequestParam MultipartFile file,
                                      @RequestParam Long promotionId,
                                      RedirectAttributes ra) {
        if (file.isEmpty()) {
            ra.addFlashAttribute("importError", "Fichier vide.");
            return "redirect:/secretariat/etudiants";
        }
        Promotion promotion = promotionRepository.findById(promotionId).orElseThrow();
        List<Group> existing = groupRepository.findAllByPromotion(promotion);
        int created = 0;
        try (Workbook wb = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            for (Row row : sheet) {
                Cell cell = row.getCell(0);
                if (cell == null) continue;
                String name = (cell.getCellType() == CellType.STRING)
                        ? cell.getStringCellValue().trim()
                        : String.valueOf((int) cell.getNumericCellValue()).trim();
                if (name.isEmpty()) continue;
                String finalName = name;
                boolean exists = existing.stream().anyMatch(g -> g.getName().equals(finalName));
                if (!exists) {
                    Group g = groupRepository.save(new Group(name, promotion));
                    existing.add(g);
                    created++;
                }
            }
            ra.addFlashAttribute("importSuccess", created + " groupe(s) importé(s) avec succès.");
        } catch (Exception e) {
            ra.addFlashAttribute("importError", "Erreur lors de l'import : " + e.getMessage());
        }
        return "redirect:/secretariat/etudiants";
    }

    // ── Gestion des étudiants ───────────────────────────────────────────

    @GetMapping("/secretariat/etudiants")
    public String etudiants(@AuthenticationPrincipal UserDetails ud, Model model) {
        model.addAttribute("secretariat", getCurrentUser(ud));
        model.addAttribute("etudiants", userRepository.findAllByRole(Role.ETUDIANT));
        model.addAttribute("promotions", promotionRepository.findAll());
        model.addAttribute("groupes", groupRepository.findAll());
        return "secretariat/etudiants";
    }

    @PostMapping("/secretariat/etudiants/create")
    public String createEtudiant(@RequestParam String firstname,
                                  @RequestParam String lastname,
                                  @RequestParam String email,
                                  @RequestParam Long promotionId,
                                  @RequestParam Long groupId) {
        Promotion promotion = promotionRepository.getReferenceById(promotionId);
        Group group = groupRepository.getReferenceById(groupId);
        if (userRepository.findByEmail(email).isEmpty()) {
            User u = new User(firstname, lastname, email, promotion, group);
            u.setPassword(passwordEncoder.encode("password123"));
            userRepository.save(u);
        }
        return "redirect:/secretariat/etudiants";
    }

    @PostMapping("/secretariat/etudiants/{id}/delete")
    public String deleteEtudiant(@PathVariable Long id) {
        attendanceRepository.deleteAll(attendanceRepository.findAllByUser(
                userRepository.findById(id).orElseThrow()));
        userRepository.deleteById(id);
        return "redirect:/secretariat/etudiants";
    }

    // ── Enseignants ─────────────────────────────────────────────────────

    @GetMapping("/secretariat/enseignants")
    public String enseignants(@AuthenticationPrincipal UserDetails ud, Model model) {
        model.addAttribute("secretariat", getCurrentUser(ud));
        model.addAttribute("enseignants", userRepository.findAllByRole(Role.ENSEIGNANT));
        return "secretariat/enseignants";
    }

    @PostMapping("/secretariat/enseignants/create")
    public String createEnseignant(@RequestParam String firstname,
                                    @RequestParam String lastname,
                                    @RequestParam String email) {
        if (userRepository.findByEmail(email).isEmpty()) {
            User u = new User(firstname, lastname, email, Role.ENSEIGNANT);
            u.setPassword(passwordEncoder.encode("teacher123"));
            userRepository.save(u);
        }
        return "redirect:/secretariat/enseignants";
    }

    @PostMapping("/secretariat/enseignants/{id}/delete")
    public String deleteEnseignant(@PathVariable Long id) {
        userRepository.deleteById(id);
        return "redirect:/secretariat/enseignants";
    }

    // ── Feuilles de présence ─────────────────────────────────────────────

    @GetMapping("/secretariat/feuilles")
    public String feuilles(@AuthenticationPrincipal UserDetails ud,
                            @RequestParam(required = false) Long promoId,
                            Model model) {
        model.addAttribute("secretariat", getCurrentUser(ud));
        model.addAttribute("promotions", promotionRepository.findAll());
        model.addAttribute("promoId", promoId);

        if (promoId != null) {
            Promotion promo = promotionRepository.findById(promoId).orElse(null);
            if (promo != null) {
                model.addAttribute("promoNom", promo.getName());
                List<Event> seances = eventRepository
                        .findAllByPromotionAndClosedOrderByStartTimeDesc(promo, true);
                model.addAttribute("seances", seances);

                // Cumul par étudiant (onglet 2)
                List<EtudiantStats> etudiantsStats = userRepository.findAllByPromotion(promo)
                        .stream()
                        .filter(u -> u.getRole() == Role.ETUDIANT)
                        .map(u -> {
                            List<Attendance> att = attendanceRepository.findAllByUser(u).stream()
                                    .filter(a -> seances.stream().anyMatch(e -> e.getId().equals(a.getEvent().getId())))
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
        return "secretariat/feuilles";
    }

    public record EtudiantStats(User etudiant, int presents, int retards, int absents, int taux) {}

    @GetMapping("/secretariat/feuilles/{seanceId}")
    public String feuilleDetail(@PathVariable Long seanceId,
                                 @AuthenticationPrincipal UserDetails ud, Model model) {
        model.addAttribute("secretariat", getCurrentUser(ud));
        Event seance = eventRepository.findById(seanceId).orElseThrow();
        List<Attendance> attendances = attendanceRepository.findAllByEventId(seanceId);

        long nbPresent = attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.Present).count();
        long nbRetard  = attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.Late).count();
        long nbAbsent  = attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.Absent).count();
        int taux = attendances.isEmpty() ? 0 : (int) Math.round((nbPresent + nbRetard) * 100.0 / attendances.size());

        model.addAttribute("seance",      seance);
        model.addAttribute("attendances", attendances);
        model.addAttribute("nbPresent",   nbPresent);
        model.addAttribute("nbRetard",    nbRetard);
        model.addAttribute("nbAbsent",    nbAbsent);
        model.addAttribute("taux",        taux);
        return "secretariat/feuille-detail";
    }

    // ── Export Excel d'une feuille ────────────────────────────────────────────

    @GetMapping("/secretariat/feuilles/{seanceId}/csv")
    public void exportExcel(@PathVariable Long seanceId,
                             HttpServletResponse response) throws IOException {
        Event seance = eventRepository.findById(seanceId).orElseThrow();
        List<Attendance> attendances = attendanceRepository.findAllByEventId(seanceId);
        excelExportService.exportPresences(seance, attendances, response);
    }
}
