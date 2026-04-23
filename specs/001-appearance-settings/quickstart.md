# Quickstart: Apparence Interface

## Prérequis

- Java 21 installé
- Maven disponible
- Projet cloné et compilé : `mvn clean compile`

## Exécution rapide

```bash
cd /home/gilles/Documents/ProjetJava/JOpencode
mvn javafx:run -pl opencode-gui
```

## Tester le changement de thème

1. Ouvrir l'application, cliquer sur le bouton ⚙ en haut à droite
2. Dans la sidebar gauche, naviguer vers **Apparence** → **Interface**
3. Le sélecteur "Thème de l'interface" affiche deux options : **Sombre** (par défaut) et **Clair**
4. Cliquer sur **Clair** :
   - L'arrière-plan de toute l'interface passe instantanément en blanc/gris clair
   - Les textes deviennent foncés
   - Les bordures s'allègent
5. Re-cliquer sur **Sombre** pour revenir au thème sombre
6. Fermer les paramètres puis les rouvrir : le dernier thème sélectionné est restauré

## Tester le changement de langue

1. Toujours dans **Apparence** → **Interface**
2. Le sélecteur "Langue d'affichage" contient 10 langues :
   - English
   - 中文（普通话）
   - हिन्दी
   - Español
   - Français
   - العربية
   - বাংলা
   - Português
   - Русский
   - اردو
3. Sélectionner une langue — les libellés traduits changent immédiatement
4. Fermer/rouvrir : la langue persiste

## Fichiers modifiés

| Fichier | Rôle |
|---------|------|
| `opencode-gui/src/main/resources/css/style-light.css` | Thème clair CSS |
| `opencode-gui/src/main/java/ai/opencode/gui/SettingsController.java` | applyTheme(), applyLanguage() |
| `opencode-gui/src/main/java/ai/opencode/gui/OpencodeGuiApp.java` | Chargement du thème initial |
| `opencode-gui/src/main/java/ai/opencode/gui/MainController.java` | Passe Scene au SettingsController |
| `opencode-gui/src/main/resources/fxml/settings_view.fxml` | Panneau Interface (sans boutons valider/annuler) |

## Configuration JSON résultante

Après avoir changé le thème et la langue, le fichier de configuration (`~/.config/opencode/config.json`) contient :

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
