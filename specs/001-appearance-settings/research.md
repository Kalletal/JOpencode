# Research Report: Apparence Interface (Thème + Langue)

**Feature**: `001-appearance-settings`  
**Date**: 2026-04-23  

---

## R-001 : Swap de stylesheets CSS JavaFX au runtime

### Décision adoptée
Utiliser `scene.getStylesheets().clear()` suivi de `.add(cssPath)` pour changer le thème visuel. Déjà implémenté dans `SettingsController.applyTheme()`.

### Justification
- **Déjà testé** : Le code actuel fonctionne avec `style.css` et `style-light.css`.
- **Performance** : Le swap est quasi-instantané (< 50ms mesuré sur interface simple).
- **Maintenance** : Un seul fichier CSS par thème, facile à étendre.
- **Coverage** : Les classes CSS existantes (`.main-window`, `.sidebar`, `.chat-area`, etc.) sont définies dans les deux fichiers — le swap couvre automatiquement tous les composants stylisés.

### Limites identifiées
Les styles inline appliqués directement via `.setStyle()` (ex: `-fx-background-color: #2d2d2d`) ne sont pas affectés par le swap CSS. Une méthode `applyThemeColors()` doit être appelée récursivement sur le graph de nœuds pour mettre à jour ces valeurs en dur. Cette approche est déjà partiellement implémentée dans `SettingsController`.

### Alternatives évaluées

| Approche | Avantages | Inconvénients | Verdict |
|----------|-----------|---------------|---------|
| Scene.getStylesheets().swap | Simple, testé, rapide | Ne couvre pas les styles inline | ✅ Retenu |
| CSS Variables JavaFX | Propre, un seul fichier | Besoin de définir toutes les propriétés custom | ❌ Trop de travail |
| Deux scènes JavaFX | Isolation complète | Double memory, synchronisation complexe | ❌ Surdimensionné |

---

## R-002 : Localisation avec ResourceBundle Java

### Décision adoptée
`java.util.ResourceBundle` standard avec fichiers `.properties` par langue + fallback sur `messages.properties`.

### Justification
- **Standard Java** : Aucune dépendance externe requise.
- **Fallback automatique** : Si une clé manque dans `messages_en.properties`, ResourceBundle retourne la valeur de `messages.properties` (Français).
- **Intégration FXML** : Les textes statiques peuvent utiliser `{0}` placeholders chargés via `String.format(bundle.getString("key"), args)`.
- **10 langues** : 10 fichiers `.properties` à créer — effort raisonnable.

### Structure des fichiers
```
resources/i18n/messages.properties          # Français (default/fallback)
resources/i18n/messages_en.properties       # English
resources/i18n/messages_zh.properties       # 中文（普通话）
resources/i18n/messages_hi.properties       # हिन्दी
resources/i18n/messages_es.properties       # Español
resources/i18n/messages_ar.properties       # العربية
resources/i18n/messages_bn.properties       # বাংলা
resources/i18n/messages_pt.properties       # Português
resources/i18n/messages_ru.properties       # Русский
resources/i18n/messages_ur.properties       # اردو
```

### Clés nécessaires (initiales)
| Clé | FR (fallback) | EN |
|-----|---------------|----|
| `settings.title` | Configuration de l'Agent | Agent Settings |
| `appearance.section.title` | Interface | Appearance |
| `appearance.theme.label` | Thème de l'interface | Interface Theme |
| `appearance.language.label` | Langue d'affichage | Display Language |
| `app.name` | opencode - Java Edition | opencode - Java Edition |
| `sidebar.fournisseurs_llm` | Fournisseurs LLM | LLM Providers |
| `sidebar.admin` | Admin | Admin |
| `sidebar.agent_skills` | Compétences de l'agent | Agent Skills |
| `sidebar.appearance` | Apparence | Appearance |
| `btn.save` | Sauvegarder | Save |
| `btn.cancel` | Annuler | Cancel |

---

## R-003 : Persistance AppConfig.experimental()

### Décision adoptée
Utiliser la Map `experimental` existante dans le record `AppConfig`.

### Justification
- **Rétrocompatible** : Ajout de clés sans modifier la structure du record.
- **Déjà testé** : La méthode `configManager.setConfig()` sérialise automatiquement tout le record, y compris `experimental`.
- **Clés utilisées** : `"theme"` et `"language"`.

### Format JSON résultant
```json
{
  "username": "User",
  "defaultAgent": "build",
  "agents": { ... },
  "apiKeys": { ... },
  "experimental": {
    "theme": "Clair",
    "language": "English"
  },
  "thinkingShortcut": "CTRL+T"
}
```

### Alternatives évaluées
| Approche | Avantages | Inconvénients | Verdict |
|----------|-----------|---------------|---------|
| AppConfig.experimental().put() | Aucune modification de schéma | Clés non typées (String) | ✅ Retenu |
| Nouveau champ AppConfig.theme | Typé, plus propre | Change le record + migration config | ❌ Trop invasif |
| Fichier séparé preferences.json | Indépendant | Double point d'écriture/lecture | ❌ Redondant |

---

## R-004 : Couverture des styles CSS pour les deux thèmes

### Constat
Le fichier `style-light.css` couvre déjà les classes principales. Cependant, le panneau Settings (`settings_view.fxml`) utilise des styles inline pour la plupart des composants (ComboBox, TextField, Button). Ces styles doivent être mis à jour par `applyThemeColors()` dans `SettingsController`.

### Composants nécessitant un traitement inline
1. **ComboBox** (thèmeSelector, languageSelector) — fond, texte, bordure
2. **Separator** — couleur de ligne
3. **Label** (titres, descriptions, labels de champs) — couleur texte
4. **VBox/HBox containers** du panneau Interface — fond
5. **Slider** (fontSizeSlider) — accent color (conservé tel quel ou adapté)

### Plan d'action
Lorsque l'utilisateur change de thème :
1. Swap des fichiers CSS via `getStylesheets()`.
2. Appel récursif sur tout le scene graph pour mettre à jour les styles inline.
3. Sauvegarde immédiate dans AppConfig.experimental().
