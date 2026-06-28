# Schéma Entité-Relation — Présence Numérique

## Diagramme

```
┌─────────────────────┐         ┌─────────────────────┐
│      PROMOTION       │         │        GROUP         │
├─────────────────────┤         ├─────────────────────┤
│ PK  id       BIGINT │◄────────│ PK  id       BIGINT  │
│     name     VARCHAR│  1   N  │     name     VARCHAR  │
└─────────────────────┘         │ FK  promotion_id      │
          ▲                     └──────────┬────────────┘
          │ N                              │ N
          │                               │
          │     ┌─────────────────────────┘
          │     │
┌─────────┴─────▼────────────────┐
│               USER              │
├─────────────────────────────────┤
│ PK  id          BIGINT          │
│     firstname   VARCHAR         │
│     lastname    VARCHAR         │
│     email       VARCHAR UNIQUE  │
│     password    VARCHAR (BCrypt)│
│     role        ENUM            │  ← ADMIN | ENSEIGNANT
│     photo_path  VARCHAR (null)  │     ETUDIANT | SECRETARIAT
│ FK  promotion_id BIGINT (null)  │
│ FK  group_id     BIGINT (null)  │
└────────────┬────────────────────┘
             │                  │
             │ 1 (enseignant)   │ 1 (étudiant)
             │                  │
┌────────────▼────────────────┐  │
│            EVENT            │  │
├─────────────────────────────┤  │
│ PK  id             BIGINT   │  │
│     title          VARCHAR  │  │
│ FK  enseignant_id  BIGINT   │  │
│ FK  promotion_id   BIGINT   │  │
│ FK  group_id       BIGINT   │  │
│     start_time     DATETIME │  │
│     late_threshold INT      │  │
│     latitude       DOUBLE   │  │  ← Position de l'enseignant
│     longitude      DOUBLE   │  │    (jamais celle de l'étudiant)
│     started        BOOLEAN  │  │
│     token          VARCHAR  │  │  ← UUID temporaire, null si clôturé
└─────────────┬───────────────┘  │
              │ 1                │ 1
              │ N                │ N
              └────────┬─────────┘
                       │
              ┌────────▼────────────┐
              │      ATTENDANCE      │
              ├─────────────────────┤
              │ PK  id        BIGINT │
              │ FK  user_id   BIGINT │  ← étudiant
              │ FK  event_id  BIGINT │
              │     status    ENUM   │  ← Present | Late | Absent
              │     validation_time  │
              │              DATETIME│
              │     late_minutes INT │
              └─────────────────────┘
```

---

## Description des entités

### PROMOTION
| Colonne | Type | Contrainte | Description |
|---------|------|-----------|-------------|
| id | BIGINT | PK, AUTO | Identifiant |
| name | VARCHAR | NOT NULL | Ex : "M1 MIAGE" |

### GROUP
| Colonne | Type | Contrainte | Description |
|---------|------|-----------|-------------|
| id | BIGINT | PK, AUTO | Identifiant |
| name | VARCHAR | NOT NULL | Ex : "Groupe 1" |
| promotion_id | BIGINT | FK → PROMOTION | Promotion parente |

### USER
| Colonne | Type | Contrainte | Description |
|---------|------|-----------|-------------|
| id | BIGINT | PK, AUTO | Identifiant |
| firstname | VARCHAR | NOT NULL | Prénom |
| lastname | VARCHAR | NOT NULL | Nom |
| email | VARCHAR | UNIQUE, NOT NULL | Identifiant de connexion |
| password | VARCHAR | NOT NULL | Haché BCrypt |
| role | ENUM | NOT NULL | ADMIN / ENSEIGNANT / ETUDIANT / SECRETARIAT |
| photo_path | VARCHAR | NULL | Chemin photo de profil |
| promotion_id | BIGINT | FK → PROMOTION, NULL | Null si enseignant/admin |
| group_id | BIGINT | FK → GROUP, NULL | Null si enseignant/admin |

### EVENT (séance d'appel)
| Colonne | Type | Contrainte | Description |
|---------|------|-----------|-------------|
| id | BIGINT | PK, AUTO | Identifiant |
| title | VARCHAR | NULL | Titre de la séance |
| enseignant_id | BIGINT | FK → USER | Enseignant qui a lancé l'appel |
| promotion_id | BIGINT | FK → PROMOTION | Promotion ciblée |
| group_id | BIGINT | FK → GROUP, NULL | Groupe ciblé (null = tous) |
| start_time | DATETIME | NULL | Heure de démarrage |
| late_threshold | INT | NOT NULL | Seuil de retard en minutes |
| latitude | DOUBLE | NULL | Position GPS enseignant |
| longitude | DOUBLE | NULL | Position GPS enseignant |
| started | BOOLEAN | NOT NULL | Appel en cours |
| token | VARCHAR | NULL | UUID invalidé à la clôture |

### ATTENDANCE (présence)
| Colonne | Type | Contrainte | Description |
|---------|------|-----------|-------------|
| id | BIGINT | PK, AUTO | Identifiant |
| user_id | BIGINT | FK → USER | Étudiant |
| event_id | BIGINT | FK → EVENT | Séance |
| status | ENUM | NOT NULL | Present / Late / Absent |
| validation_time | DATETIME | NULL | Heure du check-in |
| late_minutes | INT | NOT NULL | 0 si présent, >0 si retard |

> **RGPD** : les coordonnées GPS de l'étudiant ne sont jamais persistées.
> Seul le statut, l'heure de validation et les minutes de retard sont stockés.

---

## Relations

| Relation | Cardinalité | Description |
|----------|-------------|-------------|
| PROMOTION → GROUP | 1-N | Une promotion contient plusieurs groupes |
| PROMOTION → USER | 1-N | Un étudiant appartient à une promotion |
| GROUP → USER | 1-N | Un étudiant appartient à un groupe |
| USER (enseignant) → EVENT | 1-N | Un enseignant crée plusieurs séances |
| PROMOTION → EVENT | 1-N | Une séance cible une promotion |
| GROUP → EVENT | 1-N | Une séance peut cibler un groupe spécifique |
| USER (étudiant) → ATTENDANCE | 1-N | Un étudiant a plusieurs enregistrements de présence |
| EVENT → ATTENDANCE | 1-N | Une séance génère plusieurs enregistrements |
