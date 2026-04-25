# Tasks: Bouton Retour Paramètres → Discussion

**Feature**: Bouton Retour Paramètres → Discussion  
**Branch**: `001-appearance-settings` | **Date**: 2026-04-25  
**Spec**: [spec.md](spec.md) | **Plan**: [plan.md](plan.md)

---

## Phase 1: Setup

Initialisation du contexte de développement pour la feature.

- [ ] T001 Vérifier que le module opencode-gui compile sans erreur (`mvn clean compile -pl opencode-gui -am`)
- [ ] T002 Confirmer l'existence des fichiers sources : `opencode-gui/src/main/resources/fxml/settings_view.fxml`, `opencode-gui/src/main/java/ai/opencode/gui/SettingsController.java`, `opencode-gui/src/main/java/ai/opencode/gui/MainController.java`

---

## Phase 2: Foundational

Prérequis techniques bloquants avant toute implémentation utilisateur. Ces tâches doivent être complétées avant US1.

- [ ] T003 Lire et annoter les lignes clés dans `MainController.java` : méthode `toggleSettings()` (lignes 673-698), méthode `loadSettingsView()` (lignes 700-750), injection ConfigManager via factory
- [ ] T011 [P] Identifier exactement le style CSS du bouton `.settings-toggle-btn` dans `style.css` ou inline FXML — extraire toutes les propriétés `-fx-*` utilisées
- [ ] T012 [P] Lancer l'application en mode debug pour confirmer que `settingsToggleButton.setText("←")` s'exécute bien quand on ouvre les paramètres (`LOGGER.info` ligne 687)

**Dépendances**: Aucune — ces tâches sont indépendantes mais doivent précéder toutes les user stories.

---

## Phase 3: User Story 1 — Retour depuis accès direct aux paramètres

**Priorité**: P0  
**Description**: L'utilisateur clique sur Settings, voit le bouton retour, clique dessus, revient à la discussion sans perdre le contexte.

**Critère d'indépendance**: Le bouton retour est visible et fonctionnel dès l'ouverture des paramètres, avant toute navigation profonde. On peut tester US1 sans avoir ouvert de sous-onglet spécifique.

### Implémentation

- [ ] T014 [US1] Modifier `MainController.java` : ajouter un paramètre `MainController self` au constructeur ou utiliser `this` via la factory existante dans `loadSettingsView()` (ligne 712-724). Ajouter `if (controllerClass == SettingsController.class)` → `return new SettingsController(configManager, scene, this);`
- [ ] T015 [US1] Modifier `SettingsController.java` :
  - Ajouter champ `private final MainController mainController;` en haut du controller
  - Modifier le constructeur `public SettingsController(ConfigManager configManager, Scene scene)` pour accepter un troisième paramètre `MainController mainController`
  - Assigner `this.mainController = mainController;` dans le constructor
- [ ] T016 [US1] Ajouter méthode `@FXML public void returnToChat()` dans `SettingsController.java` qui appelle simplement `mainController.toggleSettings();` + ajoute un `LOGGER.info("Retour vers chat depuis settings")`
- [ ] T017 [US1] Modifier `settings_view.fxml` : ajouter un bloc `<HBox>` avec bouton retour en haut de la zone de contenu principal. Ce bloc doit être inséré AVANT les panneaux de configuration (`panelLLMPreference`, etc.) mais APRÈS le conteneur sidebar. Ajouter un `<Tooltip text="Retour"/>` au bouton via JavaFX Tooltip API (ou attribut FXML tooltip). Le bouton utilise l'icône flèche retour arrière (←, fichier defaire.jpg si SVG disponible, sinon caractère Unicode ←). Le bouton doit appeler `onAction="#returnToChat"`. Style inline inspiré de `.settings-toggle-btn` : `-fx-background-color: transparent; -fx-text-fill: #888; -fx-font-size: 20; -fx-cursor: hand; -fx-padding: 4 8;`
- [ ] T018 [US1] Compiler `mvn clean compile -pl opencode-gui -am` pour vérifier qu'il n'y a pas d'erreur de compilation liée aux modifications FXML/Java

### Test US1

- [ ] T019 [US1] Lancer l'application, cliquer sur ⚙ Settings → vérifier que le bouton ← apparaît en haut à gauche du panneau paramètres
- [ ] T020 [US1] Cliquer sur le bouton ← → vérifier que la vue chat réapparaît instantanément (pas de blanc, pas de lag)
- [ ] T021 [US1] Vérifier que le bouton ⚙ redevient visible en bas à droite après le retour
- [ ] T022 [US1] Vérifier que les messages de conversation sont toujours visibles (contexte préservé)

---

