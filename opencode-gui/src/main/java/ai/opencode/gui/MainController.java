package ai.opencode.gui;

import ai.opencode.core.orchestrator.AgentOrchestrator;
import ai.opencode.core.project.ProjectInitializer;
import ai.opencode.gui.components.WelcomeTitle;
import javafx.scene.text.Font;
import javafx.scene.paint.Color;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.input.*;
import javafx.scene.text.Text;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.geometry.Side;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

/**
 * Contrôleur principal de l'interface graphique.
 * Gère les interactions utilisateur et la communication avec l'orchestrateur.
 */
public class MainController {
    private static final Logger LOGGER = Logger.getLogger(MainController.class.getName());

    @FXML private VBox chatMessages;
    @FXML private VBox chatView;
    @FXML private ScrollPane chatScrollPane;
    @FXML private TextArea userInput;
    @FXML private Label agentLabel;
    @FXML private Label modelLabel;
    @FXML private Label serverLabel;
    @FXML private Label projectNameLabel;
    @FXML private Label tokensLabel;
    @FXML private Label usageLabel;
    @FXML private Label costLabel;
    @FXML private VBox mcpServersList;
    @FXML private VBox lspServersList;
    @FXML private VBox contextList;
    @FXML private VBox sessionHistoryList;
    @FXML private StackPane welcomeLabel;
    @FXML private StackPane settingsViewContainer;
    @FXML private HBox settingsButtonLayer;
    @FXML private BorderPane rootPane;
    @FXML private Button settingsToggleButton;
    @FXML private TextField sessionRestoreCommand;

    private VBox currentThinkingBubble;

    private final List<String> availableAgents = Arrays.asList("plan", "build", "custom");
    private int currentAgentIndex = 1; // Par défaut "build"
    private ai.opencode.storage.ConfigManager configManager;
    private AgentOrchestrator orchestrator;

