package ai.opencode.core.project;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

/**
 * Service pour analyser le projet actuel et générer le fichier AGENTS.md.
 */
public class ProjectInitializer {
    private static final String AGENTS_FILE = "AGENTS.md";

    /**
     * Analyse le projet et met à jour le fichier AGENTS.md.
     * @return Le contenu généré ou un message d'erreur.
     * @throws IOException Si l'accès au système de fichiers échoue.
     */
    public String initializeProject() throws IOException {
        String projectRoot = System.getProperty("user.dir");
        Path rootPath = Paths.get(projectRoot);
        
        StringBuilder analysis = new StringBuilder();
        analysis.append("# Project Analysis: ").append(rootPath.getFileName()).append("\n\n");
        
        // 1. Détection du stack technique
        String techStack = detectTechStack(rootPath);
        analysis.append("## Tech Stack\n").append(techStack).append("\n\n");
        
        // 2. Structure des dossiers
        analysis.append("## Project Structure\n");
        analysis.append(listMainDirectories(rootPath)).append("\n\n");
        
        // 3. Résumé du README
        String readmeSummary = summarizeReadme(rootPath);
        if (!readmeSummary.isEmpty()) {
            analysis.append("## Project Description\n").append(readmeSummary).append("\n\n");
        }
        
        // Construction du contenu final pour AGENTS.md
        StringBuilder agentsContent = new StringBuilder();
        agentsContent.append("# AGENTS.md\n\n");
        agentsContent.append("This file contains project-specific instructions for the Opencode agents.\n\n");
        agentsContent.append(analysis.toString());
        
        agentsContent.append("--- \n\n");
        agentsContent.append("## Agent Instructions\n\n");
        agentsContent.append("### Plan Agent\n- Analyze the project structure and technical requirements.\n- Create detailed implementation plans.\n\n");
        agentsContent.append("### Build Agent\n- Implement the agreed-upon plans.\n- Follow the project's coding conventions.\n\n");
        agentsContent.append("### Custom Agent\n- Adapt to specific user needs for this project.\n");
        
        Files.writeString(rootPath.resolve(AGENTS_FILE), agentsContent.toString());
        
        return "Project initialized successfully. AGENTS.md has been created/updated in " + projectRoot;
    }

    private String detectTechStack(Path root) {
        List<String> stack = new ArrayList<>();
        if (Files.exists(root.resolve("pom.xml"))) stack.add("Java (Maven)");
        if (Files.exists(root.resolve("build.gradle"))) stack.add("Java/Kotlin (Gradle)");
        if (Files.exists(root.resolve("package.json"))) stack.add("JavaScript/TypeScript (Node.js)");
        if (Files.exists(root.resolve("requirements.txt")) || Files.exists(root.resolve("pyproject.toml"))) stack.add("Python");
        if (Files.exists(root.resolve("Cargo.toml"))) stack.add("Rust");
        if (Files.exists(root.resolve("go.mod"))) stack.add("Go");
        
        return stack.isEmpty() ? "Unknown" : String.join(", ", stack);
    }

    private String listMainDirectories(Path root) {
        try (Stream<Path> stream = Files.list(root)) {
            return stream
                .filter(Files::isDirectory)
                .filter(path -> !path.getFileName().toString().startsWith("."))
                .map(path -> "- " + path.getFileName().toString())
                .collect(Collectors.joining("\n"));
        } catch (IOException e) {
            return "Could not list directories";
        }
    }

    private String summarizeReadme(Path root) {
        Path readme = root.resolve("README.md");
        if (Files.exists(readme)) {
            try {
                List<String> lines = Files.readAllLines(readme);
                if (lines.isEmpty()) return "";
                // Take first 5 lines as a summary
                return lines.stream().limit(5).collect(Collectors.joining("\n"));
            } catch (IOException e) {
                return "Could not read README.md";
            }
        }
        return "";
    }
}
