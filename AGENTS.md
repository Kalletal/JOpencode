# AGENTS.md

Ne demande jamais de confirmation avant de lire, modifier, créer ou supprimer des fichiers.
Effectue toutes les modifications directement sans demander de validation.

Si une compilation échoue 2 fois de suite avec la même erreur, analyse le message d'erreur ligne par ligne avant de recompiler.

## Project Overview
Java refactor of OpenCode. Multi-module Maven project.

## Settings interface
1. Le menu de gauche, dans la sidebar, doit contenir 4 sections (Fournisseurs LLM, Admin, Compétences de l'agent, et Apparence). > OK
2. L'item "Fournisseurs LLM" doit avoir les sous-items "Préférences LLM" et "Voix et parole". > OK
3. L'item "Admin" doit avoir les sous items "Historique des discussions" et "Invite système par défaut". > OK 
4. "Compétences de l'agent" n'a pas de sous-item pour l'instant.  > OK
5. Et l'item "Apparence" a le sous-item "Interface"  > OK
6. La couleur de l'arrière plan des items doit passer au gris clair quand on passe la souris dessus et revenir à sa couleur d'origine quand on sort de son champs. > OK
7. Il en est de même pour les sous items de chaque item. > OK
8. La largeur de la couleur d'arrière plan doit être précisément la même entre les items et les sous-items. > OK
9. Une icône représentative de chacun des items doit être affiché devant eux, sur la même ligne.
10. Pas d'icône pour les sous-items. > OK
11. Les items et les sous-items doivent être à la même distance du bord gauche de l'interface et donc être aligné verticalement.
12. Quand on clique sur un sous-item le texte doit passer en gras. > OK
13. Et quand on clique sur un autre sous-items, celui que l'ont sélectionne passe en gras et celui d'avant revient en caractère fin. > OK
14. La taille de la police ne doit pas changer. > OK
15. La taille de la police des items doit faire 14px et celles des sous-items 11px > OK
16. L'icône devant chacun des items doit être centré verticalement sur chaque lignes d'item.

## Solution implémentée : VBox accordion avec icônes inline

Structure actuelle du menu sidebar : structure `VBox` accordion personnalisée dans `settings_view.fxml`, construite manuellement avec des `HBox`/`VBox` imbriqués. Chaque section a un header VBox cliquable + un submenu HBox pliable avec animation Timeline.

### Directive 1 : 4 sections avec sous-items
Chaque section parente contient ses sous-items dans un HBox pliable :
- **Fournisseurs LLM** → "Préférences LLM" + "Voix et parole"
- **Admin** → "Historique des chats" + "Invite système par défaut"  
- **Compétences de l'agent** → aucun sous-item pour l'instant
- **Apparence** → "Interface"

Les sous-sections utilisent `HBox.fx:id="submenuLLM"` etc., avec clip Rectangle + animation maxHeight pour le pliage/dépliage.

### Directive 2 : Hover background gris clair (#2a2a2a) pleine largeur identique
- Les headers VBox reçoivent `-fx-background-color: transparent; -fx-background-radius: 6`. Le hover est géré par CSS `.sidebar-menu > VBox:hover { -fx-background-color: #2a2a2a !important }` ET les méthodes JavaFX `handleMenuHover()` / `handleMenuHoverExit()`.
- Les subitems Labels ont la classe CSS `.subitem` avec règle `.subitem:hover { -fx-background-color: #2a2a2a !important }`.
- **Largeur identique** : les subitems doivent avoir la même largeur visuelle que les headers. Comme les headers n'ont pas de largeur explicite (ils s'étirent sur toute la largeur du VBox parent), il faut aligner horizontalement le texte des subitems avec celui des headers. Voir directive 4 pour le calcul d'alignement.

### Directive 3 : Icônes PNG inline devant chaque item parent
Chaque header de section doit afficher une icône PNG à gauche du texte, sur la même ligne, centrée verticalement. Fichiers disponibles dans `/fxml/icons/` :
- `llm.png` → section "Fournisseurs LLM"
- `admin.png` → section "Admin"
- `agent_skills.png` → section "Compétences de l'agent"
- `appearance.png` → section "Apparence"

Implémentation FXML dans chaque header HBox : ajouter `<ImageView fx:id="iconLLM" fitWidth="16" fitHeight="16" pickOnBounds="true" preserveRatio="true"><image><Image url="@icons/llm.png"/></image></ImageView>` AVANT le Label de texte, avec `-fx-padding: 0 4 0 8` sur l'HBox pour espacer l'icône du texte.

L'`HBox.alignment="CENTER_LEFT"` assure automatiquement le centrage vertical (directive 10).

### Directive 4 : Alignement horizontal items/sous-items (même distance du bord)
**Calcul d'alignement :**
- Header : VBox padding(8) + inner HBox padding(8) + ImageView width(16) + label padding gauche(4) = **36px** du bord interne du menuContainer.
- Subitem : HBox padding(8) + spacer Region(24) + label padding gauche(4) = **36px** du bord interne.
- Le spacer `<Region prefWidth="24" style="-fx-background-color: transparent;"/>` compense exactement la double couche de padding du header (VBox + HBox imbriqué = 16px) moins l'image (16px). Les subitems n'ont PAS d'ImageView mais ont ce spacer pour aligner leur texte au même niveau X que les textes des headers.
- Dans FXML, chaque submenu HBox contient en première position un `<Region prefWidth="24">`, suivi d'un VBox qui empile verticalement les Labels des sous-items.

### Directive 8 : Hover background plein largeur — Structure HBox cascade

