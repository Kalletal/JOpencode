# UI Contract: Panneau Apparence > Interface

## Vue : Panneau Interface (settings_view.fxml)

### Structure
```
VBox panelInterface
├── VBox (titre + description)
│   ├── Label "Interface" (h2, blanc gras 24px)
│   └── Label description grise 13px
├── Separator (gris #333333)
├── VBox (paramètres, spacing 15)
│   ├── Label "Thème" (label section, gras 14px)
│   ├── VBox (champ thème)
│   │   ├── Label "Thème de l'interface" (label champ, 12px gris)
│   │   └── ComboBox fx:id="themeSelector" ← [Clair, Sombre]
│   ├── VBox (champ langue)
│   │   ├── Label "Langue d'affichage" (label champ, 12px gris)
│   │   └── ComboBox fx:id="languageSelector" ← [10 langues]
│   └── [plus de boutons Annuler/Sauvegarder]
```

### Composants interactifs

#### ComboBox themeSelector
- **Items** : `"Sombre"`, `"Clair"`
- **Valeur par défaut** : Lue depuis `AppConfig.experimental().get("theme")` ou `"Sombre"`
- **Événement** : `onAction="#applyTheme"` → change CSS + couleurs inline + sauvegarde config
- **Style** : Fond #2d2d2d (sombre) / #ffffff (clair), texte blanc/noir, bordure #444444/#cccccc

#### ComboBox languageSelector
- **Items** : Les 10 langues les plus parlées au monde dans leur script natif.
- **Valeur par défaut** : Lue depuis `AppConfig.experimental().get("language")` ou `"Français"`
- **Événement** : `onAction="#applyLanguage"` → change ResourceBundle + sauvegarde config
- **Style** : Identique à themeSelector

### Comportements

| Action | Résultat | Persistance |
|--------|----------|-------------|
| Clic sur "Clair" | Swap CSS vers style-light.css + applyThemeColors() sur tout le scene graph | Oui — écrit dans AppConfig |
| Clic sur "Sombre" | Swap CSS vers style.css + applyThemeColors() sur tout le scene graph | Oui — écrit dans AppConfig |
| Sélection d'une langue | Chargement du ResourceBundle correspondant + mise à jour de tous les Labels visibles | Oui — écrit dans AppConfig |
| Fermeture des paramètres | Préférences conservées (déjà sauvegardées lors du changement) | N/A |
| Redémarrage app | Thème et langue restaurés depuis AppConfig | Automatique via OpencodeGuiApp.start() |

### États visuels

#### Thème Sombre (par défaut)
- Fond principal : `#1a1a1a`, `#1e1e1e`
- Panneaux : `#2d2d2d`
- Texte : `#cccccc`, blanc pour les titres
- Bordures : `#444444`
- Boutons primaires : `#007acc`

#### Thème Clair
- Fond principal : `#f0f0f0`, `#f5f5f5`
- Panneaux : `#ffffff`
- Texte : `#333333`, `#1a1a1a` pour les titres
- Bordures : `#cccccc`
- Boutons primaires : `#007acc` (inchangé)

## Panneau LLM Preference (non modifié)
Le panneau "Préférence LLM" conserve ses boutons "Annuler" et "Sauvegarder" — seule la section Apparence > Interface est concernée par leur suppression.
