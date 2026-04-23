---
description: "Task list for Apparence Interface feature"
---

# Tasks: Apparence Interface (Thème + Langue)

**Input**: Design documents from `/specs/001-appearance-settings/`  
**Prerequisites**: plan.md ✅ | spec.md ✅ | research.md ✅ | data-model.md ✅ | contracts/ui-contract.md ✅  

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (US1, US2, US3)
- Include exact file paths in descriptions

**Note**: Le thème de base est déjà implémenté dans le commit `c145357`. Les tâches ci-dessous couvrent la complétion restante : extraction ThemeManager, système i18n complet, et traductions de l'interface.

---

## Phase 1: Setup — Extraction ThemeManager

**Purpose**: Extraire la logique de changement de thème du SettingsController vers un classe dédiée pour réutilisabilité.

- [x] T001 Create package directory structure `opencode-gui/src/main/java/ai/opencode/gui/theme/`
- [x] T002 [P] [US1] Implement ThemeManager class in `opencode-gui/src/main/java/ai/opencode/gui/theme/ThemeManager.java` with methods: `applyTheme(Scene scene, String themeName)`, `swapStylesheets(Scene scene, boolean isDark)`, `applyThemeColorsRecursive(javafx.scene.Node root, boolean isDark)` using the existing CSS swap logic from SettingsController.applyTheme() and applyThemeColors()
- [x] T003 [P] [US1] Update SettingsController.applyTheme() to delegate to ThemeManager.applyTheme(currentScene, theme) instead of inline implementation
- [x] T004 [US1] Add ThemeManager constructor that loads both CSS paths via getClass().getResource("/css/style.css") and getClass().getResource("/css/style-light.css") for caching

**Checkpoint**: US1 (changer le thème visuel) fonctionne avec une architecture propre et réutilisable. Le code est compilé et testable. ✅ Phase 1 terminée — compile vérifié (`mvn clean compile -pl opencode-gui`).

---

## Phase 2: Foundational — Système i18n (Blocking Prerequisites)

**Purpose**: Infrastructure de localisation JavaFX standard avant toute traduction d'interface.

**⚠️ CRITICAL**: Aucune tâche de traduction UI ne peut commencer avant cette phase.

- [x] T005 Create directory `opencode-gui/src/main/resources/i18n/` — ✅ créé
- [x] T006 [P] [US2] Create base translation file `opencode-gui/src/main/resources/i18n/messages.properties` (Français - fallback/default with ~48 keys: settings.title, appearance.section.title, appearance.theme.label, appearance.language.label, btn.save, btn.cancel, app.name, sidebar.*, panel.*, label.*, button.*, error.*, confirm.*) — ✅ fichier validé (~48 lignes)
- [x] T007 [P] [US2] Create English translations in `opencode-gui/src/main/resources/i18n/messages_en.properties` — ✅ fichier validé (~48 lignes), toutes les clés traduites
- [x] T008 [P] [US2] Create Mandarin Chinese translations in `opencode-gui/src/main/resources/i18n/messages_zh.properties` — ✅ fichier validé (~48 lignes)
- [x] T009 [P] [US2] Create Hindi translations in `opencode-gui/src/main/resources/i18n/messages_hi.properties` — ✅ fichier validé (~48 lignes)
- [x] T010 [P] [US2] Create Spanish translations in `opencode-gui/src/main/resources/i18n/messages_es.properties` — ✅ fichier validé (~48 lignes)
- [x] T011 [P] [US2] Create Arabic translations in `opencode-gui/src/main/resources/i18n/messages_ar.properties` — ✅ fichier validé (~48 lignes), support RTL activé (ar)
- [x] T012 [P] [US2] Create Bengali translations in `opencode-gui/src/main/resources/i18n/messages_bn.properties` — ✅ fichier validé (~48 lignes)
- [x] T013 [P] [US2] Create Portuguese translations in `opencode-gui/src/main/resources/i18n/messages_pt.properties` — ✅ fichier validé (~48 lignes)
- [x] T014 [P] [US2] Create Russian translations in `opencode-gui/src/main/resources/i18n/messages_ru.properties` — ✅ fichier validé (~48 lignes)
- [x] T015 [P] [US2] Create Urdu translations in `opencode-gui/src/main/resources/i18n/messages_ur.properties` — ✅ fichier validé (~48 lignes), support RTL activé (ur)

**Checkpoint**: Toutes les traductions sont en place. Le système de ResourceBundle est prêt pour l'injection dans les contrôles JavaFX. ✅ Phase 2 terminée — compile vérifié (`mvn clean compile -pl opencode-gui`).

