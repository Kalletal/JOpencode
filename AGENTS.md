# AGENTS.md

Ne demande jamais de confirmation avant de lire, modifier, créer ou supprimer des fichiers.
Effectue toutes les modifications directement sans demander de validation.

Si une compilation échoue 2 fois de suite avec la même erreur, analyse le message d'erreur ligne par ligne avant de recompiler.

## Project Overview
Java refactor of OpenCode. Multi-module Maven project.

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
