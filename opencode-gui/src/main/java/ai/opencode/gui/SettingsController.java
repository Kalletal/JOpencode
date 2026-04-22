package ai.opencode.gui;

import ai.opencode.storage.AppConfig;
import ai.opencode.storage.ConfigManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import java.util.*;
import java.util.logging.Logger;

public class SettingsController {
    private static final Logger LOGGER = Logger.getLogger(SettingsController.class.getName());

    @FXML private ComboBox<String> modelContextWindow;
    @FXML private PasswordField apiKeyField;
    @FXML private ComboBox<String> modelSelector;
    @FXML private TextField baseUrlField;
    @FXML private Label chevronAdvanced;
    @FXML private VBox advancedSettingsPanel;
    @FXML private TextArea systemPromptArea;
    @FXML private ComboBox<String> themeSelector;
    @FXML private Slider fontSizeSlider;
    @FXML private Label fontSizeLabel;
    @FXML private TreeView<String> settingsTree;

    @FXML private VBox panelLLMPreference;
    @FXML private VBox panelHistoriqueChats;
    @FXML private VBox panelDefaultPrompt;
    @FXML private VBox panelInterface;
    @FXML private VBox panelAgentSkills;
    @FXML private VBox panelVoixParole;
    @FXML private VBox panelGeneric;
    @FXML private Label genericPanelTitle;

    private final ConfigManager configManager;
    private boolean advancedSettingsVisible = false;

    // Icônes chargées une seule fois
    private static final Image IMG_LLM = new Image("/fxml/icons/llm.png");
    private static final Image IMG_ADMIN = new Image("/fxml/icons/admin.png");
    private static final Image IMG_AGENT = new Image("/fxml/icons/agent_skills.png");
    private static final Image IMG_APPEARANCE = new Image("/fxml/icons/appearance.png");

