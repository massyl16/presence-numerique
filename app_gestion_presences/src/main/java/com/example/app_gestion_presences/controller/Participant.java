package com.example.app_gestion_presences.controller;

public record Participant(

        Long user_id,
        String firstname,
        String lastname,
        String email,
        String photo,
        String promotion,
        String group,
        String status
) { }
