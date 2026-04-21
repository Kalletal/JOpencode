package ai.opencode.core.orchestrator;

import ai.opencode.core.context.ContextManager;
import ai.opencode.core.llm.*;
import ai.opencode.core.session.SessionManager;
import ai.opencode.tools.ToolRegistry;
import java.util.*;
import java.util.logging.Logger;

/**
 * Orchestrateur central de l'agent opencode.
 * Il coordonne les interactions entre l'utilisateur, le LLM, les outils et la persistance.
 */
public class AgentOrchestrator {
    private static final Logger LOGGER = Logger.getLogger(AgentOrchestrator.class.getName());
    
    private final LLMClient llmClient;
    private final SessionManager sessionManager;
    private final ToolRegistry toolRegistry;
    private final ContextManager contextManager;
    private String sessionId;
    private String currentSystemPrompt;
    private String currentAgentId;
    private final Map<String, String> agentPrompts;

    public AgentOrchestrator(LLMClient llmClient, SessionManager sessionManager, 
                             ToolRegistry toolRegistry, String sessionId, String defaultAgentId) {
        this.llmClient = llmClient;
        this.sessionManager = sessionManager;
        this.toolRegistry = toolRegistry;
        this.sessionId = sessionId;
        this.currentAgentId = defaultAgentId;
        this.contextManager = new ContextManager(4096);
        
        // Définition des prompts système par défaut pour les différents modes
        this.agentPrompts = new HashMap<>();
        String thinkingInstruction = "\n\nIMPORTANT: Before providing your final answer, you MUST first reason through the problem inside <think> tags. " +
                                     "Your internal reasoning should be detailed and analytical. " +
                                     "Format your response exactly as: <think>Your internal reasoning here</think>Final Answer";
        
        agentPrompts.put("plan", "You are Opencode Plan. Your goal is to analyze requests and create a high-level technical plan." + thinkingInstruction);
        agentPrompts.put("build", "You are Opencode Build. Your goal is to implement the technical plan and write the code." + thinkingInstruction);
        agentPrompts.put("custom", "You are a custom custom Opencode agent. Adapt your behavior to the user's needs." + thinkingInstruction);
        
        updateSystemPrompt();
        if (this.sessionId != null) {
            loadSessionHistory();
        }
    }

    public void createNewSession(String title) {
        this.sessionId = sessionManager.createSession(title);
    }

    public void restoreSession(String sessionId) {
        this.sessionId = sessionId;
        this.contextManager.clear();
        loadSessionHistory();
    }

    private void updateSystemPrompt() {
        this.currentSystemPrompt = agentPrompts.getOrDefault(currentAgentId.toLowerCase(), agentPrompts.get("custom"));
    }

    public void switchAgent(String agentId) {
        this.currentAgentId = agentId;
        updateSystemPrompt();
        LOGGER.info("Agent commuté vers : " + agentId);
    }

    public String getCurrentAgentId() {
        return currentAgentId;
    }

    public String getCurrentAgentName() {
        return currentAgentId == null ? "Unknown" : currentAgentId.toUpperCase();
    }

    private void loadSessionHistory() {
        List<SessionManager.MessageRecord> history = sessionManager.getSessionHistory(sessionId);
        for (SessionManager.MessageRecord record : history) {
            Role role = Role.valueOf(record.role().toUpperCase());
            contextManager.addMessage(new ChatMessage(role, record.content()));
        }
    }

    public String processRequest(String userMessage, java.util.function.Consumer<String> thoughtConsumer) {
        if (sessionId == null) {
            return "Error: No active session. Please start a new chat first.";
        }

        if ("Nouvelle Session".equals(getSessionTitle())) {
            String title = generateAiTitle(userMessage);
            updateSessionTitle(title);
        }
        
        ChatMessage message = new ChatMessage(Role.USER, userMessage);
        contextManager.addMessage(message);
        sessionManager.saveMessage(sessionId, Role.USER.name(), userMessage);
        
        ReasoningLoop loop = new ReasoningLoop(llmClient, contextManager, toolRegistry, currentSystemPrompt);
        String finalResponse = loop.execute(thoughtConsumer);
        
        contextManager.addMessage(new ChatMessage(Role.ASSISTANT, finalResponse));
        sessionManager.saveMessage(sessionId, Role.ASSISTANT.name(), finalResponse);
        
        return finalResponse;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getSessionTitle() {
        if (sessionId == null) return "";
        return sessionManager.getAllSessions().stream()
                .filter(s -> s.id().equals(sessionId))
                .map(ai.opencode.core.session.SessionManager.SessionRecord::title)
                .findFirst()
                .orElse("");
    }

    public void updateSessionTitle(String newTitle) {
        if (sessionId != null) {
            sessionManager.updateSessionTitle(sessionId, newTitle);
        }
    }

    private String generateAiTitle(String userMessage) {
        try {
            String prompt = "Create a concise 3-5 word title for a chat session based on this first message: '" + userMessage + "'. " +
                           "Avoid greetings (like 'Hello' or 'Hi'). Return ONLY the title, nothing else.";
            // Use a simple one-shot request to the LLM
            return llmClient.generateResponse("You are a helpful assistant that summarizes chat sessions into short titles.", 
                                               Collections.singletonList(new ChatMessage(Role.USER, prompt))).trim();
        } catch (Exception e) {
            LOGGER.severe("Failed to generate AI title: " + e.getMessage());
            return userMessage.length() > 30 ? userMessage.substring(0, 27) + "..." : userMessage;
        }
    }

    public ai.opencode.core.context.ContextManager getContextManager() {
        return contextManager;
    }
}
