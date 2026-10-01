package com.example.app_gestion_presences.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Journalise les connexions réussies et échouées (événements publiés par Spring Security).
 * Le mot de passe saisi n'est jamais journalisé.
 */
@Component
public class AuthEventLogger {

    private static final Logger log = LoggerFactory.getLogger(AuthEventLogger.class);

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) {
        String roles = event.getAuthentication().getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .collect(Collectors.joining(","));
        log.info("CONNEXION_OK utilisateur={} role={}", event.getAuthentication().getName(), roles);
    }

    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent event) {
        log.warn("CONNEXION_ECHEC utilisateur={} motif={}",
                event.getAuthentication().getName(), event.getException().getClass().getSimpleName());
    }
}
