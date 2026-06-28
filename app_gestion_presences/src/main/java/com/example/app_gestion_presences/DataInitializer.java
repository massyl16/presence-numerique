package com.example.app_gestion_presences;

import com.example.app_gestion_presences.entity.*;
import com.example.app_gestion_presences.repository.*;
import com.example.app_gestion_presences.entity.AttendanceStatus;
import jakarta.annotation.PostConstruct;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final AttendanceRepository attendanceRepository;
    private final PromotionRepository promotionRepository;
    private final GroupRepository groupRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, EventRepository eventRepository,
                           AttendanceRepository attendanceRepository, PromotionRepository promotionRepository,
                           GroupRepository groupRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.attendanceRepository = attendanceRepository;
        this.promotionRepository = promotionRepository;
        this.groupRepository = groupRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void init() {
        if (userRepository.findByEmail("admin@test.com").isEmpty()) {
            User admin = new User("Admin", "Système", "admin@test.com", Role.ADMIN);
            admin.setPassword(passwordEncoder.encode("admin123"));
            userRepository.save(admin);
        }
        if (promotionRepository.count() > 0) return;

        // ── Promotions et groupes ──────────────────────────────────────────────
        Promotion m1 = promotionRepository.save(new Promotion("M1 MIAGE"));
        Promotion m2 = promotionRepository.save(new Promotion("M2 MIAGE"));

        groupRepository.save(new Group("Complet", m1));
        groupRepository.save(new Group("Complet", m2));
        Group m1g1 = groupRepository.save(new Group("Groupe 1", m1));
        Group m1g2 = groupRepository.save(new Group("Groupe 2", m1));
        Group m2g1 = groupRepository.save(new Group("Groupe 1", m2));
        Group m2g2 = groupRepository.save(new Group("Groupe 2", m2));

        // ── Étudiants M1 ──────────────────────────────────────────────────────
        User alice   = saveStudent("Alice",   "Martin",   "alice@test.com",   m1, m1g1);
        User bob     = saveStudent("Bob",     "Dupont",   "bob@test.com",     m1, m1g1);
        User carla   = saveStudent("Carla",   "Bernard",  "carla@test.com",   m1, m1g1);
        User dylan   = saveStudent("Dylan",   "Morel",    "dylan@test.com",   m1, m1g1);
        User emilie  = saveStudent("Émilie",  "Leclerc",  "emilie@test.com",  m1, m1g1);
        User farid   = saveStudent("Farid",   "Haddad",   "farid@test.com",   m1, m1g2);
        User gaelle  = saveStudent("Gaëlle",  "Petit",    "gaelle@test.com",  m1, m1g2);
        User hugo    = saveStudent("Hugo",    "Simon",    "hugo@test.com",    m1, m1g2);
        User ines    = saveStudent("Inès",    "Nguyen",   "ines@test.com",    m1, m1g2);
        User jordan  = saveStudent("Jordan",  "Roux",     "jordan@test.com",  m1, m1g2);

        // ── Étudiants M2 ──────────────────────────────────────────────────────
        User clara   = saveStudent("Clara",   "Leroy",    "clara@test.com",   m2, m2g1);
        User david   = saveStudent("David",   "Garnier",  "david@test.com",   m2, m2g1);
        User elena   = saveStudent("Elena",   "Fontaine", "elena@test.com",   m2, m2g1);
        User felix   = saveStudent("Félix",   "Aubert",   "felix@test.com",   m2, m2g1);
        User grace   = saveStudent("Grace",   "Rousseau", "grace@test.com",   m2, m2g1);
        User hassan  = saveStudent("Hassan",  "Bakri",    "hassan@test.com",  m2, m2g2);
        User isabelle= saveStudent("Isabelle","Lemaire",  "isabelle@test.com",m2, m2g2);
        User julien  = saveStudent("Julien",  "Colin",    "julien@test.com",  m2, m2g2);
        User karine  = saveStudent("Karine",  "Vidal",    "karine@test.com",  m2, m2g2);
        User leo     = saveStudent("Léo",     "Mercier",  "leo@test.com",     m2, m2g2);

        // ── Enseignants ───────────────────────────────────────────────────────
        User charles = saveTeacher("Charles", "Dupont",  "cd@test.com",    "teacher123");
        User sophie  = saveTeacher("Sophie",  "Lambert", "sl@test.com",    "teacher123");

        // ── Secrétariat ───────────────────────────────────────────────────────
        User secretary = new User("Anna", "Bernard", "ab@test.com", Role.SECRETARIAT);
        secretary.setPassword(passwordEncoder.encode("secretary123"));
        userRepository.save(secretary);

        // ── Séances clôturées pour la démo ────────────────────────────────────
        // Séance 1 — M1 Groupe 1, il y a 5 jours (Algorithmique)
        Event s1 = new Event();
        s1.setTitle("Algorithmique avancée");
        s1.setEnseignant(charles);
        s1.setPromotion(m1);
        s1.setGroup(m1g1);
        s1.setStartTime(LocalDateTime.now().minusDays(5).withHour(9).withMinute(0).withSecond(0));
        s1.setLateThreshold(10);
        s1.setStarted(false);
        s1.setToken(null);
        eventRepository.save(s1);

        // Alice présente, Bob retard 8 min, Carla présente, Dylan absent, Émilie retard 14 min
        saveAttendance(s1, alice,  AttendanceStatus.Present, s1.getStartTime().plusMinutes(2),  0);
        saveAttendance(s1, bob,    AttendanceStatus.Late,    s1.getStartTime().plusMinutes(18), 8);
        saveAttendance(s1, carla,  AttendanceStatus.Present, s1.getStartTime().plusMinutes(4),  0);
        saveAttendance(s1, dylan,  AttendanceStatus.Absent,  null,                              0);
        saveAttendance(s1, emilie, AttendanceStatus.Late,    s1.getStartTime().plusMinutes(24), 14);

        // Séance 2 — M1 Groupe 2, il y a 5 jours (BDD)
        Event s2 = new Event();
        s2.setTitle("Bases de données relationnelles");
        s2.setEnseignant(charles);
        s2.setPromotion(m1);
        s2.setGroup(m1g2);
        s2.setStartTime(LocalDateTime.now().minusDays(5).withHour(14).withMinute(0).withSecond(0));
        s2.setLateThreshold(15);
        s2.setStarted(false);
        s2.setToken(null);
        eventRepository.save(s2);

        saveAttendance(s2, farid,  AttendanceStatus.Present, s2.getStartTime().plusMinutes(3),  0);
        saveAttendance(s2, gaelle, AttendanceStatus.Absent,  null,                              0);
        saveAttendance(s2, hugo,   AttendanceStatus.Present, s2.getStartTime().plusMinutes(7),  0);
        saveAttendance(s2, ines,   AttendanceStatus.Late,    s2.getStartTime().plusMinutes(20), 5);
        saveAttendance(s2, jordan, AttendanceStatus.Present, s2.getStartTime().plusMinutes(1),  0);

        // Séance 3 — M2 Groupe 1, il y a 3 jours (Architecture)
        Event s3 = new Event();
        s3.setTitle("Architecture logicielle");
        s3.setEnseignant(sophie);
        s3.setPromotion(m2);
        s3.setGroup(m2g1);
        s3.setStartTime(LocalDateTime.now().minusDays(3).withHour(10).withMinute(30).withSecond(0));
        s3.setLateThreshold(10);
        s3.setStarted(false);
        s3.setToken(null);
        eventRepository.save(s3);

        saveAttendance(s3, clara,   AttendanceStatus.Present, s3.getStartTime().plusMinutes(5),  0);
        saveAttendance(s3, david,   AttendanceStatus.Present, s3.getStartTime().plusMinutes(2),  0);
        saveAttendance(s3, elena,   AttendanceStatus.Late,    s3.getStartTime().plusMinutes(15), 5);
        saveAttendance(s3, felix,   AttendanceStatus.Absent,  null,                              0);
        saveAttendance(s3, grace,   AttendanceStatus.Present, s3.getStartTime().plusMinutes(8),  0);

        // Séance 4 — M2 Groupe 2, hier (DevOps)
        Event s4 = new Event();
        s4.setTitle("DevOps & CI/CD");
        s4.setEnseignant(sophie);
        s4.setPromotion(m2);
        s4.setGroup(m2g2);
        s4.setStartTime(LocalDateTime.now().minusDays(1).withHour(13).withMinute(30).withSecond(0));
        s4.setLateThreshold(5);
        s4.setStarted(false);
        s4.setToken(null);
        eventRepository.save(s4);

        saveAttendance(s4, hassan,   AttendanceStatus.Present, s4.getStartTime().plusMinutes(1),  0);
        saveAttendance(s4, isabelle, AttendanceStatus.Absent,  null,                              0);
        saveAttendance(s4, julien,   AttendanceStatus.Late,    s4.getStartTime().plusMinutes(9),  4);
        saveAttendance(s4, karine,   AttendanceStatus.Present, s4.getStartTime().plusMinutes(3),  0);
        saveAttendance(s4, leo,      AttendanceStatus.Present, s4.getStartTime().plusMinutes(4),  0);
    }

    private User saveStudent(String firstname, String lastname, String email,
                              Promotion promotion, Group group) {
        User u = new User(firstname, lastname, email, promotion, group);
        u.setPassword(passwordEncoder.encode("password123"));
        return userRepository.save(u);
    }

    private User saveTeacher(String firstname, String lastname, String email, String password) {
        User u = new User(firstname, lastname, email, Role.ENSEIGNANT);
        u.setPassword(passwordEncoder.encode(password));
        return userRepository.save(u);
    }

    private void saveAttendance(Event event, User student, AttendanceStatus status,
                                 LocalDateTime validationTime, int lateMinutes) {
        Attendance a = new Attendance();
        a.setUser(student);
        a.setEvent(event);
        a.setStatus(status);
        a.setValidationTime(validationTime);
        a.setLateMinutes(lateMinutes);
        attendanceRepository.save(a);
    }
}
