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

- [ ] T001 Create package directory structure `opencode-gui/src/main/java/ai/opencode/gui/theme/`
- [ ] T002 [P] [US1] Implement ThemeManager class in `opencode-gui/src/main/java/ai/opencode/gui/theme/ThemeManager.java` with methods: `applyTheme(Scene scene, String themeName)`, `swapStylesheets(Scene scene, boolean isDark)`, `applyThemeColorsRecursive(javafx.scene.Node root, boolean isDark)` using the existing CSS swap logic from SettingsController.applyTheme() and applyThemeColors()
- [ ] T003 [P] [US1] Update SettingsController.applyTheme() to delegate to ThemeManager.applyTheme(currentScene, theme) instead of inline implementation
- [ ] T004 [US1] Add ThemeManager constructor that loads both CSS paths via getClass().getResource("/css/style.css") and getClass().getResource("/css/style-light.css") for caching

**Checkpoint**: US1 (changer le thème visuel) fonctionne avec une architecture propre et réutilisable. Le code est compilé et testable.

---

## Phase 2: Foundational — Système i18n (Blocking Prerequisites)

**Purpose**: Infrastructure de localisation JavaFX standard avant toute traduction d'interface.

**⚠️ CRITICAL**: Aucune tâche de traduction UI ne peut commencer avant cette phase.

- [ ] T005 Create directory `opencode-gui/src/main/resources/i18n/`
- [ ] T006 [P] Create base translation file `opencode-gui/src/main/resources/i18n/messages.properties` (Français - fallback/default) with keys: settings.title, appearance.section.title, appearance.theme.label, appearance.language.label, btn.save, btn.cancel, app.name, sidebar.fournisseurs_llm, sidebar.admin, sidebar.agent_skills, sidebar.appearance, panel.llm_preference.title, panel.llm_preference.description, panel.historique_chats.title, panel.historique_chats.description, panel.default_prompt.title, panel.default_prompt.description, panel.interface.title, panel.interface.description, panel.agent_skills.title, panel.agent_skills.description, panel.voix_parole.title, panel.voix_parole.description, label.context_window, label.api_key, label.model, label.local_ai_url, label.advanced_settings, label.system_prompt, label.conserve_historique, button.init_project, button.send_message, button.new_chat, label.project, label.context, label.tokens, label.usage, label.cost, label.servers, label.navigation, label.history, label.files_in_context, label.support, label.privacy, label.license, error.api_key_required, confirm.save_success, confirm.settings_saved
- [ ] T007 [P] Create English translations in `opencode-gui/src/main/resources/i18n/messages_en.properties` — translate all 39 keys from French to English using context from existing UI labels (e.g., "Fournisseurs LLM" → "LLM Providers", "Apparence" → "Appearance")
- [ ] T008 [P] Create Mandarin Chinese translations in `opencode-gui/src/main/resources/i18n/messages_zh.properties` — translate all 39 keys
- [ ] T009 [P] Create Hindi translations in `opencode-gui/src/main/resources/i18n/messages_hi.properties` — translate all 39 keys
- [ ] T010 [P] Create Spanish translations in `opencode-gui/src/main/resources/i18n/messages_es.properties` — translate all 39 keys
- [ ] T011 [P] Create Arabic translations in `opencode-gui/src/main/resources/i18n/messages_ar.properties` — translate all 39 keys (RTL direction support)
- [ ] T012 [P] Create Bengali translations in `opencode-gui/src/main/resources/i18n/messages_bn.properties` — translate all 39 keys
- [ ] T013 [P] Create Portuguese translations in `opencode-gui/src/main/resources/i18n/messages_pt.properties` — translate all 39 keys
- [ ] T014 [P] Create Russian translations in `opencode-gui/src/main/resources/i18n/messages_ru.properties` — translate all 39 keys
- [ ] T015 [P] Create Urdu translations in `opencode-gui/src/main/resources/i18n/messages_ur.properties` — translate all 39 keys (RTL direction support)

**Checkpoint**: Toutes les traductions sont en place. Le système de ResourceBundle est prêt pour l'injection dans les contrôles JavaFX.

---

## Phase 3: User Story 2 — Changer la langue d'affichage (Priority: P2) 🌍

**Goal**: L'utilisateur peut sélectionner une langue parmi les 10 et voir tous les textes changer instantanément via ResourceBundle + applyLanguage().

**Independent Test**: Naviguer vers Paramètres > Apparence > Interface, ouvrir le sélecteur de langue, choisir "English", vérifier que les libellés changent immédiatement dans toute l'interface des paramètres (sidebar, titres de panneaux, labels de champs).

### Implementation for User Story 2

