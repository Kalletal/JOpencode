# Data Model: Apparence Interface

## PréférenceApparence

Représente l'ensemble des préférences visuelles et linguistiques d'un utilisateur pour l'interface graphique.

| Champ | Type | Description | Contrainte |
|-------|------|-------------|------------|
| `theme` | String | Thème actif : `"Clair"` ou `"Sombre"` | Valeur obligatoire, non vide |
| `language` | String | Code langue sélectionné (nom affiché) | Doit correspondre à une langue du sélecteur (10 langues supportées) |
| `lastModified` | Instant | Timestamp de la dernière modification | Généré automatiquement |

### Persistance
Stocké dans `AppConfig.experimental()` sous les clés `"theme"` et `"language"`. Sérialisé en JSON par `ConfigManager.saveConfig()`.

### Cycle de vie
1. **Lecture** : Au démarrage de l'app (`OpencodeGuiApp.start()`) ou ouverture des paramètres (`SettingsController.initialize()`). Si absent → valeurs par défaut (`"Sombre"`, `"Français"`).
2. **Écriture** : À chaque changement via `applyTheme()` ou `applyLanguage()`.
3. **Validation** : Seules 2 valeurs valides pour `theme`, 10 valeurs valides pour `language`. Toute autre valeur est ignorée.

---

## ConfigurationApparence (Agrégat)

Regroupe `PréférenceApparence` avec les métadonnées de configuration :

| Champ | Type | Description |
|-------|------|-------------|
| `version` | int | Version du schéma de config apparence (actuellement 1) |
| `migratedFrom` | String? | Version précédente si migration nécessaire |

> **Note** : Actuellement non persisté séparément — intégré directement dans `AppConfig.experimental()`. La version sera ajoutée lors d'une future migration si le schéma évolue.

---

## Dictionnaire de traduction (ResourceBundle)

Chaque fichier `.properties` contient un ensemble de paires clé=valeur. Structure type :

```properties
# messages.properties (Français - fallback)
settings.title=Configuration de l'Agent
appearance.section.title=Interface
appearance.theme.label=Thème de l'interface
appearance.language.label=Langue d'affichage
btn.save=Sauvegarder
btn.cancel=Annuler
app.name=opencode - Java Edition
sidebar.fournisseurs_llm=Fournisseurs LLM
sidebar.admin=Admin
sidebar.agent_skills=Compétences de l'agent
sidebar.appearance=Apparence
```

### Règles
- Clés uniques par langue, identiques entre tous les fichiers.
- Fallback automatique vers `messages.properties` si une clé manque.
- Encodage UTF-8 pour supporter les caractères spéciaux (arabe, bengali, ourdou, etc.).
- Échappement des caractères spéciaux selon la syntaxe properties Java (`\uXXXX` ou texte brut UTF-8).
