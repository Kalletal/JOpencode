package ai.opencode.gui;

import ai.opencode.core.llm.LangChain4jLLMClient;
import ai.opencode.core.orchestrator.AgentOrchestrator;
import ai.opencode.core.session.SessionManager;
import ai.opencode.storage.DatabaseManager;
import ai.opencode.tools.ToolRegistry;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Point d'entrée principal de l'application graphique opencode.
 */
public class OpencodeGuiApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // 1. Initialisation des composants core (en mode simplifié pour le lancement)
            ai.opencode.storage.ConfigManager configManager = new ai.opencode.storage.ConfigManager();
            DatabaseManager dbManager = DatabaseManager.getInstance();
            SessionManager sessionManager = new SessionManager(dbManager);
            ToolRegistry toolRegistry = new ToolRegistry();
            
            // Note : La clé API provient désormais de AppConfig
            String apiKey = configManager.getConfig().apiKeys() != null 
                ? configManager.getConfig().apiKeys().getOrDefault("openai", "YOUR_API_KEY") 
                : "YOUR_API_KEY";
            
            String defaultModel = "gpt-4-turbo";
            if (configManager.getConfig().agents() != null && !configManager.getConfig().agents().isEmpty()) {
                defaultModel = configManager.getConfig().agents().values().iterator().next().model();
            }
            
            LangChain4jLLMClient llmClient = new LangChain4jLLMClient(apiKey, defaultModel);
            
            String lastSessionId = null;
            var sessions = sessionManager.getAllSessions();
            if (!sessions.isEmpty()) {
                lastSessionId = sessions.get(0).id();
            }

            AgentOrchestrator orchestrator = new AgentOrchestrator(
                llmClient, sessionManager, toolRegistry, lastSessionId, "Tu es l'assistant opencode, un expert en logiciel."
            );

            // 2. Chargement de l'interface FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main_window.fxml"));
            Parent root = loader.load();
            
            MainController controller = loader.getController();
            controller.setOrchestrator(orchestrator);
            controller.setConfigManager(configManager);
            controller.setupDragAndDrop();

            // 3. Configuration de la scène et du style
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

            primaryStage.setTitle("opencode - Java Edition");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