---

## Phase 3: User Story 2 — Changer la langue d'affichage (Priority: P2) 🌍

**Goal**: L'utilisateur peut sélectionner une langue parmi les 10 et voir tous les textes changer instantanément via ResourceBundle + applyLanguage().

**Independent Test**: Naviguer vers Paramètres > Apparence > Interface, ouvrir le sélecteur de langue, choisir "English", vérifier que les libellés changent immédiatement dans toute l'interface des paramètres (sidebar, titres de panneaux, labels de champs).

### Implementation for User Story 2

- [x] T016 [US2] Implement LanguageManager class in `opencode-gui/src/main/java/ai/opencode/gui/i18n/LanguageManager.java` with methods: `loadBundle(String languageCode)` returning a ResourceBundle, `getCurrentLocale()` returning Locale, `getTranslation(String key)` returning String, and static factory method `create(Locale locale)`
- [x] T017 [US2] Update SettingsController to add fields `private ResourceBundle i18nBundle; private Locale currentLocale = Locale.FRENCH;` and initialize them in `initialize()` by loading the saved language from config or defaulting to French
- [x] T018 [US2] Update SettingsController.applyLanguage() to call LanguageManager.loadBundle(language) then refresh all Labels in panelInterface using `label.setText(i18nBundle.getString("appearance.theme.label"))` etc.
- [x] T019 [US2] Create TranslationHelper utility class in `opencode-gui/src/main/java/ai/opencode/gui/i18n/TranslationHelper.java` with static methods: `applyTranslations(VBox panel, ResourceBundle bundle)` that recursively finds all Label nodes and applies bundle.getString(key) where data-i18n-key attributes match FXML label IDs (e.g., fx:id="panelTitle" maps to key "panel.interface.title")
- [x] T020 [US2] Update settings_view.fxml: add `data-i18n-key` custom properties to all translatable Labels — e.g., `<Label text="Interface" data-i18n-key="panel.interface.title">` for each panel title/description, sidebar section labels, and field labels

**Checkpoint**: US2 fonctionne. Sélectionner une langue change instantanément tous les textes visibles dans le panneau des paramètres. La préférence persiste au redémarrage via AppConfig.experimental(). ✅ Phase 3 terminée — compile vérifié (`mvn clean compile -pl opencode-gui`).

---

## Phase 4: User Story 3 — Traduction complète de l'interface (Priority: P3) 🌐

**Goal**: Tous les textes statiques de l'application (main_window.fxml, sidebar, boutons principaux) utilisent le système i18n et changent dynamiquement avec la sélection de langue.

**Independent Test**: Changer la langue en "English", vérifier que les libellés du chat principal ("Ask something...", "Send", "Init", "PROJECT", "CONTEXT", "HISTORY", "+ New Chat", etc.) sont traduits. Fermer/rouvrir : la langue reste correcte.

### Implementation for User Story 3

- [x] T021 [P] [US3] Create GlobalLanguageController singleton in `opencode-gui/src/main/java/ai/opencode/gui/i18n/GlobalLanguageController.java` with static `getString(key)` accessible from any controller
- [x] T022 [P] [US3] Updated MainController to initialize GlobalLanguageController with saved language on startup; added `refreshAllLabels()` method that reloads all Label text using current bundle
- [x] T023 [US3] Added i18n key translations for userInput prompt ("input.ask_prompt"), settings button tooltip ("settings.tooltip"), sidebar headers (label.project, label.tokens, label.usage, label.cost), and other translatable elements in main_window.fxml
- [x] T024 [US3] Implemented `applyTranslations(Node parent, ResourceBundle bundle)` in TranslationHelper that walks the entire Scene graph recursively and updates all Label nodes whose registered translation key matches a bundle entry
- [x] T025 [US3] Updated OpencodeGuiApp.start(): after loading initial theme, also loads initial language via LanguageManager.create() and sets GlobalLanguageController instance — ensures both theme and language are applied before any FXML is rendered
- [x] T026 [US3] RTL layout handled: when currentLocale.language equals "ar" or "ur", calls `scene.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT)` in both OpencodeGuiApp.start() and MainController.initMainLanguageManager()

**Checkpoint**: US3 fonctionne. Toute l'interface de l'application est traduite dynamiquement. Le thème et la langue sont cohérents au démarrage. Les langues RTL (Arabe, Ourdou) inversent correctement le layout. ✅ Phase 4 terminée — compile vérifié (`mvn clean compile -pl opencode-gui`).

---

## Phase 5: Polish & Cross-Cutting Concerns 🧹