## Phase 4: User Story 2 — Retour depuis navigation profonde dans les paramètres

**Priorité**: P1  
**Description**: L'utilisateur navigue dans un onglet spécifique des paramètres (LLM Preference, Chat History, Apparence), clique sur le bouton retour et revient à la vue précédente ou par défaut.

**Critère d'indépendance**: Le bouton retour fonctionne depuis n'importe quel sous-panneau de configuration. On peut tester US2 indépendamment si on a déjà implémenté US1 + un panneau de paramètres ouvert.

### Implémentation

- [ ] T023 [US2] Vérifier que le bouton retour ajouté dans `settings_view.fxml` est bien présent dans tous les panneaux : il doit être un enfant direct du VBox content area principal, donc visible quelque soit quel sous-panneau (`panelLLMPreference`, `panelHistoriqueChats`, `panelDefaultPrompt`, `panelInterface`, `panelAgentSkills`, `panelVoixParole`) est actif
- [ ] T024 [US2] Tester que cliquer sur le bouton retour depuis chaque panneau spécifique ramène bien vers la vue chat principale (le comportement actuel de `toggleSettings()` ferme settingsViewContainer et réaffiche chatView)
- [ ] T025 [US2] Si nécessaire : ajouter un spacer `<Region HBox.hgrow="ALWAYS"/>` après le bouton retour pour pousser visuellement vers la gauche et conserver l'espace blanc à droite

### Test US2

- [ ] T026 [US2] Ouvrir "Préférence LLM" → cliquer ← → vérifier retour chat
- [ ] T027 [US2] Ouvrir "Apparence" → cliquer ← → vérifier retour chat  
- [ ] T028 [US2] Ouvrir "Compétences de l'agent" → cliquer ← → vérifier retour chat

---

## Phase 5: Polish & Cross-Cutting Concerns

Améliorations transversales, accessibilité, thème, performance.

- [ ] T029 Thème sombre : vérifier que le bouton retour apparaît en gris clair (#cccccc ou #888) sur fond sombre (#1a1a1a). Tester avec le themeSelector "Sombre".
- [ ] T030 Thème clair : vérifier que le bouton retour apparaît en texte foncé (#444444) sur fond clair (#e8e8e8). Tester avec le themeSelector "Clair".
- [ ] T031 Accessibilité clavier : vérifier que Tab permet d'atteindre le bouton ← puis Enter/Space déclenche le retour (comportement JavaFX standard pour Button)
- [ ] T032 Performance : chronométrer le temps entre clic sur ← et apparition de la vue chat — doit être < 200ms (JavaFX setVisible natif devrait suffire)
- [ ] T033 Cohérence visuelle : comparer le bouton ← ajouté avec le bouton ⚙ existant — mêmes padding, même border-radius, même font-size, seule l'icône diffère (← vs ⚙). Vérifier aussi que le tooltip "Retour" apparaît au survol de la souris et disparaît quand le curseur quitte le bouton.
- [ ] T034 Tester le bouton retour pendant qu'une animation est en cours (ex: sous-menu accordion qui se plie) — ne pas bloquer ni interrompre l'animation
- [ ] T035 Compiler définitivement `mvn clean compile -pl opencode-gui -am` et confirmer zéro warning

---

## Dépendances entre User Stories

```
Phase 1 (Setup) → Phase 2 (Foundational) → Phase 3 (US1) → Phase 4 (US2) → Phase 5 (Polish)
                                                       ↑
                                            US2 dépend US1 
                                            (le bouton doit exister avant test navigation profonde)
```

**US1 peut être testée indépendamment** : le bouton retour fonctionne dès son ajout dans settings_view.fxml.  
**US2 dépend US1** : nécessite que le bouton soit déjà implémenté et fonctionnel depuis un panneau de paramètres.

## Opportunités de parallélisation

Les tâches suivantes peuvent s'exécuter en parallèle (fichiers différents, aucune dépendance croisée) :

- **T011 + T012** : Recherche style CSS et debug toggleSettings (lecture seule, fichiers différents)
- **T014 + T017** : Modification MainController.java et modification settings_view.fxml (fichiers distincts) — possible après T003/T011/T012 complétés

## Stratégie d'implémentation recommandée (MVP)

1. **MVP = US1 uniquement** (T001-T022) : bouton retour visible et fonctionnel depuis l'écran de paramètres principal
2. **Enrichissement = US2** (T023-T028) : vérification que le bouton fonctionne aussi depuis les sous-panneaux spécifiques
3. **Polish = Phase 5** (T029-T035) : thème, accessibilité, performance, cohérence visuelle

Le MVP peut être compilé et testé après T022. L'enrichissement US2 ajoute la robustesse transversale. Le polish final affine tous les détails.
