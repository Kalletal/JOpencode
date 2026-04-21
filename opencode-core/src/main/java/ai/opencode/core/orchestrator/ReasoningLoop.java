package ai.opencode.core.orchestrator;

import ai.opencode.core.context.ContextManager;
import ai.opencode.core.llm.*;
import ai.opencode.tools.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.*;
import java.util.logging.Logger;

/**
 * Implémente la boucle de raisonnement "Pensée -> Action -> Observation".
 * Elle pilote l'interaction itérative avec le LLM jusqu'à l'obtention d'une réponse finale.
 */
public class ReasoningLoop {
    private static final Logger LOGGER = Logger.getLogger(ReasoningLoop.class.getName());
    
    private final LLMClient llmClient;
    private final ContextManager contextManager;
    private final ToolRegistry toolRegistry;
    private final String systemPrompt;
    
    // Patterns pour parser les appels d'outils (format inspiré de ReAct)
    private static final Pattern ACTION_PATTERN = Pattern.compile("Action:\\s*(\\w+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern INPUT_PATTERN = Pattern.compile("Action Input:\\s*(.*)", Pattern.CASE_INSENSITIVE);

    public ReasoningLoop(LLMClient llmClient, ContextManager contextManager, ToolRegistry toolRegistry, String systemPrompt) {
        this.llmClient = llmClient;
        this.contextManager = contextManager;
        this.toolRegistry = toolRegistry;
        this.systemPrompt = systemPrompt;
    }

    /**
     * Exécute la boucle de raisonnement.
     * 
     * @param thoughtConsumer Callback pour notifier l'UI des étapes de réflexion.
     * @return La réponse finale de l'agent.
     */
    public String execute(Consumer<String> thoughtConsumer) {
        int iterations = 0;
        int maxIterations = 10;
        String lastResponse = "";

        while (iterations < maxIterations) {
            iterations++;
            
            List<ChatMessage> history = contextManager.getOptimizedHistory();
            String response = llmClient.generateResponse(systemPrompt, history);
            lastResponse = response;

            LOGGER.info("Raw LLM response: " + response);

            // Extraire la partie "pensée" (tout ce qui précède l'Action ou la totalité si pas d'action)
            String thought = extractThought(response);
            LOGGER.info("Thought extracted: " + (thought.isEmpty() ? "EMPTY" : thought.substring(0, Math.min(thought.length(), 50)) + "..."));
            if (thoughtConsumer != null && !thought.isEmpty()) {
                thoughtConsumer.accept(thought);
            }

            Matcher actionMatcher = ACTION_PATTERN.matcher(response);
            Matcher inputMatcher = INPUT_PATTERN.matcher(response);

            if (actionMatcher.find() && inputMatcher.find()) {
                String toolName = actionMatcher.group(1);
                String toolInput = inputMatcher.group(1).trim();

                LOGGER.info("Appel de l'outil : " + toolName + " avec l'input : " + toolInput);

                String observation = executeTool(toolName, toolInput);

                contextManager.addMessage(new ChatMessage(Role.ASSISTANT, response));
                contextManager.addMessage(new ChatMessage(Role.TOOL, "Observation: " + observation));
                
                LOGGER.info("Observation reçue : " + observation);
            } else {
                return response;
            }
        }

        return "Erreur : Limite d'itérations atteinte sans réponse finale. Dernière réponse : " + lastResponse;
    }

    private String extractThought(String response) {
        // 1. Extraction des balises <think> (DeepSeek, etc.)
        Pattern thinkPattern = Pattern.compile("<think>(.*?)</think>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher thinkMatcher = thinkPattern.matcher(response);
        if (thinkMatcher.find()) {
            return thinkMatcher.group(1).trim();
        }

        // 2. Extraction avant "Action:" (ReAct)
        int actionIndex = response.toUpperCase().indexOf("ACTION:");
        if (actionIndex != -1) {
            return response.substring(0, actionIndex).trim();
        }

        // 3. Extraction si on voit des indicateurs de réflexion courants
        if (response.toLowerCase().contains("i think") || response.toLowerCase().contains("reasoning:")) {
            int reasoningIndex = response.toLowerCase().indexOf("reasoning:");
            if (reasoningIndex != -1) {
                return response.substring(reasoningIndex).trim();
            }
        }

        return "";
    }

    private String executeTool(String toolName, String input) {
        try {
            Tool tool = toolRegistry.getTool(toolName);
            if (tool == null) {
                return "Erreur : L'outil '" + toolName + "' n'existe pas.";
            }

            Map<String, Object> params = new HashMap<>();
            params.put("input", input);
            
            ToolRequest request = new ToolRequest(params, "Appel de " + toolName);
            ToolResponse response = tool.execute(request, null);
            return response != null ? response.output() : "L'outil n'a renvoyé aucun résultat.";
        } catch (Exception e) {
            LOGGER.severe("Erreur lors de l'exécution de l'outil " + toolName + ": " + e.getMessage());
            return "Erreur technique lors de l'exécution de l'outil : " + e.getMessage();
        }
    }
}
