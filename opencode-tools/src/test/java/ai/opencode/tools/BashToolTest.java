package ai.opencode.tools;

import ai.opencode.storage.AppConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

public class BashToolTest {
    private BashTool bashTool;
    private AppConfig mockConfig;

    @BeforeEach
    void setUp() {
        bashTool = new BashTool();
        mockConfig = AppConfig.createDefault();
    }

    @Test
    void testExecuteSimpleCommand() throws ToolException {
        ToolRequest request = new ToolRequest(Map.of("command", "echo 'Hello'"), "Test echo");
        ToolResponse response = bashTool.execute(request, mockConfig);

        assertTrue(response.output().contains("Hello"));
    }

    @Test
    void testExecuteFailingCommand() throws ToolException {
        // Commande qui échoue (cat un fichier inexistant)
        ToolRequest request = new ToolRequest(Map.of("command", "cat non_existent_file_12345.txt"), "Test fail");
        ToolResponse response = bashTool.execute(request, mockConfig);

        // Le BashTool capture généralement l'erreur dans l'output ou lève une ToolException selon l'implémentation
        // Ici, on vérifie que la réponse contient un message d'erreur ou que la commande a échoué
        assertTrue(response.output().contains("No such file") || response.output().contains("erreur"));
    }
}
