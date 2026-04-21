package ai.opencode.core.integration;

import ai.opencode.core.context.ContextManager;
import ai.opencode.core.llm.ChatMessage;
import ai.opencode.core.llm.Role;
import ai.opencode.core.session.SessionManager;
import ai.opencode.storage.DatabaseManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class SessionIntegrationTest {
    private DatabaseManager dbManager;
    private SessionManager sessionManager;
    private ContextManager contextManager;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        // Utilisation d'une base SQLite en mémoire ou dans le dossier temporaire pour les tests
        // Note : DatabaseManager devrait idéalement accepter un chemin de fichier en constructeur
        // Pour ce test, on suppose qu'il utilise le chemin par défaut ou on surcharge via System properties
        dbManager = DatabaseManager.getInstance(); 
        sessionManager = new SessionManager(dbManager);
        contextManager = new ContextManager(4096);
    }

    @Test
    void testSessionPersistenceCycle() {
        // 1. Créer une session
        String title = "Test Integration Session";
        String sessionId = sessionManager.createSession(title);
        assertNotNull(sessionId);

        // 2. Sauvegarder des messages
        String userMsg = "Hello integration!";
        String assistantMsg = "Hello back!";
        sessionManager.saveMessage(sessionId, Role.USER.name(), userMsg);
        sessionManager.saveMessage(sessionId, Role.ASSISTANT.name(), assistantMsg);

        // 3. Simuler un redémarrage en rechargeant l'historique dans un nouveau ContextManager
        List<SessionManager.MessageRecord> records = sessionManager.getSessionHistory(sessionId);
        assertEquals(2, records.size());

        for (SessionManager.MessageRecord record : records) {
            contextManager.addMessage(new ChatMessage(Role.valueOf(record.role()), record.content()));
        }

        // 4. Vérifier que le contexte est correctement reconstruit
        List<ChatMessage> history = contextManager.getOptimizedHistory();
        assertEquals(2, history.size());
        assertEquals(userMsg, history.get(0).content());
        assertEquals(assistantMsg, history.get(1).content());
    }
}
