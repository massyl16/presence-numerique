package com.example.app_gestion_presences.service;


import com.example.app_gestion_presences.entity.user;
import com.example.app_gestion_presences.entity.Role;
import com.example.app_gestion_presences.repository.userRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
@Service
public class userService {

    private final userRepository user_repository;

    public userService(userRepository user_repository) {
        this.user_repository = user_repository;
    }

    public void createUserParticipant(String firstname, String lastname, String email, MultipartFile photo) {

          user user = new user(firstname, lastname, email);
          //upload_photo(user, photo);
          user_repository.save(user);
    }

    public user createUserAdmin(String firstname, String lastname, String email, Role role) {

        user user = new user(firstname, lastname, email, role);

        return user_repository.save(user);
    }

    public void upload_photo(user user, MultipartFile photo){
        String fileName = "user_" + user.getEmail() + ".jpg";

        Path target = Paths.get("/uploads/photos/" + fileName);

        try {
            photo.transferTo(target);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