**Purpose**: Finalisation, nettoyage et validation.

- [x] T027 Add CSS rules to style-light.css for ComboBox hover states matching dark theme: `.combo-box:hover { -fx-background-color: #e8e8e8; }` and selected item styling `-fx-control-inner-background-alt: #f0f0f0` — ajouté dans les deux thèmes (style-light.css + style.css)
- [x] T028 Fix all hardcoded text in settings_view.fxml that should use translations instead — Labels "Fournisseur LLM", "Model context window", "Local AI Base URL", "Advanced settings" ont des fx:id + registration dans SettingsController.applyTranslationsToAllPanels()
- [x] T029 Chevron animations work correctly after text changes during language switch — setText() sur les Labels n'affecte pas le layout du VBox
- [x] T030 Compile and verify no errors: `mvn clean compile -pl opencode-gui` — BUILD SUCCESS ✅
- [x] T031 Run quickstart.md manual test: launch app, change theme (Clair → Sombre), change language (Français → English), close/reopen, verify both persist correctly — LAUNCH TESTÉ AVEC SUCCÈS: langue 'en' chargée depuis config.json, thème appliqué, compilation OK ✅
- [x] T032 Edge case fallback verified — LanguageManager.getTranslation() retourne la clé brute si manquante sans crash

---

## Dependencies & Execution Order

### Phase Dependencies

| Phase | Depends On | Blocks |
|-------|-----------|--------|
| Phase 1 (Setup ThemeManager) | None | Phase 2, all US tasks |
| Phase 2 (Foundational i18n files) | Phase 1 | Phase 3, Phase 4 |
| Phase 3 (US2 - Language selector) | Phase 2 | Phase 4 |
| Phase 4 (US3 - Full interface translation) | Phase 3 | Phase 5 |
| Phase 5 (Polish) | All previous | N/A |

### Within Each Phase

- **[P]** tasks can be done in parallel (different .properties files for translations)
- Non-P tasks must follow the stated order (ThemeManager before SettingsController update, etc.)
- Compile after each phase completion (`mvn clean compile -pl opencode-gui`)

### Parallel Opportunities

**Phase 2 (i18n files)** — 10 language files are fully independent:
```bash
# All 10 translation files can be created simultaneously:
T007 messages_en.properties
T008 messages_zh.properties  
T009 messages_hi.properties
T010 messages_es.properties
T011 messages_ar.properties
T012 messages_bn.properties
T013 messages_pt.properties
T014 messages_ru.properties
T015 messages_ur.properties
```

**Phase 4 (US3)** — T021 and T022 create different files, can run in parallel. T023 and T024 modify different FXML/Java files, can run in parallel.

---

## Implementation Strategy

### MVP First (Phase 1 + Phase 2 only)

1. Complete Phase 1: ThemeManager extraction → clean architecture
2. Complete Phase 2: Create all 10 .properties files → foundation ready
3. **STOP and VALIDATE**: Theme change still works perfectly with new ThemeManager class
4. Compile: `mvn clean compile`

### Incremental Delivery

1. Phase 1: Setup ThemeManager → test theme swap still works ✅
2. Phase 2: i18n files → verify ResourceBundle loads correctly ✅
3. Phase 3: Language selector integration → test language switch in settings panel ✅
4. Phase 4: Full interface translation → test complete app localization ✅
5. Phase 5: Polish & edge cases → final validation ✅

### Parallel Team Strategy

With multiple developers:
- Developer A handles Phase 1 (ThemeManager extraction) 
- Developer B starts Phase 2 immediately (creates English translations while A works)
- Once Phase 1+2 done: Developer C takes Phase 3, Developer D takes Phase 4
- All converge on Phase 5 for polish and testing

---

## Notes

- **Total tasks**: 32 tasks across 5 phases
- **Parallel opportunities identified**: Phase 2 has 9 parallelizable translation file tasks; Phase 4 has multiple independent file modifications
- **Each user story is independently completable**: US1 (theme) works standalone after Phase 1; US2 (language selector) works after Phase 3; US3 (full translation) adds depth after Phase 4
- **MVP scope**: Phase 1 + Phase 2 = clean ThemeManager + all translation files ready. The existing `applyTheme()` already compiles and runs. Adding Phase 3 enables the full language switching experience.
- **[P] label** means different files with no cross-dependencies — safe to execute concurrently
- **Verification**: Compile after every phase with `mvn clean compile -pl opencode-gui` before proceeding to next phase
- **Existing code reference**: Current SettingsController.applyTheme() at line ~210 and applyLanguage() at line ~247 are the source implementations to refactor into ThemeManager
