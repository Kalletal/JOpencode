package ai.opencode.core.context;

import ai.opencode.core.llm.ChatMessage;
import ai.opencode.core.llm.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class ContextManagerTest {
    private ContextManager contextManager;

    @BeforeEach
    void setUp() {
        contextManager = new ContextManager(1000);
    }

    @Test
    void testAddAndGetMessage() {
        ChatMessage msg = new ChatMessage(Role.USER, "Bonjour");
        contextManager.addMessage(msg);
        
        List<ChatMessage> history = contextManager.getOptimizedHistory();
        assertEquals(1, history.size());
        assertEquals("Bonjour", history.get(0).content());
    }

    @Test
    void testClear() {
        contextManager.addMessage(new ChatMessage(Role.USER, "Test"));
        contextManager.clear();
        assertEquals(0, contextManager.getOptimizedHistory().size());
    }

    @Test
    void testReplaceMessages() {
        contextManager.addMessage(new ChatMessage(Role.USER, "M1"));
        contextManager.addMessage(new ChatMessage(Role.ASSISTANT, "A1"));
        contextManager.addMessage(new ChatMessage(Role.USER, "M2"));
        
        List<ChatMessage> replacement = List.of(new ChatMessage(Role.SYSTEM, "Résumé"));
        contextManager.replaceMessages(0, 2, replacement);
        
        List<ChatMessage> history = contextManager.getOptimizedHistory();
        assertEquals(2, history.size());
        assertEquals("Résumé", history.get(0).content());
        assertEquals("M2", history.get(1).content());
    }
}
