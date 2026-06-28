package com.example.app_gestion_presences.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() { return "redirect:/login"; }

    @GetMapping("/login")
    public String login() { return "login"; }

    /* ── Anciennes routes prototypées → nouvelles routes ── */
    @RequestMapping("/speaker/**")
    public String oldSpeaker() { return "redirect:/enseignant/accueil"; }

    @RequestMapping("/participant/**")
    public String oldParticipant() { return "redirect:/etudiant/accueil"; }

    @RequestMapping("/secretary/**")
    public String oldSecretary() { return "redirect:/secretariat/accueil"; }
}
