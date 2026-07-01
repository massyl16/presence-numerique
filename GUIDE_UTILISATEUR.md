# Guide Utilisateur — Présence Numérique

Application de feuilles d'émargement dématérialisées — M1 MIAGE

---

## Connexion

Accédez à l'application sur **http://localhost:8082**

Saisissez votre **email** et votre **mot de passe**, puis cliquez **Se connecter**.  
Vous êtes automatiquement redirigé vers votre espace selon votre rôle.

| Rôle | Email de test | Mot de passe |
|------|--------------|--------------|
| Enseignant | cd@test.com | teacher123 |
| Étudiant | alice@test.com | password123 |
| Secrétariat | ab@test.com | secretary123 |
| Administrateur | admin@test.com | admin123 |

---

## Espace Enseignant

### Faire l'appel

1. Cliquez **Faire l'appel** dans le menu
2. Sélectionnez une **promotion** (le filtre de groupes se met à jour automatiquement)
3. Sélectionnez un **groupe** (ou laissez vide pour toute la promotion)
4. Choisissez le **seuil de retard** : 5, 10, 15 ou 20 minutes
5. Saisissez un **titre** optionnel pour la séance
6. Cliquez **Démarrer l'appel**
   - Le navigateur demande votre géolocalisation → **Autoriser**
   - Si le GPS est indisponible, cliquez **Continuer sans GPS** (la vérification de proximité sera désactivée)

### Trombinoscope en temps réel

Une fois l'appel démarré, le trombinoscope s'affiche avec tous les étudiants du groupe.

- **Gris** — en attente
- **Vert** — présent
- **Orange** — retard
- **Rouge** — absent

Les statuts se mettent à jour **sans rechargement de page** dès que les étudiants valident leur présence.

### Clôturer la séance

Cliquez **Clôturer la séance** quand vous avez terminé.  
La séance est archivée et le jeton de validation est invalidé — plus aucune présence ne peut être enregistrée.

### Historique

Cliquez **Historique** pour consulter toutes vos séances passées.

- Filtrez par promotion
- Consultez le détail d'une séance (liste nominative, taux de présence)
- Téléchargez la feuille en **Excel** (.xlsx) avec un clic sur **⬇ Excel**

---

## Espace Étudiant

### Valider sa présence

1. Connectez-vous et accédez à l'**Accueil**
2. Si un appel est en cours pour votre groupe, le bouton **Je suis présent** apparaît automatiquement (sans avoir à rafraîchir la page)
3. Cliquez le bouton
   - Le navigateur demande votre géolocalisation → **Autoriser**
   - Le serveur vérifie que vous êtes à moins de **150 mètres** de l'enseignant
   - Si votre GPS manque de précision (> 300 m d'incertitude), un message vous demande de vous déplacer en espace ouvert
4. Un message de confirmation s'affiche — votre statut apparaît immédiatement sur le trombinoscope de l'enseignant

> **Vos coordonnées GPS ne sont jamais enregistrées.** Seul votre statut (Présent / Retard / Absent) et l'heure de validation sont conservés.

### Mes présences

Cliquez **Mes présences** pour consulter l'historique de toutes vos séances avec le statut et l'heure de chaque validation.

---

## Espace Secrétariat

### Gestion des promotions et groupes

Menu **Promotions** :
- Créez une **nouvelle promotion** (ex : "M1 MIAGE 2026")
- Créez un **nouveau groupe** en le rattachant à une promotion (ex : "Groupe 1")
- Supprimez un groupe existant

### Gestion des étudiants

Menu **Étudiants** :
- Ajoutez un étudiant (prénom, nom, email, promotion, groupe)
  - Le mot de passe par défaut est `password123`
- Supprimez un étudiant (ses présences sont supprimées en cascade)

### Gestion des enseignants

Menu **Enseignants** :
- Ajoutez un enseignant (prénom, nom, email)
  - Le mot de passe par défaut est `teacher123`
- Supprimez un enseignant

### Feuilles de présence

Menu **Feuilles de présence** :
1. Sélectionnez une promotion dans le menu déroulant
2. La liste de toutes les séances clôturées s'affiche
3. Cliquez **Voir →** pour consulter le détail d'une séance
4. Cliquez **⬇ Excel** pour télécharger la feuille formatée

Le fichier Excel contient :
- En-tête avec le nom de la séance, la promotion, la date et l'enseignant
- Tableau nominatif coloré (vert = présent, orange = retard, rouge = absent)
- Récapitulatif statistique (taux de présence, nombre de présents/retards/absents)

---

## Espace Administrateur

### Vue d'ensemble

L'accueil admin affiche le nombre total d'étudiants, d'enseignants et de comptes secrétariat.

### Gestion des comptes

Le tableau liste tous les comptes du système avec leur rôle.

- **Reset MDP** — réinitialise le mot de passe au mot de passe par défaut du rôle
- **Supprimer** — supprime définitivement le compte (confirmation requise)

> L'administrateur ne peut pas supprimer son propre compte.

---

## Mon compte (tous les rôles)

Cliquez **Mon compte** dans le menu de gauche pour :
- Consulter vos informations (nom, email, promotion, groupe)
- **Changer votre mot de passe** (l'ancien mot de passe est requis, minimum 6 caractères)

---

## Règles métier

| Règle | Détail |
|-------|--------|
| Proximité GPS | L'étudiant doit être à moins de **150 mètres** de l'enseignant |
| Seuil de retard | Configurable par séance : 5, 10, 15 ou 20 min après le démarrage |
| Jeton de séance | Invalidé à la clôture — impossible de valider une présence après |
| Anti-doublon | Impossible de valider deux fois la même séance |
| RGPD | Les coordonnées GPS des étudiants ne sont jamais stockées |
| Précision GPS | Refusé si la précision GPS est supérieure à 300 m |
