package com.example.app_gestion_presences.controller;

import com.example.app_gestion_presences.entity.User;
import com.example.app_gestion_presences.repository.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CompteController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public CompteController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/compte")
    public String compte(@AuthenticationPrincipal UserDetails ud, Model model) {
        User user = userRepository.findByEmail(ud.getUsername()).orElseThrow();
        model.addAttribute("user", user);
        return "compte";
    }

    @PostMapping("/compte/change-password")
    public String changePassword(@AuthenticationPrincipal UserDetails ud,
                                  @RequestParam String currentPassword,
                                  @RequestParam String newPassword,
                                  @RequestParam String confirmPassword,
                                  RedirectAttributes ra) {
        User user = userRepository.findByEmail(ud.getUsername()).orElseThrow();

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            ra.addFlashAttribute("error", "Mot de passe actuel incorrect.");
            return "redirect:/compte";
        }
        if (newPassword.length() < 6) {
            ra.addFlashAttribute("error", "Le nouveau mot de passe doit faire au moins 6 caractères.");
            return "redirect:/compte";
        }
        if (!newPassword.equals(confirmPassword)) {
            ra.addFlashAttribute("error", "Les deux nouveaux mots de passe ne correspondent pas.");
            return "redirect:/compte";
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        ra.addFlashAttribute("success", "Mot de passe modifié avec succès.");
        return "redirect:/compte";
    }
}
