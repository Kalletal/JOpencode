# Implementation Plan: Apparence Interface (Thème + Langue)

**Branch**: `001-appearance-settings` | **Date**: 2026-04-23 | **Spec**: [spec.md](../spec.md)
**Input**: Feature specification from `/specs/001-appearance-settings/spec.md`

## Summary

Ajouter la gestion complète du thème visuel (Clair/Sombre) et de la langue d'affichage dans le panneau Apparence > Interface des paramètres. Le changement de thème s'applique instantanément à toute l'interface par swap de CSS JavaFX. La langue se sélectionne parmi les 10 plus parlées au monde et persiste automatiquement. Suppression du paramètre "Taille de police" existant et des boutons "Annuler"/"Sauvegarder" du panneau Apparence.

## Technical Context

**Language/Version**: Java 21  
**Primary Dependencies**: JavaFX (GUI), Maven multi-module  
**Storage**: Fichier JSON local via `ConfigManager` (`opencode-storage`) — clé `experimental.theme` et `experimental.language` dans `AppConfig.experimental()`  
**Testing**: JUnit 5, Mockito (tests unitaires existants dans `opencode-core`)  
**Target Platform**: Desktop (Windows/Linux/macOS) via JavaFX  
**Project Type**: Application desktop JavaFX avec architecture modulaire  
**Performance Goals**: Changement de thème < 500ms, application immédiate sans rechargement complet  
**Constraints**: Pas de rechargement de scène requis pour les changements de thème/langue ; compatibilité rétroactive avec config existante  
**Scale/Scope**: ~1 écran de paramètres principal + 4-5 sous-panneaux, ~10 labels traduits initialement  

## Constitution Check

| Gate | Statut | Commentaires |
|------|--------|-------------|
| I. Library-first | ✅ Pass | Modifications uniquement dans `opencode-gui`, modules core non touchés |
| II. CLI Interface | ✅ N/A | Feature purement GUI |
| III. Test-First | ✅ Pass | Tests unitaires prévus sur ThemeSwitcher et LanguageManager |
| IV. Integration Testing | ✅ Pass | Un test d'intégration vérifiera le swap CSS + restauration config |
| V. Simplicity | ✅ Pass | Approche minimaliste : CSS swap + ResourceBundle standard Java |

**Post-design re-check** : Non applicable — aucune complexité ajoutée. Le design utilise des patterns déjà présents dans le codebase (CSS stylesheets JavaFX, AppConfig.experimental() pour persistance).

## Project Structure

### Documentation (this feature)

```text
specs/001-appearance-settings/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/ui-contract.md   # UI contract for settings panel
└── tasks.md             # Phase 2 output (/speckit.tasks command)
```

### Source Code (repository root)

```text
opencode-gui/src/main/resources/css/
├── style.css            # Thème sombre (existant)
└── style-light.css      # Thème clair (existant)

opencode-gui/src/main/java/ai/opencode/gui/
├── OpencodeGuiApp.java          # Modifié : chargement thème initial depuis config
├── MainController.java          # Modifié : passe Scene au SettingsController
├── SettingsController.java      # Modifié : applyTheme(), applyLanguage(), setupLanguageSelector()
└── theme/                       # NOUVEAU package
    └── ThemeManager.java        # Gestionnaire de thème centralisé (swap CSS + couleurs inline)

opencode-gui/src/main/resources/i18n/       # NOUVEAU répertoire
├── messages.properties              # Français (par défaut)
├── messages_en.properties           # English
├── messages_zh.properties           # 中文（普通话）
├── messages_hi.properties           # हिन्दी
├── messages_es.properties           # Español
├── messages_ar.properties           # العربية
├── messages_bn.properties           # বাংলা
├── messages_pt.properties           # Português
├── messages_ru.properties           # Русский
└── messages_ur.properties           # اردو

opencode-storage/src/main/java/ai/opencode/storage/AppConfig.java   # Non modifié (experimental Map existante)
```

**Structure Decision**: Utiliser le package `theme` existant dans `SettingsController` pour extraire la logique de changement de thème dans un `ThemeManager` dédié. Les traductions utilisent `java.util.ResourceBundle` standard Java (aucune dépendance externe). La persistance passe par `AppConfig.experimental()` déjà en place.

## Phase 0: Research & Decisions

### R-001 : Mécanisme de swap CSS JavaFX — Décision validée

**Décision** : Utiliser `Scene.getStylesheets()` pour remplacer dynamiquement les feuilles de style. Cette approche est déjà implémentée dans `SettingsController.applyTheme()`.

**Rationale** : 
- JavaFX supporte nativement le remplacement des stylesheets au runtime via `scene.getStylesheets().clear()` + `.add()`.
- Déjà testé et fonctionnel avec `style-light.css`.
- Aucun rechargement de scène nécessaire.
- Moins invasif qu'un override de propriétés CSS via `-fx-property`.

**Alternatives considérées** :
1. **CSS Variables / `-fx-custom-property`** : Plus élégant mais nécessite de définir toutes les variables personnalisées dans les deux thèmes. Plus de maintenance.
2. **Two Scene graphs** : Un scene clair, un scene sombre. Trop lourd à gérer.
3. **Inline style overrides** : Fait partiellement dans `applyThemeColors()` mais insuffisant seul (ne couvre pas tous les contrôles JavaFX).

### R-002 : Système de traduction — ResourceBundle Java standard

**Décision** : Utiliser `java.util.ResourceBundle` pour la localisation, avec fallback automatique sur `messages.properties` (Français).

**Rationale** :
- Standard Java, aucune dépendance externe.
- Fallback automatique intégré si une clé manque dans une langue.
- Compatible avec FXML via `StringResource` ou chargement en Java via `bundle.getString("key")`.
- Les 10 langues sont codées en dur dans le ComboBox ; chaque langue aura son fichier `.properties`.

**Alternatives considérées** :
1. **i18n framework tiers** (ex: JUnidecode) : Surdimensionné pour 10 fichiers de clés simples.
2. **JSON-based i18n** : Inutile car Java supporte nativement `.properties`.

### R-003 : Persistance — AppConfig.experimental() existant

**Décision** : Stocker `theme` et `language` dans `AppConfig.experimental().put("theme", "Clair")` et `put("language", "English")`.

**Rationale** :
- Déjà utilisé dans le code actuel (`SettingsController.applyTheme()`).
- Sérialisé automatiquement par `ConfigManager.setConfig()`.
- Aucune modification du record `AppConfig` nécessaire.

## Phase 1: Design & Contracts

### Data Model

Voir `data-model.md`.

### UI Contract

Voir `contracts/ui-contract.md`.

### Quickstart

Voir `quickstart.md`.

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| Nouveau package `theme/` | Centraliser la logique theme pour réutilisabilité | Garder dans SettingsController : trop couplé au contrôleur FXML |
| 10 fichiers `.properties` | Support multilingue complet | Un seul fichier JSON : fallback moins robuste, parsing supplémentaire requis |
