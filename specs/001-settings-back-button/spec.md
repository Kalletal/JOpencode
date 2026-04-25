# Spécification : Bouton Retour Paramètres → Discussion

**Date**: 2026-04-25  
**Feature Directory**: `specs/001-settings-back-button`

## Description Utilisateur

Ajouter un bouton dans l'interface de paramétrage permettant de revenir à l'interface de discussion principale. Ce bouton doit être positionné symétriquement au bouton Settings actuel (dans la fenêtre de discussion), mais en miroir dans les paramètres. Même style, mêmes proportions, icône adaptée (flèche retour arrière).

## Acteurs

| Rôle | Description |
|------|-------------|
| **Utilisateur** | Toute personne utilisant l'application Opencode, naviguant entre l'interface de discussion et celle des paramètres |

## User Scenarios & Testing

### Scenario 1 : Accès aux paramètres depuis la discussion (primaire)
**Actor:** Utilisateur  
**Précondition:** L'utilisateur est dans l'interface de discussion principale  
**Flux primaire:**
1. L'utilisateur clique sur le bouton "Settings" (engrenage) dans la fenêtre de discussion
2. La vue bascule vers l'interface de paramétrage
3. Un bouton visible "Retour" apparaît en haut à gauche du panneau de paramètres
4. L'utilisateur clique sur ce bouton "Retour"
5. L'interface revient instantanément à l'interface de discussion principale
6. Le contexte de conversation actif est restauré tel quel

**Post-condition:** Vue discussion affichée, même session active

### Scenario 2 : Navigation profonde dans les paramètres puis retour
**Actor:** Utilisateur  
**Précondition:** L'utilisateur a ouvert plusieurs sous-onglets dans les paramètres (LLM, Admin, Apparence)  
**Flux primaire:**
1. L'utilisateur navigue dans un onglet de paramètres spécifique
2. Il clique sur le bouton "Retour" présent dans chaque panneau de paramètres
3. Il revient directement à la liste principale des paramètres ou à la vue par défaut des paramètres (selon état précédent)

**Post-condition:** Retour à la dernière vue de paramètres ouverte avant d'accéder aux détails

## Clarifications

### Session 2026-04-25

- Q: Quelle icône utiliser pour le bouton retour ? → A: Flèche retour arrière (définie dans defaire.jpg), identique à la direction ←
- Q: Comment indiquer la fonction du bouton sans texte visible ? → A: Tooltip "Retour" affiché au survol de la souris (hover)

## Functional Requirements

| ID | Requirement | Priorité |
|----|-------------|----------|
| FR-001 | Le bouton "Retour" doit être visible et cliquable dans l'interface de paramétrage | P0 |
| FR-002 | Le bouton utilise une icône SVG de flèche retour arrière (fichier defaire.jpg / ←) | P0 |
| FR-003 | Le bouton doit avoir le même style visuel que le bouton Settings actuel (dimensions, padding, border-radius, couleurs) mais avec position symétrique (aligné à gauche vs droite) | P0 |
| FR-004 | Au clic, le bouton ferme/restore la vue de discussion principale sans perdre l'état courant de la conversation | P0 |
| FR-005 | Le bouton affiche une icône de flèche retour arrière (←) et un tooltip "Retour" au survol de la souris | P1 |
| FR-006 | Le bouton doit rester visible sur tous les panneaux de sous-paramètres (LLM Preference, Chat History, Apparence, etc.) | P1 |
| FR-007 | Le bouton doit respecter le thème actif (sombre → fond #2d2d2d, clair → fond #e8e8e8) | P1 |

## Success Criteria

| Critère | Mesure |
|---------|--------|
| **Visibilité** | Le bouton est présent et visible dans 100% des vues de paramétrage |
| **Fonctionnalité retour** | 100% des clics sur le bouton ramènent à la vue discussion sans erreur |
| **Cohérence visuelle** | Le bouton correspond au style Settings button (largeur ≥ 40px, hauteur ≥ 35px, border-radius = 6px) |
| **Performance** | Transition paramètres → discussion en < 200ms |
| **Accessibilité** | Bouton accessible via clavier (Tab + Enter/Space) comme tous les boutons JavaFX |

## Key Entities

| Entité | Rôle |
|--------|------|
| **SettingsView** | Vue FXML contenant la sidebar et les panneaux de configuration |
| **MainChatView** | Vue principale de discussion (celle vers laquelle on retourne) |
| **BackButton** | Composant Button JavaFX ajouté au layout settings_view.fxml |

## Assumptions

- L'interface de discussion principale existe déjà et est gérée par un autre controller/fxml
- La navigation entre les deux vues se fait via visibilité des panes VBox/HBox existants
- Le bouton doit être positionné dans la zone supérieure gauche du panneau principal de paramètres (pas dans la sidebar)
- Aucune donnée n'a besoin d'être sauvegardée avant le retour (état volatile conservé nativement par JavaFX)

## Dependencies

- `settings_view.fxml` — fichier FXML à modifier pour ajouter le bouton
- `SettingsController.java` — méthode handler pour le clic sur le bouton retour
- CSS global (`style.css`) — styles existants du bouton Settings pour duplication

## Scope

**Inclus:**
- Ajout du bouton "Retour" dans l'UI de paramétrage
- Style identique au bouton Settings actuel
- Navigation fonctionnelle vers la vue discussion

**Exclus:**
- Sauvegarde automatique de l'état des paramètres modifiés
- Animation de transition entre les deux vues
- Bouton retour depuis les sous-vues profondes (ex: édition API key spécifique)
