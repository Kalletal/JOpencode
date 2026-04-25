# Implementation Plan: Bouton Retour Paramètres → Discussion

**Branch**: `001-appearance-settings` | **Date**: 2026-04-25 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/001-settings-back-button/spec.md`

## Summary

Ajouter un bouton "Retour" dans l'interface de paramétrage (`settings_view.fxml`) qui permet à l'utilisateur de revenir à la vue de discussion principale. Le bouton sera positionné en haut à gauche du panneau principal de paramètres, symétriquement au bouton Settings actuel (aligné à droite). Il utilisera le même style CSS que le bouton `.settings-toggle-btn` existant avec une icône flèche retour. La navigation se fait via le mécanisme existant `MainController.toggleSettings()`.

## Technical Context

**Language/Version**: Java 21  
**Primary Dependencies**: JavaFX 21 (GUI framework), FXML (UI definition), Maven multi-module build  
**Storage**: SQLite (via opencode-storage module) — non concerné par cette feature  
**Testing**: JUnit 5 + Mockito (opencode-core tests) — pas de tests UI spécifiques pour ce change  
**Target Platform**: Desktop Linux/macOS/Windows (JavaFX cross-platform)  
**Project Type**: Desktop application GUI (JavaFX)  
**Performance Goals**: Transition entre vues < 200ms (déjà garanti par toggle visibility/invisible JavaFX natif)  
**Constraints**:  
- Le bouton doit respecter le thème actif (sombre/clair) via `ThemeManager.updateRegionBackground()` et les variables CSS `-fx-text-fill`  
- Doit survivre au rechargement dynamique de la vue settings dans `StackPane settingsViewContainer`  
- Style cohérent avec `.settings-toggle-btn` du fichier `style.css`  

**Scale/Scope**: 1 nouveau composant Button, 2 fichiers modifiés (`main_window.fxml` + `settings_view.fxml`), 2 contrôleurs touchés (`MainController`, `SettingsController`)

## Architecture Actuelle

L'application utilise une architecture single-window avec navigation par visibilité :

```
main_window.fxml (BorderPane)
├── center: StackPane → contient chatView ET settingsViewContainer (toggled via setVisible)
│   ├── chatView (VBox): interface de discussion principale
│   └── settingsViewContainer (StackPane): reçoit dynamiquement settings_view.fxml
│       └── settings_view.fxml (HBox)
│           ├── sidebar (VBox): menu accordion paramètres
│           └── content area (VBox): panneaux de configuration
└── right: VBox sidebar (projets, contextes, sessions)
```

Le bouton Settings actuel (`settingsToggleButton` dans `main_window.fxml` ligne 27) agit déjà comme bouton retour : quand il est en mode paramètres, son texte devient "←" et un clic sur ce bouton revient à la vue chat. La méthode `toggleSettings()` dans `MainController.java:673-698` gère cette bascule complète.

**Décision**: Ajouter un bouton supplémentaire dans `settings_view.fxml` qui appelle `MainController.toggleSettings()` via une référence passée au `SettingsController`. Le bouton sera positionné en haut du panneau de contenu principal (pas dans la sidebar).

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Critère | Statut | Notes |
|---------|--------|-------|
| Respecte le pattern FXML/Controller existant | ✅ Pass | Ajout d'un Button FXML + handler JavaFX standard |
| Utilise les mêmes techno que le reste du projet | ✅ Pass | JavaFX, FXML, CSS inline, ThemeManager |
| Ne casse pas la navigation existante | ✅ Pass | Le toggle settings existant reste inchangé |
| Respecte le thème sombre/clair | ✅ Pass | Style hérite des variables CSS globales |
| Pas de nouvelles dépendances | ✅ Pass | Tout est dans opencode-gui module actuel |

## Project Structure

### Documentation (this feature)

```text
specs/001-settings-back-button/
├── plan.md              # This file (/speckit.plan command output)
├── spec.md              # Feature specification
└── checklists/
    └── requirements.md  # Quality checklist
```

### Source Code (repository root) — fichiers modifiés

```
opencode-gui/src/main/resources/fxml/settings_view.fxml
  → Ligne ~30-45: ajout HBox avec bouton "Retour" ← en haut du content area principal
  
opencode-gui/src/main/java/ai/opencode/gui/MainController.java
  → Methode toggleSettings() ligne 673: inchangée (déjà fonctionnelle)
  
opencode-gui/src/main/java/ai/opencode/gui/SettingsController.java
  → Nouveau champ: MainController mainController (référence pour appel retour)
  → Nouvelle méthode @FXML returnToChat(): delegate à mainController.toggleSettings()
  → Constructor update: accepte MainController en paramètre optionnel