**Problème :** JavaFX VBox ne respecte pas `hgrow`, et un enfant VBox rétrécit à la taille de son contenu. Les Labels subitems restent donc collés au texte, le hover CSS ne couvre qu'eux-mêmes.

**Solution :** Cascade d'HBox avec `hgrow="ALWAYS"` et `maxWidth="Infinity"` à **chaque niveau** :

```xml
<HBox fx:id="submenuLLM" style="-fx-padding: 2 0;">
    <children>
        <!-- Niveau 1 : VBox enfant — étiré dans son parent HBox -->
        <VBox fx:id="vboxLLMChildren" spacing="2"
              HBox.hgrow="ALWAYS" maxWidth="Infinity">
            <children>
                <!-- Niveau 2 : HBox wrapper — étiré dans VBox -->
                <HBox HBox.hgrow="ALWAYS" maxWidth="Infinity"
                      prefHeight="35" styleClass="submenu-row" alignment="CENTER_LEFT">
                    <!-- Niveau 3 : Label — étiré dans HBox -->
                    <Label fx:id="btnLLMPreference" text="Préférence LLM"
                           styleClass="subitem"
                           style="-fx-text-fill: #cccccc; -fx-font-size: 11;
                                  -fx-padding: 5 10 5 32; -fx-cursor: hand;
                                  -fx-max-width: Infinity;"
                           maxWidth="Infinity" HBox.hgrow="ALWAYS"/>
                </HBox>
            </children>
        </VBox>
    </children>
</HBox>
```

**CSS :** Le hover s'applique sur le wrapper `.submenu-row`, pas sur le Label :
```css
.submenu-row { -fx-background-color: transparent; }
.submenu-row:hover { -fx-background-color: #2a2a2a !important; }
```

**Points clés :**
- Chaque niveau doit avoir `HBox.hgrow="ALWAYS"` + `maxWidth="Infinity"` sinon la cascade se brise.
- Le padding droit `32` du Label compense l'espace vide à droite (équivalent au spacer de header).
- Les Regions `bgLLM`/`bgAdmin`/`bgAppearance` sont supprimées — inutiles avec cette approche.
- L'alignement horizontal est conservé via le padding droit du Label (`5 10 5 32`).

### Directive 5 : Pas d'icône pour les sous-items
Les Labels `.subitem` n'ont pas d'ImageView. C'est déjà le cas actuellement.

### Directive 6 : Texte en gras sur sélection, retour en fin sur désélection
La classe CSS `.menu-item-active` applique `-fx-font-weight: bold` via `settings_menu.css`. La méthode `highlightMenuItem()` dans SettingsController retire cette classe de tous les autres sous-items et l'ajoute uniquement au sous-item cliqué. La taille de police ne change pas (directive 9).

### Directive 7 : Tailles de police — items 14px / sous-items 11px
- Headers : `-fx-font-size: 14` sur le Label de texte dans chaque header HBox.
- Subitems : `-fx-font-size: 11` sur chaque Label `.subitem`.
- Ces tailles sont fixes, la sélection en gras ne modifie PAS la taille.

## Fichiers clés
- `opencode-gui/src/main/resources/fxml/settings_view.fxml` — Structure VBox accordion + submenus
- `opencode-gui/src/main/java/ai/opencode/gui/SettingsController.java` — Toggle accordion, hover, highlight
- `opencode-gui/src/main/resources/fxml/settings_menu.css` — Styles hover, subitem, menu-item-active
- `opencode-gui/src/main/resources/fxml/icons/llm.png`, `admin.png`, `agent_skills.png`, `appearance.png` — Icônes PNG 16x16

## Tech Stack
- **Java:** 21
- **Build Tool:** Maven
- **LLM Orchestration:** LangChain4j (ReAct-style reasoning)
- **GUI:** JavaFX
- **Database:** SQLite
- **Testing:** JUnit 5, Mockito

## Modules & Architecture
- `opencode-core`: Core logic (Orchestrator, LLM Client, Context, Session).
- Reasoning follows a "Thought -> Action -> Observation" pattern.
- Utilise exclusivement les outils natifs d'opencode (bash, read, edit, write, glob, grep).
- Ne jamais utiliser le format Action:/Action Input: ni les balises <think>.
- Depends on `opencode-tools` and `opencode-storage`.
- `opencode-tools`: Implementation of agent tools (Bash, Read, etc.). Depends on `opencode-storage`.
- `opencode-gui`: JavaFX application. Entry point: `ai.opencode.gui.OpencodeGuiApp`. Depends on `opencode-core`.
- `opencode-storage`: Configuration and SQLite database management.

## Common Commands
- **Build all modules:** `mvn clean install` (from root)
- **Run tests:** `mvn test`
- **Run GUI:** `mvn javafx:run` (from `opencode-gui` directory)

## Development Gotchas
- Ensure Java 21 is installed and configured.
- When adding new tools, they must be registered in `opencode-tools` via `ToolRegistry`.
- The agent's reasoning loop expects specific output formats for tool calling (`Action:` and `Action Input:`).

## Active Technologies
- Java 21 + JavaFX (GUI), Maven multi-module (001-appearance-settings)
- Fichier JSON local via `ConfigManager` (`opencode-storage`) — clé `experimental.theme` et `experimental.language` dans `AppConfig.experimental()` (001-appearance-settings)
- Java 21 + JavaFX 21 (GUI framework), FXML (UI definition), Maven multi-module build (001-appearance-settings)
- SQLite (via opencode-storage module) — non concerné par cette feature (001-appearance-settings)

## Recent Changes
- 001-appearance-settings: Added Java 21 + JavaFX (GUI), Maven multi-module
