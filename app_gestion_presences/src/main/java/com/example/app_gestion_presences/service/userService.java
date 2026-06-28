package com.example.app_gestion_presences.service;

import com.example.app_gestion_presences.entity.*;
import com.example.app_gestion_presences.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class userService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public userService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void createUserParticipant(String firstname, String lastname, String email,
                                       String password, Promotion promotion, Group group) {
        User user = new User(firstname, lastname, email, promotion, group);
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
    }

    public User createUserAdmin(String firstname, String lastname, String email,
                                String password, Role role) {
        User user = new User(firstname, lastname, email, role);
        user.setPassword(passwordEncoder.encode(password));
        return userRepository.save(user);
    }
}
