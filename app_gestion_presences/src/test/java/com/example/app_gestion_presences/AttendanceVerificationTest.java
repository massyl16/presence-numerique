package com.example.app_gestion_presences;

import com.example.app_gestion_presences.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires de la logique de validation de présence.
 * Couvre : Haversine (proximité GPS), calcul de retard, appel non démarré.
 * Aucun contexte Spring requis — tests purement POJO.
 */
class AttendanceVerificationTest {

    // Paris — point de référence (position enseignant)
    private static final double LAT_ENSEIGNANT = 48.8566;
    private static final double LON_ENSEIGNANT = 2.3522;

    // 30 m au nord (≤ 50 m → doit être accepté)
    private static final double LAT_PROCHE  = 48.8566 + (30.0 / 111_000.0);
    private static final double LON_PROCHE  = 2.3522;

    // 200 m au nord (> 50 m → doit être refusé)
    private static final double LAT_LOIN    = 48.8566 + (200.0 / 111_000.0);
    private static final double LON_LOIN    = 2.3522;

    private Event event;
    private Attendance attendance;

    @BeforeEach
    void setUp() {
        event = new Event();
        event.setLatitude(LAT_ENSEIGNANT);
        event.setLongitude(LON_ENSEIGNANT);
        event.setStarted(true);
        event.setToken("token-test-uuid");
        event.setLateThreshold(10);            // seuil : 10 minutes
        event.setStartTime(LocalDateTime.now().minusMinutes(3)); // démarré il y a 3 min

        User etudiant = new User("Alice", "Martin", "alice@test.com", Role.ETUDIANT);
        attendance = new Attendance(etudiant, event);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 1. Géolocalisation
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Étudiant à 30 m → validation acceptée")
    void testGeolocProche_presenceAcceptee() {
        String result = attendance.verification(LAT_PROCHE, LON_PROCHE);
        assertEquals("Ok", result, "Un étudiant à 30 m doit être accepté");
    }

    @Test
    @DisplayName("Étudiant à 200 m → validation refusée")
    void testGeolocLoin_presenceRefusee() {
        String result = attendance.verification(LAT_LOIN, LON_LOIN);
        assertNotEquals("Ok", result, "Un étudiant à 200 m doit être refusé");
        assertTrue(result.toLowerCase().contains("loin") || result.toLowerCase().contains("éloign"),
                "Le message doit mentionner la distance");
    }

    @Test
    @DisplayName("GPS désactivé (lat=0, lon=0) → proximité non vérifiée, présence acceptée")
    void testGpsDesactive_presenceAcceptee() {
        event.setLatitude(0.0);
        event.setLongitude(0.0);
        String result = attendance.verification(0.0, 0.0);
        assertEquals("Ok", result, "Sans GPS, la présence doit être validée sans vérification de distance");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. Calcul du retard
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Check-in 3 min après le début (seuil 10 min) → statut PRESENT")
    void testCheckinAvantSeuil_statutPresent() {
        // event démarré il y a 3 min, seuil 10 min → dans les temps
        event.setLatitude(0.0);
        event.setLongitude(0.0); // GPS désactivé pour isoler le test de retard

        String result = attendance.verification(0.0, 0.0);

        assertEquals("Ok", result);
        assertEquals(AttendanceStatus.Present, attendance.getStatus(),
                "Après 3 min avec un seuil de 10 min, l'étudiant doit être Présent");
        assertEquals(0, attendance.getLateMinutes());
    }

    @Test
    @DisplayName("Check-in 20 min après le début (seuil 10 min) → statut RETARD")
    void testCheckinApresSeuil_statutRetard() {
        event.setStartTime(LocalDateTime.now().minusMinutes(20));
        event.setLatitude(0.0);
        event.setLongitude(0.0);

        String result = attendance.verification(0.0, 0.0);

        assertEquals("Ok", result);
        assertEquals(AttendanceStatus.Late, attendance.getStatus(),
                "Après 20 min avec un seuil de 10 min, l'étudiant doit être en Retard");
        assertTrue(attendance.getLateMinutes() >= 15,
                "Le retard doit être d'au moins 15 minutes (entre 20 et 21 min)");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. Appel non actif
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Appel non démarré → validation refusée")
    void testAppelNonDemarre_refus() {
        event.setStarted(false);
        event.setToken(null);

        String result = attendance.verification(LAT_PROCHE, LON_PROCHE);

        assertNotEquals("Ok", result, "Sans appel actif, la validation doit être refusée");
        assertTrue(result.toLowerCase().contains("actif") || result.toLowerCase().contains("appel"),
                "Le message doit indiquer que l'appel n'est pas actif");
    }

    @Test
    @DisplayName("Token révoqué (séance clôturée) → validation refusée")
    void testTokenRevoque_refus() {
        event.setStarted(false);
        event.setToken(null); // token invalidé à la clôture

        String result = attendance.verification(LAT_PROCHE, LON_PROCHE);

        assertNotEquals("Ok", result);
    }
}
