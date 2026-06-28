# Présence Numérique — Feuilles d'émargement dématérialisées

Application web permettant aux enseignants de réaliser l'appel en temps réel via un trombinoscope interactif, avec validation de présence des étudiants par géolocalisation.

**Projet M1 MIAGE** — Binôme : Massyl (authentification & sécurité) + Rémi (fonctionnalités métier & temps réel)

---

## Prérequis

| Outil | Version minimale |
|-------|-----------------|
| Java (JDK) | 17 |
| Maven | 3.9+ (wrapper inclus) |
| PostgreSQL | 14+ |
| Navigateur | Chrome / Firefox (géolocalisation requise) |

---

## Configuration de la base de données

### 1. Créer la base PostgreSQL

```sql
CREATE DATABASE attendance;
CREATE USER admin WITH PASSWORD 'admin';
GRANT ALL PRIVILEGES ON DATABASE attendance TO admin;
```

### 2. Vérifier `application.properties`

```
src/main/resources/application.properties
```

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/attendance
spring.datasource.username=admin
spring.datasource.password=admin
spring.jpa.hibernate.ddl-auto=update
server.port=8082
```

Adaptez les identifiants si votre PostgreSQL local est différent.

---

## Lancement

```bash
cd app_gestion_presences

# Linux / macOS
./mvnw spring-boot:run

# Windows
.\mvnw.cmd spring-boot:run
```

Au premier démarrage, Hibernate crée les tables automatiquement et le `DataInitializer` injecte les données de démonstration.

Accès : **http://localhost:8082**

---

## Comptes de test

| Rôle | Email | Mot de passe |
|------|-------|--------------|
| Administrateur | admin@test.com | admin123 |
| Enseignant | cd@test.com | teacher123 |
| Enseignant | sl@test.com | teacher123 |
| Secrétariat | ab@test.com | secretary123 |
| Étudiant (M1 Gr.1) | alice@test.com | password123 |
| Étudiant (M1 Gr.2) | farid@test.com | password123 |
| Étudiant (M2 Gr.1) | clara@test.com | password123 |
| Étudiant (M2 Gr.2) | hassan@test.com | password123 |

> Tous les étudiants ont le mot de passe `password123`.
> Chaque utilisateur peut changer son mot de passe depuis **Mon compte**.

---

## Données de démonstration (injectées au démarrage)

- **2 promotions** : M1 MIAGE, M2 MIAGE
- **4 groupes** : Groupe 1 et Groupe 2 pour chaque promotion
- **20 étudiants** répartis dans les groupes
- **2 enseignants** : Charles Dupont, Sophie Lambert
- **4 séances clôturées** avec présences variées (Présent / Retard / Absent)

---

## Scénario complet

### Enseignant

1. Connexion sur `http://localhost:8082` → `cd@test.com / teacher123`
2. **Faire l'appel** → sélectionner promotion, groupe, seuil de retard, titre
3. **Démarrer** → le navigateur demande la géolocalisation
4. Le trombinoscope s'affiche — les statuts se mettent à jour en temps réel (WebSocket)
5. **Clôturer** la séance
6. **Historique** → **Voir détail** → **Télécharger Excel**

### Étudiant

1. Connexion → `alice@test.com / password123`
2. Si un appel est en cours pour son groupe : bouton **Valider ma présence**
3. Le navigateur demande la géolocalisation
4. Le serveur vérifie la proximité (≤ 50 m) et calcule un éventuel retard
5. Le statut apparaît immédiatement sur le trombinoscope de l'enseignant

### Secrétariat

- Gérer promotions, groupes, étudiants, enseignants
- Consulter et exporter les feuilles de présence en **Excel (.xlsx)**

---

## Architecture

```
app_gestion_presences/
├── src/main/java/com/example/app_gestion_presences/
│   ├── config/
│   │   ├── SecurityConfig.java        # Spring Security, formLogin, rôles
│   │   └── WebSocketConfig.java       # STOMP sur /ws
│   ├── controller/
│   │   ├── EnseignantController.java  # /enseignant/**
│   │   ├── EtudiantController.java    # /etudiant/**
│   │   ├── SecretariatController.java # /secretariat/**
│   │   ├── AdminController.java       # /admin/**
│   │   └── CompteController.java      # /compte (tous rôles)
│   ├── entity/
│   │   ├── User.java + Role.java
│   │   ├── Promotion.java + Group.java
│   │   ├── Event.java
│   │   └── Attendance.java + AttendanceStatus.java
│   ├── service/
│   │   ├── AttendanceService.java     # Haversine + check-in
│   │   ├── AttendancePushService.java # Push WebSocket
│   │   └── ExcelExportService.java    # Export .xlsx (Apache POI)
│   └── security/
│       ├── JwtUtil.java + JwtAuthFilter.java
│       └── CustomUserDetailsService.java
└── src/main/resources/
    ├── application.properties
    └── templates/                     # Thymeleaf
        ├── login.html + compte.html
        ├── enseignant/
        ├── etudiant/
        ├── secretariat/
        └── admin/
```

---

## Fonctionnalités

| Fonctionnalité | Détail |
|----------------|--------|
| Authentification | Spring Security — session HTTP, BCrypt |
| Autorisation | 4 rôles : ADMIN, ENSEIGNANT, ETUDIANT, SECRETARIAT |
| Trombinoscope temps réel | WebSocket STOMP — `/topic/event/{id}` |
| Géolocalisation | Haversine — seuil 50 m configurable |
| Jeton temporaire | UUID invalidé à la clôture |
| Retard configurable | 5 / 10 / 15 / 20 min (choisi par l'enseignant) |
| Export | Excel (.xlsx) stylé — couleurs par statut + résumé |
| Anti-doublon | Vérification serveur avant chaque check-in |
| RGPD | Coordonnées GPS jamais stockées |

---

## Stack technique

| Couche | Technologie |
|--------|-------------|
| Backend | Spring Boot 4.x — Java 17 |
| Sécurité | Spring Security 6 + JWT (jjwt) |
| ORM | Spring Data JPA / Hibernate |
| Base de données | PostgreSQL |
| Temps réel | Spring WebSocket + STOMP (SockJS / StompJS) |
| Frontend | Thymeleaf + CSS |
| Export | Apache POI 5.3.0 |
| Build | Maven (wrapper inclus) |