    public SettingsController(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @FXML
    public void initialize() {
        LOGGER.info("=== SettingsController initialize() called ===");

        loadLLMSettings();
        setupModelSelectors();
        setupThemeSelector();
        setupFontSizeSlider();

        buildSettingsTree();

        showPanel(panelLLMPreference);

        LOGGER.info("=== SettingsController initialize() done ===");
    }

    private void buildSettingsTree() {
        LOGGER.info("buildSettingsTree called");

        TreeItem<String> root = new TreeItem<>("Paramètres");
        root.setExpanded(true);

        // Section: Fournisseurs LLM
        TreeItem<String> llmSection = createSection("Fournisseurs LLM", IMG_LLM);
        llmSection.getChildren().addAll(
            createLeaf("Préférences LLM"),
            createLeaf("Voix et parole")
        );
        root.getChildren().add(llmSection);

        // Section: Admin
        TreeItem<String> adminSection = createSection("Admin", IMG_ADMIN);
        adminSection.getChildren().addAll(
            createLeaf("Historique des discussions"),
            createLeaf("Invite système par défaut")
        );
        root.getChildren().add(adminSection);

        // Section: Compétences de l'agent (pas de sous-item)
        TreeItem<String> agentSection = createSection("Compétences de l'agent", IMG_AGENT);
        root.getChildren().add(agentSection);

        // Section: Apparence
        TreeItem<String> appearanceSection = createSection("Apparence", IMG_APPEARANCE);
        appearanceSection.getChildren().add(createLeaf("Interface"));
        root.getChildren().add(appearanceSection);

        settingsTree.setRoot(root);
        LOGGER.info("TreeView root set with " + root.getChildren().size() + " sections");
        // showTreeLines non disponible sur cette version de JavaFX
        settingsTree.setCellFactory(tree -> new SettingsTreeCell());

        settingsTree.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null && newSelection.getValue() != null) {
                handleTreeSelection(newSelection);
            }
        });
    }

   private TreeItem<String> createSection(String text, Image icon) {
        ImageView imageView = new ImageView(icon);
        imageView.setFitWidth(16);
        imageView.setFitHeight(16);
        imageView.setPreserveRatio(true);

        TreeItem<String> section = new TreeItem<>(text);
        section.setGraphic(imageView);

        LOGGER.info("Created section: " + text);

        return section;
    }

    private TreeItem<String> createLeaf(String text) {
        TreeItem<String> leaf = new TreeItem<>(text);
        leaf.setGraphic(null);
        return leaf;
    }

    private void handleTreeSelection(TreeItem<String> selection) {
        String value = selection.getValue();
        LOGGER.info("Sélection treeview: " + value);

        switch (value) {
            case "Préférences LLM" -> showPanel(panelLLMPreference);
            case "Voix et parole" -> showPanel(panelVoixParole);
            case "Historique des discussions" -> showPanel(panelHistoriqueChats);
            case "Invite système par défaut" -> showPanel(panelDefaultPrompt);
            case "Compétences de l'agent" -> showPanel(panelAgentSkills);
            case "Interface" -> showPanel(panelInterface);
        }

        // Développer la section parente si nécessaire
        if (selection.getParent() != null && !selection.getParent().isExpanded()) {
            selection.getParent().setExpanded(true);
        }
    }

    private void loadLLMSettings() {
        AppConfig config = configManager.getConfig();
        String apiKey = config.apiKeys() != null ? config.apiKeys().getOrDefault("openai", "") : "";
        apiKeyField.setText(apiKey);

        if (config.agents() != null && !config.agents().isEmpty()) {
            AppConfig.AgentConfig defaultAgent = config.agents().values().iterator().next();
            if (defaultAgent != null) {
                modelSelector.setValue(defaultAgent.model());
            }
        }
    }

    private void setupModelSelectors() {
        List<String> models = Arrays.asList(
            "gpt-4o", "gpt-4o-mini", "gpt-4-turbo", "gpt-4",
            "claude-3.5-sonnet", "claude-3-opus",
            "gemini-pro", "gemini-1.5-pro",
            "llama-3-70b", "mistral-large", "qwen-2.5-72b"
        );
        modelContextWindow.getItems().addAll(models);
        modelSelector.getItems().addAll(models);

        List<String> contextWindows = Arrays.asList("8K", "16K", "32K", "64K", "128K", "200K");
        modelContextWindow.getItems().addAll(contextWindows);
        if (!modelContextWindow.getItems().isEmpty()) {
            modelContextWindow.setValue("8K");
        }
    }

    private void setupThemeSelector() {
        themeSelector.getItems().addAll("Sombre", "Clair", "Système");
        themeSelector.setValue("Sombre");
    }

    private void setupFontSizeSlider() {
        fontSizeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (fontSizeLabel != null) {
                fontSizeLabel.setText(String.valueOf(newVal.intValue()));
            }
        });
    }

    private void showPanel(VBox activePanel) {
        panelLLMPreference.setVisible(false);
        panelLLMPreference.setManaged(false);
        panelHistoriqueChats.setVisible(false);
        panelHistoriqueChats.setManaged(false);
        panelDefaultPrompt.setVisible(false);
        panelDefaultPrompt.setManaged(false);
        panelInterface.setVisible(false);
        panelInterface.setManaged(false);
        panelAgentSkills.setVisible(false);
        panelAgentSkills.setManaged(false);
        panelVoixParole.setVisible(false);
        panelVoixParole.setManaged(false);
        panelGeneric.setVisible(false);
        panelGeneric.setManaged(false);

        activePanel.setVisible(true);
        activePanel.setManaged(true);
    }

    // ===== ADVANCED SETTINGS =====

    @FXML
    public void toggleAdvancedSettings() {
        advancedSettingsPanel.setVisible(!advancedSettingsVisible);
        advancedSettingsVisible = !advancedSettingsVisible;
        String chevronStyle = "-fx-text-fill: #999999; -fx-font-size: 13; -fx-padding: 0 0 0 8;";
        if (advancedSettingsVisible) {
            chevronAdvanced.setStyle(chevronStyle + "-fx-rotate: 90;");
        } else {
            chevronAdvanced.setStyle(chevronStyle);
        }
    }

    // ===== SAVE / CANCEL =====

    @FXML
    public void saveLLMSettings() {
        AppConfig oldConfig = configManager.getConfig();

        Map<String, String> newApiKeys = new HashMap<>(oldConfig.apiKeys());
        newApiKeys.put("openai", apiKeyField.getText());

        Map<String, AppConfig.AgentConfig> newAgents = new HashMap<>();
        if (oldConfig.agents() != null) {
            newAgents.putAll(oldConfig.agents());
        }

        String selectedModel = modelSelector.getValue();
        AppConfig.AgentConfig agentConfig = new AppConfig.AgentConfig(
            "build", "build",
            selectedModel != null ? selectedModel : "gpt-4o",
            new HashMap<>()
        );
        newAgents.put("build", agentConfig);

        AppConfig newConfig = new AppConfig(
            oldConfig.username(), oldConfig.defaultAgent(),
            newAgents, newApiKeys,
            oldConfig.experimental(), oldConfig.thinkingShortcut()
        );

        configManager.setConfig(newConfig);
        LOGGER.info("Paramètres LLM sauvegardés avec succès.");
        showAlert("Succès", "Paramètres sauvegardés avec succès.");
    }

    @FXML
    public void cancelSettings() {
        LOGGER.info("Paramètres annulés.");
    }

    @FXML
    public void saveSettings() {
        LOGGER.info("Paramètres sauvegardés.");
        showAlert("Succès", "Paramètres sauvegardés avec succès.");
    }

    // ===== BOTTOM LINKS =====

    @FXML
    public void openSupport() {
        LOGGER.info("Ouverture du support...");
    }

    @FXML
    public void openPrivacy() {
        LOGGER.info("Ouverture de la politique de confidentialité...");
    }

    @FXML
    public void openLicense() {
        LOGGER.info("Ouverture de la licence...");
    }

    // ===== UTILITY =====

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ===== Custom TreeCell =====

