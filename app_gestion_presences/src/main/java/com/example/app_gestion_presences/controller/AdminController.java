package com.example.app_gestion_presences.controller;

import com.example.app_gestion_presences.entity.Role;
import com.example.app_gestion_presences.repository.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AdminController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/admin/accueil")
    public String accueil(@AuthenticationPrincipal UserDetails ud, Model model) {
        model.addAttribute("email", ud.getUsername());
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
}
