package ai.opencode.core.orchestrator;

import ai.opencode.core.context.ContextManager;
import ai.opencode.core.llm.*;
import ai.opencode.tools.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReasoningLoopTest {
    @Mock private LLMClient llmClient;
    @Mock private ContextManager contextManager;
    @Mock private ToolRegistry toolRegistry;
    @Mock private Tool mockTool;

    private ReasoningLoop reasoningLoop;
    private final String systemPrompt = "System Prompt";

    @BeforeEach
    void setUp() {
        reasoningLoop = new ReasoningLoop(llmClient, contextManager, toolRegistry, systemPrompt);
    }

    @Test
    void testExecuteDirectResponse() throws Exception {
        // Le LLM répond directement sans utiliser d'outil
        when(llmClient.generateResponse(eq(systemPrompt), any()))
            .thenReturn("Voici la réponse finale.");
        when(contextManager.getOptimizedHistory()).thenReturn(Collections.emptyList());

        String result = reasoningLoop.execute(thought -> {});

        assertEquals("Voici la réponse finale.", result);
        verify(llmClient, times(1)).generateResponse(any(), any());
    }

    @Test
    void testExecuteWithToolCall() throws Exception {
        // 1. Le LLM demande l'utilisation d'un outil
        String toolCall = "Je vais chercher l'info.\nAction: read\nAction Input: file.txt";
        String finalResponse = "Le contenu est : Bonjour";
        
        when(contextManager.getOptimizedHistory()).thenReturn(Collections.emptyList());
        when(llmClient.generateResponse(eq(systemPrompt), any()))
            .thenReturn(toolCall) // Premier appel -> Action
            .thenReturn(finalResponse); // Second appel -> Réponse finale

        // Mock de l'outil
        when(toolRegistry.getTool("read")).thenReturn(mockTool);
        ToolResponse toolResponse = new ToolResponse("Contenu du fichier", new HashMap<>(), null);
        
        // Utilisation de doReturn pour éviter le problème d'exception checked (ToolException)
        Mockito.doReturn(toolResponse).when(mockTool).execute(any(), any());

        String result = reasoningLoop.execute(thought -> {});

        assertEquals(finalResponse, result);
        verify(llmClient, times(2)).generateResponse(any(), any());
        verify(mockTool, times(1)).execute(any(), any());
    }
}
