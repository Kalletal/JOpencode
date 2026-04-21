package ai.opencode.tools;

import ai.opencode.storage.AppConfig;

/**
 * Interface racine pour tous les outils d'opencode.
 * Chaque outil doit implémenter sa propre logique d'exécution et fournir
 * une description claire pour que le LLM sache quand l'utiliser.
 */
public interface Tool {
    /**
     * Retourne l'identifiant unique de l'outil (ex: "bash", "read").
     */
    String getName();

    /**
     * Retourne la description de l'outil, utilisée dans le prompt système du LLM.
     */
    String getDescription();

    /**
     * Exécute la logique de l'outil.
     * 
     * @param request Les paramètres envoyés par l'agent.
     * @param config La configuration actuelle de l'application.
     * @return Le résultat de l'exécution.
     * @throws ToolException Si l'exécution échoue.
     */
    ToolResponse execute(ToolRequest request, AppConfig config) throws ToolException;
}