private class SettingsTreeCell extends TreeCell<String> {
        private final Background defaultBg = Background.EMPTY;
        private final Background hoverBg = new Background(new BackgroundFill(
            Color.web("#2a2a2a"), javafx.scene.layout.CornerRadii.EMPTY, null));
        private static final Font FONT_PARENT = Font.font(14);
        private static final Font FONT_CHILD = Font.font(11);
        private static final Font FONT_BOLD = Font.font("System Bold", 14);
        private static final Font FONT_BOLD_CHILD = Font.font("System Bold", 11);

        public SettingsTreeCell() {
            this.setMinHeight(35);
            this.setMaxHeight(35);
            this.setPadding(new Insets(0, 8, 0, 4));
        }

       @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setText(null);
                setGraphic(null);
                setBackground(defaultBg);
                setFont(FONT_PARENT);
            } else {
                setText(item);

                if (isParent()) {
                    setGraphic(getImageForItem(item));
                    setFont(isSelected() ? FONT_BOLD : FONT_PARENT);
                    setTextFill(Color.web("#cccccc"));
                    getStyleClass().removeAll("subitem");
                } else {
                    HBox spacer = new HBox();
                    spacer.setPrefWidth(28);
                    spacer.setMinWidth(28);
                    spacer.setMaxWidth(28);
                    spacer.setBackground(defaultBg);
                    setGraphic(spacer);
                    setFont(isSelected() ? FONT_BOLD_CHILD : FONT_CHILD);
                    setTextFill(Color.web("#cccccc"));
                    getStyleClass().add("subitem");
                }
                setBackground(isSelected() ? hoverBg : defaultBg);
            }

            this.setOnMouseEntered(e -> {
                if (!isSelected()) {
                    setBackground(hoverBg);
                }
            });
            this.setOnMouseExited(e -> {
                if (!isSelected()) {
                    setBackground(defaultBg);
                }
            });
        }

        private ImageView getImageForItem(String value) {
            switch (value) {
                case "Fournisseurs LLM" -> {
                    ImageView iv = new ImageView(IMG_LLM);
                    iv.setFitWidth(16); iv.setFitHeight(16);
                    iv.setPreserveRatio(true);
                    return iv;
                }
                case "Admin" -> {
                    ImageView iv = new ImageView(IMG_ADMIN);
                    iv.setFitWidth(16); iv.setFitHeight(16);
                    iv.setPreserveRatio(true);
                    return iv;
                }
                case "Compétences de l'agent" -> {
                    ImageView iv = new ImageView(IMG_AGENT);
                    iv.setFitWidth(16); iv.setFitHeight(16);
                    iv.setPreserveRatio(true);
                    return iv;
                }
                case "Apparence" -> {
                    ImageView iv = new ImageView(IMG_APPEARANCE);
                    iv.setFitWidth(16); iv.setFitHeight(16);
                    iv.setPreserveRatio(true);
                    return iv;
                }
                default -> {
                    ImageView iv = new ImageView();
                    iv.setFitWidth(0); iv.setFitHeight(0);
                    return iv;
                }
            }
        }

        private boolean isParent() {
            return !getTreeItem().getChildren().isEmpty();
        }
    }
}
