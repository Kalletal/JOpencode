package ai.opencode.core.integration;

import ai.opencode.core.context.ContextManager;
import ai.opencode.core.llm.*;
import ai.opencode.core.orchestrator.*;
import ai.opencode.core.session.SessionManager;
import ai.opencode.storage.DatabaseManager;
import ai.opencode.tools.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class OrchestrationIntegrationTest {
    private AgentOrchestrator orchestrator;
    private LLMClient mockLlmClient;
    private ToolRegistry toolRegistry;
    private SessionManager sessionManager;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        DatabaseManager dbManager = DatabaseManager.getInstance();
        sessionManager = new SessionManager(dbManager);
        toolRegistry = new ToolRegistry();
        
        // Enregistrement des outils réels pour le test d'intégration
        toolRegistry.registerTool(new ReadTool());
        toolRegistry.registerTool(new BashTool());
        
        mockLlmClient = Mockito.mock(LLMClient.class);
        
        String sessionId = sessionManager.createSession("Integration Test");
        orchestrator = new AgentOrchestrator(
            mockLlmClient, sessionManager, toolRegistry, sessionId, "Tu es un assistant."
        );
    }

    @Test
    void testFullReasoningCycleWithRealTool() throws IOException {
        // 1. Créer un fichier réel pour que le ReadTool puisse le lire
        Path testFile = tempDir.resolve("integration_test.txt");
        Files.writeString(testFile, "Ceci est le contenu secret du fichier.");

        // 2. Configurer le LLM pour demander la lecture de ce fichier
        String toolCall = "Je vais lire le fichier.\nAction: read\nAction Input: " + testFile.toString();
        String finalResponse = "Le fichier contient : Ceci est le contenu secret du fichier.";
        
        when(mockLlmClient.generateResponse(anyString(), anyList()))
            .thenReturn(toolCall)
            .thenReturn(finalResponse);

        // 3. Lancer la requête
        String result = orchestrator.processRequest("Peux-tu lire le fichier " + testFile.getFileName(), thought -> {});

        // 4. Vérifications
        assertEquals(finalResponse, result);
        
        // Vérifier que le LLM a été appelé deux fois (Action puis Réponse)
        verify(mockLlmClient, times(2)).generateResponse(anyString(), anyList());
        
        // Vérifier que le message final est bien enregistré en DB
        List<SessionManager.MessageRecord> history = sessionManager.getSessionHistory(orchestrator.getSessionId());
        assertTrue(history.stream().anyMatch(m -> m.content().equals(finalResponse)));
    }
}
