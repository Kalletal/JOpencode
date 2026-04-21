package ai.opencode.core.llm;

/**
 * Représente un message dans une conversation entre l'utilisateur, l'agent et les outils.
 */
public record ChatMessage(Role role, String content) {}
