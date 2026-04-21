package ai.opencode.tools;

import java.util.Map;

/**
 * Encapsule la requête envoyée par l'agent pour utiliser un outil.
 * Les paramètres sont dynamiques pour s'adapter à chaque outil (ex: filePath pour 'read').
 */
public record ToolRequest(
    Map<String, Object> parameters,
    String description
) {}