```

**Structure Decision**: Single-module change within `opencode-gui`. La navigation est gérée par le `MainController` qui est le parent de toutes les vues. Le `SettingsController` recevra une référence au `MainController` via la factory FXML existante (`loader.setControllerFactory`), permettant d'appeler `toggleSettings()` depuis les paramètres.

## Phase 0: Outline & Research

| Unknown | Resolution | Status |
|---------|------------|--------|
| Comment le SettingsController peut-il appeler MainController ? | Passage de référence via `loader.setControllerFactory` dans `loadSettingsView()` — pattern déjà utilisé pour ConfigManager et Scene | ✅ Résolu |
| Où positionner exactement le bouton ? | En haut du panneau de contenu principal (content area VBox), avant le titre du panneau actif, aligné à gauche via HBox avec spacer | ✅ Décidé |
| Quel style utiliser ? | Copie exacte du `.settings-toggle-btn` style.css : `-fx-background-color: transparent; -fx-text-fill: #888; font-size: 20; cursor: hand; border-radius: 4` | ✅ Décidé |
| Icône vs texte ? | Texte "←" (Unicode) comme le bouton settings actuel utilise "⚙" — cohérent avec l'existant | ✅ Décidé |

### Research Findings

**Decision**: Utiliser `MainController.toggleSettings()` existant plutôt que créer une nouvelle méthode  
**Rationale**: Cette méthode gère déjà la bascule complète (visibility + state + button text), évitant la duplication. Le bouton dans settings n'est qu'un trigger supplémentaire vers la même logique.  
**Alternatives considérées**: 
1. Créer `mainController.returnFromSettings()` séparée → plus propre mais code dupliqué  
2. Utiliser un événement custom JavaFX → surkill pour 2 vues  

**Decision**: Positionnement en haut du content area principal (pas dans sidebar)  
**Rationale**: La spec demande "à la même position que le bouton settings, mais en miroir". Le bouton settings est dans la zone de contenu chat (en haut à droite). Le retour doit être symétrique en haut à gauche du contenu paramètres.  
**Alternatives considérées**: 
1. Dans la sidebar (plus visible) → moins intuitif car pas au même endroit visuel  
2. En footer des paramètres → trop éloigné de l'action principale  

## Phase 1: Design & Contracts

### Data Model

Aucune entité de données nouvelle. Feature purement UI — aucun modèle, aucune table DB, aucune configuration persistante nécessaire.

### Interface Contracts

| Contract | Format | Description |
|----------|--------|-------------|
| **Bouton Retour** | FXML Button + CSS `.settings-toggle-btn` modifié | Appelle `MainController.toggleSettings()` via référence injectée |
| **Navigation** | Method call `toggleSettings()` | Toggle entre chatView et settingsViewContainer (déjà implémenté) |

### Quickstart

Pour tester cette feature :

1. Lancer l'application : `mvn javafx:run` depuis `opencode-gui/`
2. Cliquer sur le bouton ⚙ Settings en bas à droite de la vue chat
3. Un bouton "←" apparaît en haut à gauche du panneau de paramétrage
4. Cliquer sur ce bouton retourne immédiatement à la vue chat
5. Le bouton ⚙ redevient visible en bas à droite

Vérifications :
- [ ] Bouton visible avec icône ← sur fond transparent
- [ ] Texte du bouton blanc/gris selon thème actif
- [ ] Transition instantanée vers la discussion
- [ ] Conversation active préservée (messages non perdus)
- [ ] Bouton fonctionne depuis tous les panneaux de sous-paramètres

## Phase 2: Implementation Steps

### Étape 1 : Modifier `main_window.fxml`
- Ajouter un attribut `fx:id="backButton"` au bouton `settingsToggleButton` existant ou créer un nouveau Button dans settings_view_container

### Étape 2 : Passer référence MainController au SettingsController
Dans `MainController.loadSettingsView()`, modifier la factory pour injecter `this` comme MainController :
```java
if (controllerClass == SettingsController.class) {
    return new SettingsController(configManager, scene, this);
}
```

### Étape 3 : Modifier `SettingsController.java`
- Ajouter champ `private final MainController mainController;`
- Modifier le constructor pour accepter `MainController`  
- Ajouter méthode `@FXML public void returnToChat()` qui appelle `mainController.toggleSettings()`

### Étape 4 : Ajouter bouton dans `settings_view.fxml`
Ajouter en haut du content area principal (VBox des panneaux) :
```xml
<HBox alignment="CENTER_LEFT" spacing="10">
    <Button text="←" onAction="#returnToChat" 
            style="-fx-background-color: transparent; -fx-text-fill: #888; 
                   -fx-font-size: 20; -fx-cursor: hand;"/>
</HBox>
```

### Étape 5 : Compiler et tester
```bash
mvn clean compile -pl opencode-gui -am
# Puis lancer l'app et vérifier visuellement
```

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| Injection de dépendance entre controllers | Permet au SettingsController d'appeler toggleSettings() | Le pattern setControllerFactory existe déjà pour ConfigManager/Scene |
