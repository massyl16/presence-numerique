package com.example.app_gestion_presences.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstname;
    private String lastname;
    private String email;
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;
    private String photo_path;

    @ManyToOne
    private Promotion promotion;

    @ManyToOne
    private Group group;

    public Long getId() { return id; }
    public String getFirstname() { return firstname; }
    public String getLastname() { return lastname; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public Role getRole() { return role; }
    public String getPhoto() { return photo_path; }
    public Promotion getPromotion() { return promotion; }
    public Group getGroup() { return group; }

    public void setFirstname(String firstname) { this.firstname = firstname; }
    public void setLastname(String lastname) { this.lastname = lastname; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(Role role) { this.role = role; }
    public void setPhoto(String photo_path) { this.photo_path = photo_path; }
    public void setPromotion(Promotion promotion) { this.promotion = promotion; }
    public void setGroup(Group group) { this.group = group; }

    @PrePersist
    public void initPhoto() {
        if (photo_path == null && email != null) {
            this.photo_path = "/uploads/photos/user_" + email + ".jpg";
        }
    }

    public User(String firstname, String lastname, String email, Promotion promotion, Group group) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.role = Role.ETUDIANT;
        this.photo_path = "/uploads/photos/user_" + email + ".jpg";
        this.promotion = promotion;
        this.group = group;
    }

    public User(String firstname, String lastname, String email, Role role) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.role = role;
    }

    public User() {}
}