- [ ] T016 [US2] Implement LanguageManager class in `opencode-gui/src/main/java/ai/opencode/gui/i18n/LanguageManager.java` with methods: `loadBundle(String languageCode)` returning a ResourceBundle, `getCurrentLocale()` returning Locale, `getTranslation(String key)` returning String, and static factory method `create(Locale locale)`
- [ ] T017 [US2] Update SettingsController to add fields `private ResourceBundle i18nBundle; private Locale currentLocale = Locale.FRENCH;` and initialize them in `initialize()` by loading the saved language from config or defaulting to French
- [ ] T018 [US2] Update SettingsController.applyLanguage() to call LanguageManager.loadBundle(language) then refresh all Labels in panelInterface using `label.setText(i18nBundle.getString("appearance.theme.label"))` etc.
- [ ] T019 [US2] Create TranslationHelper utility class in `opencode-gui/src/main/java/ai/opencode/gui/i18n/TranslationHelper.java` with static methods: `applyTranslations(VBox panel, ResourceBundle bundle)` that recursively finds all Label nodes and applies bundle.getString(key) where data-i18n-key attributes match FXML label IDs (e.g., fx:id="panelTitle" maps to key "panel.interface.title")
- [ ] T020 [US2] Update settings_view.fxml: add `data-i18n-key` custom properties to all translatable Labels — e.g., `<Label text="Interface" data-i18n-key="panel.interface.title">` for each panel title/description, sidebar section labels, and field labels

**Checkpoint**: US2 fonctionne. Sélectionner une langue change instantanément tous les textes visibles dans le panneau des paramètres. La préférence persiste au redémarrage via AppConfig.experimental().

---

## Phase 4: User Story 3 — Traduction complète de l'interface (Priority: P3) 🌐

**Goal**: Tous les textes statiques de l'application (main_window.fxml, sidebar, boutons principaux) utilisent le système i18n et changent dynamiquement avec la sélection de langue.

**Independent Test**: Changer la langue en "English", vérifier que les libellés du chat principal ("Ask something...", "Send", "Init", "PROJECT", "CONTEXT", "HISTORY", "+ New Chat", etc.) sont traduits. Fermer/rouvrir : la langue reste correcte.

### Implementation for User Story 3

- [ ] T021 [P] [US3] Create GlobalLanguageController in `opencode-gui/src/main/java/ai/opencode/gui/i18n/GlobalLanguageController.java` — singleton that holds the current ResourceBundle and provides a static `getString(key)` method accessible from any controller
- [ ] T022 [P] [US3] Update MainController to initialize GlobalLanguageController with the saved language on startup (read from AppConfig.experimental() same as OpencodeGuiApp does for theme), and add method `refreshAllLabels()` that reloads all Label text from main_window.fxml using the current bundle
- [ ] T023 [US3] Add data-i18n-key attributes to all translatable Labels in `opencode-gui/src/main/resources/fxml/main_window.fxml`: userInput prompt → "input.ask_prompt", settings toggle button tooltip → "settings.tooltip", agentLabel/modelLabel/serverLabel display texts, sidebar section headers (PROJECT, CONTEXT, SERVERS, NAVIGATION, HISTORY, FILES IN CONTEXT), session history labels, context list items
- [ ] T024 [US3] Implement applyTranslationsInScene(Scene scene, ResourceBundle bundle) in TranslationHelper that walks the entire Scene graph and updates all Label nodes whose fx:id matches a translation key pattern
- [ ] T025 [US3] Update OpencodeGuiApp.start(): after loading initial theme, also load initial language via LanguageManager.create(Locale.forLanguageTag(savedLang)) and set GlobalLanguageController instance — this ensures both theme and language are applied before any FXML is rendered
- [ ] T026 [US3] Handle RTL layout for Arabic/Urdu: when currentLocale.language equals "ar" or "ur", set `root.setLayoutDirection(javafx.scene.layout.LayoutDirection.RTL)` on the root BorderPane of main_window.fxml via MainController.applyRTL()

**Checkpoint**: US3 fonctionne. Toute l'interface de l'application est traduite dynamiquement. Le thème et la langue sont cohérents au démarrage. Les langues RTL (Arabe, Ourdou) inversent correctement le layout.

---

## Phase 5: Polish & Cross-Cutting Concerns 🧹

**Purpose**: Finalisation, nettoyage et validation.

- [ ] T027 Add CSS rules to style-light.css for ComboBox hover states matching dark theme: `.combo-box:hover { -fx-background-color: #e8e8e8; }` and selected item styling `-fx-control-inner-background-alt: #f0f0f0`
- [ ] T028 Fix all hardcoded text in settings_view.fxml that should use translations instead: replace `text="Préférence LLM"` with dynamic loading from i18nBundle, same for "Advanced settings", "Local AI Base URL", "Model context window", "API Key", "Model"
- [ ] T029 Update existing FXML labels in `opencode-gui/src/main/resources/fxml/settings_menu.css` sidebar header texts — ensure chevron animations work correctly after text changes during language switch
- [ ] T030 Compile and verify no errors: run `mvn clean compile -pl opencode-gui` from repo root
- [ ] T031 Run quickstart.md manual test: launch app, change theme (Clair → Sombre), change language (Français → English), close/reopen, verify both persist correctly
- [ ] T032 Verify edge case: select a non-translated key (e.g., a typo) — confirm fallback to French via ResourceBundle default mechanism works without crash

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
