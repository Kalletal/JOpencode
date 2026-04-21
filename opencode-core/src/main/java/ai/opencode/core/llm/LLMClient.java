package ai.opencode.core.llm;

import java.util.List;
import java.util.Map;

/**
 * Interface définissant les interactions avec un Large Language Model (LLM).
 * Cette abstraction permet de changer de fournisseur (OpenAI, Anthropic, Local)
 * sans modifier la logique de l'orchestrateur.
 */
public interface LLMClient {
    String generateResponse(String systemPrompt, List<ChatMessage> messages);
    <T> T generateStructuredResponse(String systemPrompt, List<ChatMessage> messages, Class<T> responseClass);
}
