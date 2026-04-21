package ai.opencode.core.context;

import ai.opencode.core.llm.ChatMessage;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Gère la mémoire de la conversation pour un agent.
 * Ce manager s'assure que le volume de données envoyées au LLM ne dépasse pas
 * la fenêtre de contexte disponible (Context Window).
 */
public class ContextManager {
    private final List<ChatMessage> history = new ArrayList<>();
    private final int maxContextTokens;

    public ContextManager(int maxContextTokens) {
        this.maxContextTokens = maxContextTokens;
    }

    public void addMessage(ChatMessage message) {
        history.add(message);
    }

    public int getTotalTokens() {
        return history.stream()
                .mapToInt(msg -> (int) (msg.content().length() / 4.0))
                .sum();
    }

    public double getUsagePercent() {
        return (double) getTotalTokens() / maxContextTokens * 100.0;
    }

    public List<ChatMessage> getOptimizedHistory() {

        // Pour l'instant, implémentation simple : on retourne tout.
        // Plus tard, on ajoutera ici la logique de compression (Compactor).
        return new ArrayList<>(history);
    }

    /**
     * Vide l'historique pour recommencer une nouvelle session.
     */
    public void clear() {
        history.clear();
    }

    /**
     * Permet d'insérer des messages à un endroit spécifique (utile pour le résumé).
     */
    public void replaceMessages(int start, int end, List<ChatMessage> replacement) {
        if (start < 0 || end > history.size() || start > end) {
            throw new IllegalArgumentException("Indices de remplacement invalides");
        }
        // Supprime la plage et insère le remplacement
        for (int i = 0; i < (end - start); i++) {
            history.remove(start);
        }
        history.addAll(start, replacement);
    }
}