    @FXML
    public void initialize() {
        if (welcomeLabel != null) {
            setupWelcomeLabel();
        }
        
        if (rootPane != null) {
            rootPane.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (event.getCode() == KeyCode.TAB) {
                    cycleAgent();
                    event.consume();
                } else {
                    try {
                        if (isThinkingShortcutPressed(event)) {
                            toggleThinkingVisibility();
                            event.consume();
                        }
                    } catch (Exception e) {
                        // Ignore shortcut errors to prevent UI freeze
                    }
                }
            });
        }
        
        if (chatScrollPane != null) {
            chatScrollPane.widthProperty().addListener((obs, oldVal, newVal) -> {
                updateAllMessagesWidth();
            });
        }
        
        if (chatMessages != null) {
            chatMessages.setFillWidth(true);
        }
        
        if (userInput != null) {
            userInput.setPrefRowCount(6);
            userInput.setWrapText(true);
        }
        
        updateContextMetrics(0, 0, 0.0);
        updateProjectName("");
        updateServers();
        updateAgentUI();
        updateSessionHistory();
        updateInputAvailability();
        
        LOGGER.info("=== MAIN CONTROLLER INITIALIZED ===");
        LOGGER.info("settingsToggleButton is null: " + (settingsToggleButton == null));
        LOGGER.info("chatView is null: " + (chatView == null));
        LOGGER.info("settingsViewContainer is null: " + (settingsViewContainer == null));
        if (settingsToggleButton != null) {
            LOGGER.info("settingsToggleButton visible: " + settingsToggleButton.isVisible());
            LOGGER.info("settingsToggleButton text: " + settingsToggleButton.getText());
        }
    }

    private void setupWelcomeLabel() {
        welcomeLabel.setAlignment(Pos.CENTER);
        welcomeLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        welcomeLabel.getChildren().clear();

        WelcomeTitle title = new WelcomeTitle();
	title.setScaleX(2.0);
	title.setScaleY(2.0);

        VBox titleBox = new VBox(20);
        titleBox.setAlignment(Pos.CENTER);
	titleBox.setTranslateY(-80);

        Text message = new Text("Veuillez créer un nouveau chat via le bouton '+ New Chat' dans le panneau latéral pour commencer.");
        message.setFont(Font.font("Arial", 13));
        message.setFill(Color.web("#888"));
        message.setWrappingWidth(400);
	VBox.setMargin(message, new javafx.geometry.Insets(-60, 0, 0, 0));

        titleBox.getChildren().addAll(title, message);
        StackPane.setAlignment(titleBox, Pos.CENTER);
        welcomeLabel.getChildren().add(titleBox);
    }

    private void updateInputAvailability() {
        boolean hasSession = orchestrator != null && orchestrator.getSessionId() != null;
        if (userInput != null) {
            userInput.setDisable(!hasSession);
        }
        if (welcomeLabel != null) {
            welcomeLabel.setVisible(!hasSession);
        }
        if (chatView != null) {
            chatView.setVisible(true);
        }
    }

    private void updateAllMessagesWidth() {
        if (chatScrollPane == null || chatMessages == null) return;
        double width = chatScrollPane.getWidth() - 40;
        chatMessages.setPrefWidth(width + 40);
        chatMessages.setMaxWidth(width + 40);
        for (var node : chatMessages.getChildren()) {
            if (node instanceof HBox wrapper) {
                wrapper.setMaxWidth(width + 40);
                for (var child : wrapper.getChildren()) {
                    if (child instanceof VBox box) {
                        for (var innerChild : box.getChildren()) {
                            if (innerChild instanceof Label label) {
                                label.setMaxWidth(width);
                            }
                        }
                    }
                }
            }
        }
        chatMessages.requestLayout();
    }

    private void updateAgentUI() {
        if (agentLabel != null) {
            String agentId = availableAgents.get(currentAgentIndex).toLowerCase();
            agentLabel.setText(agentId.toUpperCase());
            
            String color = "#FFFFFF";
            if ("plan".equals(agentId)) color = "#FFA500";
            else if ("build".equals(agentId)) color = "#4A90E2";
            else if ("custom".equals(agentId)) color = "#2ECC71";
            
            agentLabel.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        }

        if (modelLabel != null && configManager != null) {
            ai.opencode.storage.AppConfig config = configManager.getConfig();
            String model = "gpt-4o";
            if (config.agents() != null && !config.agents().isEmpty()) {
                String currentAgentId = availableAgents.get(currentAgentIndex);
                if (config.agents().containsKey(currentAgentId)) {
                    model = config.agents().get(currentAgentId).model();
                } else {
                    model = config.agents().values().iterator().next().model();
                }
            }
            modelLabel.setText(model);
        }

        if (serverLabel != null && configManager != null) {
            ai.opencode.storage.AppConfig config = configManager.getConfig();
            String model = "gpt-4o";
            if (config.agents() != null && !config.agents().isEmpty()) {
                String currentAgentId = availableAgents.get(currentAgentIndex);
                if (config.agents().containsKey(currentAgentId)) {
                    model = config.agents().get(currentAgentId).model();
                } else {
                    model = config.agents().values().iterator().next().model();
                }
            }
            serverLabel.setText(getServerName(model));
        }
    }

    private String getServerName(String model) {
        if (model == null) return "Unknown";
        String m = model.toLowerCase();
        if (m.contains("gpt") || m.contains("openai")) return "OpenAI";
        if (m.contains("claude") || m.contains("anthropic")) return "Anthropic";
        if (m.contains("gemini") || m.contains("google")) return "Google";
        if (m.contains("llama") || m.contains("mistral") || m.contains("deepseek") || m.contains("qwen") || m.contains("phi")) return "llama.cpp";
        return "Local LLM";
    }

    public void setConfigManager(ai.opencode.storage.ConfigManager configManager) {
        this.configManager = configManager;
        updateAgentUI();
    }

    private void cycleAgent() {
        currentAgentIndex = (currentAgentIndex + 1) % availableAgents.size();
        updateAgentUI();
        if (orchestrator != null) {
            orchestrator.switchAgent(availableAgents.get(currentAgentIndex));
        }
    }

    private boolean isThinkingShortcutPressed(KeyEvent event) {
        if (configManager == null) return event.isControlDown() && event.getCode() == KeyCode.T;
        
        String shortcut = configManager.getConfig().getThinkingShortcut();
        if (shortcut == null || shortcut.isEmpty()) return false;

        String[] parts = shortcut.toUpperCase().split("\\+");
        boolean ctrl = false;
        boolean alt = false;
        boolean shift = false;
        KeyCode key = null;

        for (String part : parts) {
            if ("CTRL".equals(part)) ctrl = true;
            else if ("ALT".equals(part)) alt = true;
            else if ("SHIFT".equals(part)) shift = true;
            else {
                try {
                    key = KeyCode.valueOf(part);
                } catch (IllegalArgumentException e) {
                    return false;
                }
            }
        }

        return event.isControlDown() == ctrl && 
               event.isAltDown() == alt && 
               event.isShiftDown() == shift && 
               event.getCode() == key;
    }

    public void updateProjectName(String name) {
        if (projectNameLabel != null) {
            projectNameLabel.setText(name);
        }
    }

    public void updateContextMetrics(int tokens, int usagePercent, double cost) {
        if (tokensLabel != null) tokensLabel.setText(String.valueOf(tokens));
        if (usageLabel != null) usageLabel.setText(usagePercent + "%");
        if (costLabel != null) costLabel.setText(String.format("$%.2f", cost));
    }

    public void updateServers() {
        if (mcpServersList != null) {
            mcpServersList.getChildren().clear();
            Label l = new Label("Aucun serveur MCP");
            l.setStyle("-fx-text-fill: #666; -fx-font-size: 11;");
            mcpServersList.getChildren().add(l);
        }
        if (lspServersList != null) {
            lspServersList.getChildren().clear();
            Label l = new Label("Aucun serveur LSP");
            l.setStyle("-fx-text-fill: #666; -fx-font-size: 11;");
            lspServersList.getChildren().add(l);
        }
    }

    public void setOrchestrator(AgentOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
        if (orchestrator != null) {
            String currentId = orchestrator.getCurrentAgentId();
            int index = availableAgents.indexOf(currentId != null ? currentId.toLowerCase() : "build");
            if (index != -1) {
                currentAgentIndex = index;
                updateAgentUI();
            }
        }
        updateSessionHistory();
    }

    @FXML
    public void initProject() {
        ProjectInitializer initializer = new ProjectInitializer();
        Thread.ofVirtual().start(() -> {
            try {
                String result = initializer.initializeProject();
                Platform.runLater(() -> {
                    addMessageToChat("System", result, "assistant-message");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    addMessageToChat("System", "Error initializing project: " + e.getMessage(), "assistant-message");
                });
            }
        });
    }

    @FXML
    public void sendRequest() {
        if (orchestrator == null || orchestrator.getSessionId() == null) return;
        String text = userInput.getText();
        if (text == null || text.trim().isEmpty()) return;
        
        String trimmedText = text.trim();
        if (trimmedText.startsWith("/")) {
            handleSlashCommand(trimmedText);
            userInput.clear();
            return;
        }

        addMessageToChat("You", text, "user-message");
        userInput.clear();
        
        // Afficher immédiatement la bulle de réflexion "Thinking..."
        Platform.runLater(() -> addThinkingMessageToChat("Thinking..."));
        
        Thread.ofVirtual().start(() -> {
            String response = orchestrator.processRequest(text, thought -> {
                Platform.runLater(() -> addThinkingMessageToChat(thought));
            });
            Platform.runLater(() -> {
                if (response.startsWith("Error: No active session")) {
                    addMessageToChat("System", response, "assistant-message");
                } else {
                    addMessageToChat("Opencode (" + availableAgents.get(currentAgentIndex).toUpperCase() + ")", response, "assistant-message");
                    updateProjectName(orchestrator.getSessionTitle());
                    updateActualMetrics();
                    updateSessionHistory();
                }
            });
        });
    }

    private void handleSlashCommand(String commandLine) {
        String[] parts = commandLine.split("\\s+", 2);
        String command = parts[0].toLowerCase();
        String args = parts.length > 1 ? parts[1] : "";

        switch (command) {
            case "/new":
                startNewSession();
                break;
            case "/exit":
                Platform.exit();
                System.exit(0);
                break;
            case "/help":
                addMessageToChat("System", "Available commands:\n" +
                        "/new - Start a new session\n" +
                        "/exit - Exit Opencode\n" +
                        "/sessions - List sessions\n" +
                        "/models - List available models\n" +
                        "/status - Show current session status\n" +
                        "/thinking - Toggle thinking visibility\n" +
                        "/help - Show this menu", "assistant-message");
                break;
            case "/thinking":
                toggleThinkingVisibility();
                addMessageToChat("System", "Toggled thinking visibility.", "assistant-message");
                break;
            case "/status":
                updateActualMetrics();
                String status = String.format("Session: %s\nTokens: %d\nProject: %s", 
                        orchestrator.getSessionId(), 
                        orchestrator.getContextManager().getTotalTokens(),
                        orchestrator.getSessionTitle());
                addMessageToChat("System", status, "assistant-message");
                break;
            case "/sessions":
                updateSessionHistory();
                addMessageToChat("System", "Session history updated in the sidebar.", "assistant-message");
                break;
            default:
                addMessageToChat("System", "Unknown command: " + command + ". Type /help for list.", "assistant-message");
                break;
        }
    }

    private void updateActualMetrics() {
        if (orchestrator == null) return;
        ai.opencode.core.context.ContextManager cm = orchestrator.getContextManager();
        updateContextMetrics(cm.getTotalTokens(), (int) cm.getUsagePercent(), 0.0);
    }

    @FXML
    public void startNewSession() {
        if (orchestrator == null) return;
        orchestrator.createNewSession("Nouvelle Session");
        chatMessages.getChildren().clear();
        updateProjectName(orchestrator.getSessionTitle());
        updateSessionHistory();
        updateInputAvailability();
    }

    private void updateSessionHistory() {
        if (sessionHistoryList == null) return;
        sessionHistoryList.getChildren().clear();
        if (orchestrator == null) return;
        
        var sessionManager = new ai.opencode.core.session.SessionManager(ai.opencode.storage.DatabaseManager.getInstance());
        var sessions = sessionManager.getAllSessions();
        for (var session : sessions) {
            sessionHistoryList.getChildren().add(createSessionItem(session));
        }
    }

    private HBox createSessionItem(ai.opencode.core.session.SessionManager.SessionRecord session) {
        HBox item = new HBox();
        item.setAlignment(Pos.CENTER_LEFT);
        item.setSpacing(10);
        item.getStyleClass().add("session-item");
        item.setPrefHeight(36);

        Label titleLabel = new Label(session.title());
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        titleLabel.setStyle("-fx-text-fill: #ccc; -fx-cursor: hand; -fx-text-overrun: ellipsis;");
        titleLabel.setOnMouseClicked(e -> restoreSession(session.id()));

        Button deleteButton = new Button("✕");
        deleteButton.getStyleClass().addAll("button", "button-danger");
        deleteButton.setPrefSize(24, 24);
        deleteButton.setStyle("-fx-font-size: 10; -fx-font-weight: bold;");
        deleteButton.setOnAction(e -> deleteSession(session.id()));

        item.getChildren().addAll(titleLabel, deleteButton);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);
        return item;
    }

    private void restoreSession(String id) {
        if (orchestrator == null) return;
        orchestrator.restoreSession(id);
        updateProjectName(orchestrator.getSessionTitle());
        chatMessages.getChildren().clear();
        var sessionManager = new ai.opencode.core.session.SessionManager(ai.opencode.storage.DatabaseManager.getInstance());
        var history = sessionManager.getSessionHistory(id);
        for (var msg : history) {
            String sender = msg.role().equalsIgnoreCase("USER") ? "You" : "Opencode";
            String style = msg.role().equalsIgnoreCase("USER") ? "user-message" : "assistant-message";
            addMessageToChat(sender, msg.content(), style);
        }
        updateSessionHistory();
        updateInputAvailability();
    }

    private void deleteSession(String id) {
        if (orchestrator == null) return;
        var sessionManager = new ai.opencode.core.session.SessionManager(ai.opencode.storage.DatabaseManager.getInstance());
        sessionManager.deleteSession(id);
        if (orchestrator.getSessionId() != null && orchestrator.getSessionId().equals(id)) {
            orchestrator.restoreSession(null);
            updateProjectName("No project open");
            chatMessages.getChildren().clear();
        }
        updateSessionHistory();
        updateInputAvailability();
    }

    @FXML
    public void handleKeyPress(javafx.scene.input.KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (!event.isShiftDown()) {
                sendRequest();
                event.consume();
            } else {
                // Shift+Enter: allow new line (default TextArea behavior)
            }
        }
        // Ctrl + Enter as an alternative for submitting
        if (event.getCode() == KeyCode.ENTER && event.isControlDown()) {
            sendRequest();
            event.consume();
        }
    }

    private void addMessageToChat(String sender, String content, String styleClass) {
        if (welcomeLabel != null) {
            welcomeLabel.setVisible(false);
        }
        
        // Réinitialiser la bulle de thinking pour la prochaine requête
        currentThinkingBubble = null;

        HBox wrapper = new HBox();
        wrapper.setAlignment("user-message".equals(styleClass) ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        wrapper.setMaxWidth(Double.MAX_VALUE);
        
        VBox msgBox = new VBox();
        msgBox.getStyleClass().add(styleClass);
        
        Label senderLabel = new Label(sender + ":");
        senderLabel.setStyle("-fx-font-weight: bold;");
        if ("assistant-message".equals(styleClass)) {
            senderLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #ffffff;");
        }
        
        Label contentLabel = new Label(content);
        contentLabel.setWrapText(true);
        contentLabel.setMaxWidth(chatScrollPane.getWidth() - 40);
        if ("assistant-message".equals(styleClass)) {
            contentLabel.setTextFill(javafx.scene.paint.Color.WHITE);
        } else if ("user-message".equals(styleClass)) {
            contentLabel.setTextFill(javafx.scene.paint.Color.WHITE);
        }
        
        msgBox.getChildren().addAll(senderLabel, contentLabel);
        wrapper.getChildren().add(msgBox);
        chatMessages.getChildren().add(wrapper);
        chatScrollPane.setVvalue(1.0);
    }

    private void addThinkingMessageToChat(String thought) {
        if (welcomeLabel != null) {
            welcomeLabel.setVisible(false);
        }
        
        String shortcut = "Ctrl+T";
        if (configManager != null && configManager.getConfig().getThinkingShortcut() != null) {
            shortcut = configManager.getConfig().getThinkingShortcut();
        }

        if (currentThinkingBubble != null) {
            // Mettre à jour le contenu de la bulle existante
            VBox contentBox = (VBox) currentThinkingBubble.getChildren().get(0);
            Label textLabel = (Label) contentBox.getChildren().get(1);
            textLabel.setText(thought);
            chatScrollPane.setVvalue(1.0);
            return;
        }

        HBox wrapper = new HBox();
        wrapper.setAlignment(Pos.CENTER_LEFT);
        wrapper.setMaxWidth(Double.MAX_VALUE);
        
        VBox thinkBox = new VBox();
        thinkBox.getStyleClass().add("thinking-bubble");
        thinkBox.setStyle("-fx-background-color: #2d2d2d; -fx-background-radius: 10; -fx-padding: 10; -fx-border-color: #444; -fx-border-radius: 10;");
        
        Label label = new Label("Thinking... (" + shortcut + " to expand)");
        label.setStyle("-fx-font-style: italic; -fx-text-fill: #aaa; -fx-font-size: 11;");
        
        Label text = new Label(thought);
        text.setWrapText(true);
        text.setMaxWidth(chatScrollPane.getWidth() - 40);
        text.setTextFill(javafx.scene.paint.Color.LIGHTGRAY);
        
        VBox contentBox = new VBox(5, label, text);
        contentBox.setMaxHeight(100);
        thinkBox.getChildren().add(contentBox);
        
        currentThinkingBubble = thinkBox;
        wrapper.getChildren().add(thinkBox);
        chatMessages.getChildren().add(wrapper);
        chatScrollPane.setVvalue(1.0);
    }

    private void toggleThinkingVisibility() {
        for (var node : chatMessages.getChildren()) {
            if (node instanceof VBox box && box.getStyleClass().contains("thinking-bubble")) {
                VBox content = (VBox) box.getChildren().get(0);
                if (content.getMaxHeight() == 100) {
                    content.setMaxHeight(Double.MAX_VALUE);
                } else {
                    content.setMaxHeight(100);
                }
            }
        }
    }

    @FXML
    public void toggleSettings() {
        LOGGER.info("=== toggleSettings() called ===");
        LOGGER.info("chatView visible=" + (chatView != null ? chatView.isVisible() : "null"));
        LOGGER.info("settingsViewContainer visible=" + (settingsViewContainer != null ? settingsViewContainer.isVisible() : "null"));
        
        if (chatView != null && chatView.isVisible()) {
            chatView.setVisible(false);
            chatView.setManaged(false);
            if (welcomeLabel != null) welcomeLabel.setVisible(false);
            
            loadSettingsView();
            
            settingsViewContainer.setVisible(true);
            settingsViewContainer.setManaged(true);
            settingsToggleButton.setText("←");
            LOGGER.info("Settings view activated");
        } else {
            settingsViewContainer.setVisible(false);
            settingsViewContainer.setManaged(false);
            chatView.setVisible(true);
            chatView.setManaged(true);
            settingsToggleButton.setText("⚙");
            updateInputAvailability();
            LOGGER.info("Chat view restored");
        }
    }

    private void loadSettingsView() {
        LOGGER.info("loadSettingsView() called");
        try {
            ai.opencode.storage.ConfigManager configManager = new ai.opencode.storage.ConfigManager();
            String fxmlPath = "/fxml/settings_view.fxml";
            java.net.URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                LOGGER.severe("Fichier FXML non trouvé: " + fxmlPath);
                return;
            }
            
            FXMLLoader loader = new FXMLLoader(resource);
            loader.setControllerFactory(controllerClass -> {
                try {
                    if (controllerClass == SettingsController.class) {
                        return new SettingsController(configManager);
                    } else {
                        return controllerClass.getDeclaredConstructor().newInstance();
                    }
                } catch (Exception e) {
                    LOGGER.severe("Impossible d'instancier le contrôleur " + controllerClass.getSimpleName() + ": " + e.getMessage());
                    throw new RuntimeException(e);
                }
            });
            
            try {
                Pane root = loader.load();
                settingsViewContainer.getChildren().clear();
                settingsViewContainer.getChildren().add(root);
                
                // Ajuster la taille du conteneur settings à la zone disponible
                Platform.runLater(() -> {
                    settingsViewContainer.setMaxWidth(Double.MAX_VALUE);
                    settingsViewContainer.setMaxHeight(Double.MAX_VALUE);
                    settingsViewContainer.setPrefWidth(Double.MAX_VALUE);
                    settingsViewContainer.setPrefHeight(Double.MAX_VALUE);
                    root.setPrefWidth(Double.MAX_VALUE);
                    root.setPrefHeight(Double.MAX_VALUE);
                    settingsViewContainer.requestLayout();
                    LOGGER.info("Vue paramètres chargée avec succès. Taille conteneur: " + settingsViewContainer.getWidth() + "x" + settingsViewContainer.getHeight());
                });
            } catch (Exception e) {
                LOGGER.severe("Erreur lors du chargement de la vue paramètres: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            LOGGER.severe("Erreur lors du chargement de la vue paramètres: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setupDragAndDrop() {
        contextList.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });
        contextList.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasFiles()) {
                for (File file : db.getFiles()) {
                    addFileToContext(file);
                }
            }
            event.setDropCompleted(true);
            event.consume();
        });
    }

    private void addFileToContext(File file) {
        Label fileLabel = new Label(file.getName());
        fileLabel.getStyleClass().add("context-item");
        contextList.getChildren().add(fileLabel);
        LOGGER.info("Fichier ajouté au contexte : " + file.getAbsolutePath());
    }
}
