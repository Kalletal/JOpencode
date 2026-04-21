package ai.opencode.core.llm;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import java.util.*;
import java.util.stream.Collectors;
import java.time.Duration;

/**
 * Implémentation de LLMClient utilisant LangChain4j.
 * Cette classe gère la conversion entre les modèles de messages d'opencode et ceux de LangChain4j.
 */
public class LangChain4jLLMClient implements LLMClient {
    private final ChatLanguageModel model;

    public LangChain4jLLMClient(String apiKey, String modelName) {
        if (isLocalModel(modelName)) {
            this.model = OpenAiChatModel.builder()
                    .baseUrl("http://localhost:8080/v1")
                    .apiKey("local-no-key")
                    .modelName(modelName)
                    .timeout(Duration.ofSeconds(300))
                    .build();
        } else {
            this.model = OpenAiChatModel.builder()
                    .apiKey(apiKey == null || apiKey.isBlank() ? "dummy-key" : apiKey)
                    .modelName(modelName != null ? modelName : "gpt-4-turbo")
                    .timeout(Duration.ofSeconds(60))
                    .build();
        }
    }

    private boolean isLocalModel(String modelName) {
        if (modelName == null) return false;
        String m = modelName.toLowerCase();
        return m.contains("gemma") || m.contains("llama") || m.contains("mistral") || 
               m.contains("deepseek") || m.contains("qwen") || m.contains("phi");
    }

    @Override
    public String generateResponse(String systemPrompt, List<ChatMessage> messages) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("System: ").append(systemPrompt).append("\n\n");
        
        for (ChatMessage msg : messages) {
            prompt.append(msg.role()).append(": ").append(msg.content()).append("\n");
        }
        
        return model.generate(prompt.toString());
    }

    @Override
    public <T> T generateStructuredResponse(String systemPrompt, List<ChatMessage> messages, Class<T> responseClass) {
        throw new UnsupportedOperationException("La réponse structurée n'est pas encore implémentée.");
    }
}
