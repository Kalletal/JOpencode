package ai.opencode.storage;

import java.util.Map;
import java.util.HashMap;

/**
 * Représente la configuration globale de l'application.
 * Ce record est utilisé pour mapper le fichier JSON de configuration.
 */
public record AppConfig(
    String username,
    String defaultAgent,
    Map<String, AgentConfig> agents,
    Map<String, String> apiKeys,
    Map<String, Object> experimental,
    String thinkingShortcut
) {
    public String getThinkingShortcut() {
        return thinkingShortcut;
    }

    /**
     * Configuration spécifique pour un agent.
     */
    public record AgentConfig(
        String name,
        String mode,
        String model,
        Map<String, String> permission
    ) {}

    /**
     * Retourne une configuration par défaut si le fichier est manquant ou corrompu.
     */
    public static AppConfig createDefault() {
        return new AppConfig(
            "User",
            "build",
            new HashMap<>(),
            new HashMap<>(),
            new HashMap<>(),
            "CTRL+T"
        );
    }
}
