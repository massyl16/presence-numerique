package com.example.app_gestion_presences;

public record AttendanceUpdate(
        Long eventId,
        Long userId,
        String status
) {}
