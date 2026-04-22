# AGENTS.md

Ne demande jamais de confirmation avant de lire, modifier, créer ou supprimer des fichiers.
Effectue toutes les modifications directement sans demander de validation.

Si une compilation échoue 2 fois de suite avec la même erreur, analyse le message d'erreur ligne par ligne avant de recompiler.

## Project Overview
Java refactor of OpenCode. Multi-module Maven project.

## Settings interface
Le menu de gauche, dans la sidebar, doit contenir 4 sections (Fournisseurs LLM, Admin, Compétences de l'agent, et Apparence). 
L'item "Fournisseurs LLM" doit avoir les sous-items "Préférences LLM" et "Voix et parole". 
L'item "Admin" doit avoir les sous items "Historique des discussions" et "Invite système par défaut". 
"Compétences de l'agent" n'a pas de sous-item pour l'instant. 
Et l'item "Apparence" a le sous-item "Interface" 
La couleur de l'arrière plan des items doit passer au gris clair quand on passe la souris dessus et revenir à sa couleur d'origine quand on sort de son champs. 
Il en est de même pour les sous items de chaque item. 
La largeur de la couleur d'arrière plan doit être précisément la même entre les items et les sous-items.
Une icône représentative de chacun des items doit être affiché devant eux, sur la même ligne.
Pas d'icône pour les sous-items.
Les items et les sous-items doivent être à la même distance du bord de l'interface.
Quand on clique sur un sous-item le texte doit passer en gras.
La taille de la police ne doit pas changer.
La taille de la police des items doit faire 14px et celles des sous-items 11px
L'icône devant chacun des items doit être centré verticalement sur chaque lignes d'item.

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
