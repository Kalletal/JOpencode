package ai.opencode.gui;

import ai.opencode.storage.AppConfig;
import ai.opencode.storage.ConfigManager;
import ai.opencode.gui.theme.ThemeManager;
import ai.opencode.gui.i18n.LanguageManager;
import ai.opencode.gui.i18n.TranslationHelper;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.*;
import java.util.logging.Logger;

public class SettingsController {
    private static final Logger LOGGER = Logger.getLogger(SettingsController.class.getName());

    @FXML private ComboBox<String> modelContextWindow;
    @FXML private PasswordField apiKeyField;
    @FXML private ComboBox<String> modelSelector;
    @FXML private TextField baseUrlField;
    @FXML private Label labelLLMProvider;
    @FXML private Label labelContextWindow;
    @FXML private Label labelLocalAIUrl;
    @FXML private Label labelAdvancedSettings;
    @FXML private Label chevronAdvanced;
    @FXML private VBox advancedSettingsPanel;
    @FXML private TextArea systemPromptArea;
    @FXML private ComboBox<String> themeSelector;
    @FXML private ComboBox<String> languageSelector;
    @FXML private Label headerTextLLM;
    @FXML private Label headerTextAdmin;
    @FXML private Label headerTextAppearance;
    @FXML private Label chevronLLM;
    @FXML private Label chevronAdmin;
    @FXML private Label chevronAppearance;

    @FXML private Label btnLLMPreference;
    @FXML private Label btnVoixParole;
    @FXML private Label btnHistoriqueChats;
    @FXML private Label btnDefaultPrompt;
    @FXML private Label btnAgentSkills;
    @FXML private Label btnInterface;
    @FXML private Button backButton;
    @FXML private VBox panelLLMPreference;
    @FXML private VBox panelHistoriqueChats;
    @FXML private VBox panelDefaultPrompt;
    @FXML private VBox panelInterface;
    @FXML private VBox panelAgentSkills;
    @FXML private VBox panelVoixParole;
    @FXML private VBox panelGeneric;
    @FXML private Label genericPanelTitle;

      @FXML private Region bgLLM, bgAdmin, bgAppearance;
    @FXML private HBox submenuLLM, submenuAdmin, submenuAppearance;
    @FXML private VBox vboxLLMChildren, vboxAdminChildren, vboxAppearanceChildren;
    @FXML private HBox headerLLM;
    @FXML private HBox headerAdmin;
    @FXML private HBox headerAgentSkills;
    @FXML private HBox headerAppearance;
    @FXML private VBox menuContainer;

    private final ConfigManager configManager;
    private Node activeMenuItem;
    private boolean advancedSettingsVisible = false;
    private boolean llmSubmenuVisible = false;
    private boolean adminSubmenuVisible = false;
    private boolean appearanceSubmenuVisible = false;
    private javafx.animation.Animation activeAnimation;
    private String originalStyleLLMPreference;
    private String originalStyleVoixParole;
    private String originalStyleHistoriqueChats;
    private String originalStyleDefaultPrompt;
    private String originalStyleInterface;
    private boolean hoveringSubItem = false;
    private Scene currentScene;
    private ThemeManager themeManager;
    private LanguageManager languageManager;
    private final MainController mainController;

    public SettingsController(ConfigManager configManager, Scene scene, MainController mainController) {
        this.configManager = configManager;
        this.currentScene = scene;
        this.mainController = mainController;
        this.themeManager = new ThemeManager();
        this.languageManager = new LanguageManager();
    }

  @FXML
    public void initialize() {
        LOGGER.info("=== SettingsController initialize() called ===");
        
    loadLLMSettings();
        setupModelSelectors();
        setupThemeSelector();
        setupLanguageSelector();
       initLanguageManager();
     applyTranslationsToAllPanels();
        showPanel(panelLLMPreference);
        Platform.runLater(() -> { if (themeSelector.getValue() != null) updateBackButtonColor(themeSelector.getValue()); });
        
        AtomicInteger llmCount = new AtomicInteger(0);
        AtomicInteger adminCount = new AtomicInteger(0);
        AtomicInteger appearanceCount = new AtomicInteger(0);
        
        Platform.runLater(() -> {
            javafx.scene.layout.Region regionLLM = (javafx.scene.layout.Region) submenuLLM;
            javafx.scene.layout.Region regionAdmin = (javafx.scene.layout.Region) submenuAdmin;
            javafx.scene.layout.Region regionAppearance = (javafx.scene.layout.Region) submenuAppearance;
            
            Rectangle clipLLM = new Rectangle(menuContainer.getWidth(), 0);
            clipLLM.heightProperty().bind(regionLLM.heightProperty());
            regionLLM.setClip(clipLLM);
            regionLLM.setMaxHeight(0);
            
            Rectangle clipAdmin = new Rectangle(menuContainer.getWidth(), 0);
            clipAdmin.heightProperty().bind(regionAdmin.heightProperty());
            regionAdmin.setClip(clipAdmin);
            regionAdmin.setMaxHeight(0);
            
            Rectangle clipAppearance = new Rectangle(menuContainer.getWidth(), 0);
            clipAppearance.heightProperty().bind(regionAppearance.heightProperty());
            regionAppearance.setClip(clipAppearance);
            regionAppearance.setMaxHeight(0);
            
          // Sauvegarder les styles originaux des sous-items menu
            if (btnLLMPreference instanceof Label lbl) originalStyleLLMPreference = lbl.getStyle();
            if (btnVoixParole instanceof Label lbl) originalStyleVoixParole = lbl.getStyle();
            if (btnHistoriqueChats instanceof Label lbl) originalStyleHistoriqueChats = lbl.getStyle();
            if (btnDefaultPrompt instanceof Label lbl) originalStyleDefaultPrompt = lbl.getStyle();
            if (btnInterface instanceof Label lbl) originalStyleInterface = lbl.getStyle();
        });
        
        LOGGER.info("=== SettingsController initialize() done ===");
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
        if (modelContextWindow.getItems().size() > 0) {
            modelContextWindow.setValue("8K");
        }
    }

    private void setupThemeSelector() {
        themeSelector.getItems().addAll("Sombre", "Clair");
        String savedTheme = configManager.getConfig().experimental() != null 
            ? (String) configManager.getConfig().experimental().getOrDefault("theme", "Sombre") 
            : "Sombre";
        themeSelector.setValue(savedTheme);
  applyTheme();
    }

   private void setupLanguageSelector() {
        List<String> languages = LanguageManager.getAvailableDisplayNames();
        languageSelector.getItems().addAll(languages);
        
        String savedLang = configManager.getConfig().experimental() != null 
            ? (String) configManager.getConfig().experimental().getOrDefault("language", "Français") 
            : "Français";
        if (languages.contains(savedLang)) {
            languageSelector.setValue(savedLang);
        } else {
            languageSelector.setValue("Français");
        }
    }

    private void initLanguageManager() {
        String savedLang = configManager.getConfig().experimental() != null 
            ? (String) configManager.getConfig().experimental().getOrDefault("language", "Français") 
            : "Français";
        languageManager.loadBundleByDisplayName(savedLang);
        
       // Appliquer RTL si nécessaire via setNodeOrientation de Scene
        if (currentScene != null && languageManager.isRTL()) {
            currentScene.setNodeOrientation(javafx.geometry.NodeOrientation.RIGHT_TO_LEFT);
        }
    }

    // ===== ACCORDION SIDEBAR =====

   @FXML
    public void applyTheme() {
        String theme = themeSelector.getValue();
        if (currentScene != null) {
            themeManager.applyTheme(currentScene, theme);
        }
        updateBackButtonColor(theme);
        
        Map<String, Object> expConfig = new HashMap<>(configManager.getConfig().experimental());
        expConfig.put("theme", theme);
        expConfig.put("lastModified", Instant.now().toString());
        AppConfig oldConfig = configManager.getConfig();
        AppConfig newConfig = new AppConfig(
            oldConfig.username(),
            oldConfig.defaultAgent(),
            oldConfig.agents(),
            oldConfig.apiKeys(),
            expConfig,
            oldConfig.thinkingShortcut()
        );
        configManager.setConfig(newConfig);
        LOGGER.info("Thème appliqué immédiatement : " + theme);
    }

    @FXML
    public void applyLanguage() {
        String language = languageSelector.getValue();
       if (language != null) {
            // Charger le bundle de traduction et rafraîchir les labels
            languageManager.loadBundleByDisplayName(language);
            
           // RTL support — changer l'orientation nœud via Scene
            if (currentScene != null) {
                if (languageManager.isRTL()) {
                    currentScene.setNodeOrientation(javafx.geometry.NodeOrientation.RIGHT_TO_LEFT);
                } else {
                    currentScene.setNodeOrientation(javafx.geometry.NodeOrientation.LEFT_TO_RIGHT);
                }
            }
            
            // Rafraîchir tous les Labels traduisibles dans le panneau des paramètres
            applyTranslationsToAllPanels();

            Map<String, Object> expConfig = new HashMap<>(configManager.getConfig().experimental());
            expConfig.put("language", language);
            expConfig.put("lastModified", Instant.now().toString());
            AppConfig oldConfig = configManager.getConfig();
            AppConfig newConfig = new AppConfig(
                oldConfig.username(),
                oldConfig.defaultAgent(),
                oldConfig.agents(),
                oldConfig.apiKeys(),
                expConfig,
                oldConfig.thinkingShortcut()
            );
           configManager.setConfig(newConfig);
            LOGGER.info("Langue appliquée : " + language);
        }
    }

    private void updateBackButtonColor(String theme) {
        if (backButton == null) return;
        boolean isDark = "Sombre".equals(theme);
        String textColor = isDark ? "#bbbbbb" : "#555555";
        backButton.setStyle(backButton.getStyle() != null && backButton.getStyle().contains("-fx-text-fill:")
            ? backButton.getStyle().replaceAll("-fx-text-fill:\\s*#[a-fA-F0-9]+", "-fx-text-fill: " + textColor)
            : "-fx-text-fill: " + textColor + "; -fx-font-size: 22; -fx-background-color: transparent; -fx-border-color: transparent; -fx-padding: 6; -fx-cursor: hand;");
        LOGGER.info("Couleur bouton retour mise à jour : " + textColor);
    }

   /**
      * Applique les traductions à tous les panneaux du contrôleur des paramètres.
     * Enregistre d'abord les mappages Label->clé i18n, puis traduit tout le panneau.
     */
    private void applyTranslationsToAllPanels() {
        if (languageManager.getResourceBundle() == null) return;
        
        ResourceBundle bundle = languageManager.getResourceBundle();
        
        // Enregistrer les mappings explicites pour la sidebar
        TranslationHelper.registerTranslation(headerTextLLM, "sidebar.fournisseurs_llm");
        TranslationHelper.registerTranslation(headerTextAdmin, "sidebar.admin");
        TranslationHelper.registerTranslation(headerTextAppearance, "sidebar.appearance");
        TranslationHelper.registerTranslation(btnLLMPreference, "panel.llm_preference.title");
        TranslationHelper.registerTranslation(btnVoixParole, "panel.voix_parole.description");
        TranslationHelper.registerTranslation(btnHistoriqueChats, "panel.historique_chats.title");
        TranslationHelper.registerTranslation(btnDefaultPrompt, "panel.default_prompt.title");
     TranslationHelper.registerTranslation(btnInterface, "panel.interface.title");
        TranslationHelper.registerTranslation(labelLLMProvider, "panel.llm_preference.title");
        TranslationHelper.registerTranslation(labelContextWindow, "label.context_window");
        TranslationHelper.registerTranslation(labelLocalAIUrl, "label.local_ai_url");
        TranslationHelper.registerTranslation(labelAdvancedSettings, "label.advanced_settings");
        
        // Traduire la sidebar
        TranslationHelper.applyTranslations(menuContainer, bundle);
        
        // Traduire chaque panneau de contenu
        TranslationHelper.applyTranslations(panelLLMPreference, bundle);
        TranslationHelper.applyTranslations(panelHistoriqueChats, bundle);
        TranslationHelper.applyTranslations(panelDefaultPrompt, bundle);
        TranslationHelper.applyTranslations(panelInterface, bundle);
        TranslationHelper.applyTranslations(panelAgentSkills, bundle);
        TranslationHelper.applyTranslations(panelVoixParole, bundle);
    }

    @FXML
    public void toggleSectionLLM() {
        llmSubmenuVisible = !llmSubmenuVisible;
        String chevronStyle = "-fx-text-fill: #999999; -fx-font-size: 14; -fx-padding: 0 0 0 8;";
        if (llmSubmenuVisible) {
            chevronLLM.setStyle(chevronStyle + "-fx-rotate: 90;");
        } else {
            chevronLLM.setStyle(chevronStyle);
        }
        animateSubmenu(submenuLLM, llmSubmenuVisible);
    }

    @FXML
    public void toggleSectionAdmin() {
        adminSubmenuVisible = !adminSubmenuVisible;
        String chevronStyle = "-fx-text-fill: #999999; -fx-font-size: 14; -fx-padding: 0 0 0 8;";
        if (adminSubmenuVisible) {
            chevronAdmin.setStyle(chevronStyle + "-fx-rotate: 90;");
        } else {
            chevronAdmin.setStyle(chevronStyle);
        }
        animateSubmenu(submenuAdmin, adminSubmenuVisible);
    }

    @FXML
    public void toggleSectionAppearance() {
        appearanceSubmenuVisible = !appearanceSubmenuVisible;
        String chevronStyle = "-fx-text-fill: #999999; -fx-font-size: 14; -fx-padding: 0 0 0 8;";
        if (appearanceSubmenuVisible) {
            chevronAppearance.setStyle(chevronStyle + "-fx-rotate: 90;");
        } else {
            chevronAppearance.setStyle(chevronStyle);
        }
        animateSubmenu(submenuAppearance, appearanceSubmenuVisible);
    }

    private void animateSubmenu(Node submenu, boolean expand) {
        javafx.scene.layout.Region region = (javafx.scene.layout.Region) submenu;
        if (activeAnimation != null) {
            activeAnimation.stop();
        }
        double width = menuContainer.getWidth();
        if (width <= 0) {
            width = 263;
        }
        
        // Mettre à jour le clip avec la largeur courante du menuContainer
        if (region.getClip() != null) {
            ((javafx.scene.shape.Rectangle) region.getClip()).setWidth(width);
        }
        
        double targetHeight;
        if (expand) {
            targetHeight = region.prefHeight(width);
            if (targetHeight <= 0) {
                return;
            }
        } else {
            region.setMaxHeight(region.prefHeight(width));
            targetHeight = 0;
        }
        LOGGER.info("animateSubmenu: expand=" + expand + ", width=" + width + ", targetHeight=" + targetHeight + ", currentMaxHeight=" + region.getMaxHeight());
        Timeline timeline = new Timeline(new KeyFrame(
            javafx.util.Duration.millis(250),
            new KeyValue(region.maxHeightProperty(), targetHeight, Interpolator.EASE_OUT)
        ));
        timeline.setOnFinished(e -> {
            if (expand) {
                region.setMaxHeight(Double.POSITIVE_INFINITY);
            } else {
                region.setMaxHeight(0);
            }
        });
        activeAnimation = timeline;
        activeAnimation.play();
    }

    // ===== NAVIGATION PANNEAUX =====

   @FXML
    public void showLLMPreference() {
        showPanel(panelLLMPreference);
        highlightMenuItem(btnLLMPreference);
        if (!llmSubmenuVisible) {
            toggleSectionLLM();
        }
    }

    @FXML
    public void showHistoriqueChats() {
        showPanel(panelHistoriqueChats);
        highlightMenuItem(btnHistoriqueChats);
        if (!adminSubmenuVisible) {
            toggleSectionAdmin();
        }
    }

    @FXML
    public void showDefaultPrompt() {
        showPanel(panelDefaultPrompt);
        highlightMenuItem(btnDefaultPrompt);
        if (!adminSubmenuVisible) {
            toggleSectionAdmin();
        }
    }

    @FXML
    public void showAgentSkills() {
        showPanel(panelAgentSkills);
        highlightMenuItem(btnAgentSkills);
    }

    @FXML
    public void showInterfaceSettings() {
        showPanel(panelInterface);
        highlightMenuItem(btnInterface);
        if (!appearanceSubmenuVisible) {
            toggleSectionAppearance();
        }
    }

    @FXML
    public void showVoixParole() {
        showPanel(panelVoixParole);
        highlightMenuItem(btnVoixParole);
    }

    @FXML
    public void showGenericPanel(javafx.event.Event event) {
        Button clickedButton = (Button) event.getSource();
        String title = clickedButton.getText();
        genericPanelTitle.setText(title);
        showPanel(panelGeneric);
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

private void highlightMenuItem(Node activeButton) {
        // Retirer gras de TOUS les sous-items en restaurant leurs styles originaux
        if (btnLLMPreference instanceof Label lbl) {
            lbl.getStyleClass().remove("menu-item-active");
            lbl.setStyle(originalStyleLLMPreference);
        }
        if (btnVoixParole instanceof Label lbl) {
            lbl.getStyleClass().remove("menu-item-active");
            lbl.setStyle(originalStyleVoixParole);
        }
        if (btnHistoriqueChats instanceof Label lbl) {
            lbl.getStyleClass().remove("menu-item-active");
            lbl.setStyle(originalStyleHistoriqueChats);
        }
        if (btnDefaultPrompt instanceof Label lbl) {
            lbl.getStyleClass().remove("menu-item-active");
            lbl.setStyle(originalStyleDefaultPrompt);
        }
        if (btnInterface instanceof Label lbl) {
            lbl.getStyleClass().remove("menu-item-active");
            lbl.setStyle(originalStyleInterface);
        }

        // Appliquer gras au bouton cliqué via style inline
        if (activeButton instanceof Label label) {
            String currentStyle = label.getStyle();
            label.setStyle(currentStyle + "; -fx-font-weight: bold; -fx-text-fill: white;");
            activeMenuItem = activeButton;
        } else if (activeButton != null) {
            activeMenuItem = activeButton;
        }
    }

   @FXML
    public void handleMenuHover(MouseEvent event) {
        Node source = (Node) event.getSource();
        if (source != activeMenuItem && source instanceof Region) {
            Region region = (Region) source;
            region.setBackground(new Background(new BackgroundFill(javafx.scene.paint.Color.web("#2a2a2a"), CornerRadii.EMPTY, Insets.EMPTY)));
        }
    }

    @FXML
    public void handleMenuHoverExit(MouseEvent event) {
        Node source = (Node) event.getSource();
        if (source != activeMenuItem && source instanceof Region) {
            Region region = (Region) source;
            region.setBackground(Background.EMPTY);
        }
    }


    // ===== ADVANCED SETTINGS =====

    @FXML
    public void toggleAdvancedSettings() {
        advancedSettingsVisible = !advancedSettingsVisible;
        advancedSettingsPanel.setVisible(advancedSettingsVisible);
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
            "build",
            "build",
            selectedModel != null ? selectedModel : "gpt-4o",
            new HashMap<>()
        );
        newAgents.put("build", agentConfig);

        AppConfig newConfig = new AppConfig(
            oldConfig.username(),
            oldConfig.defaultAgent(),
            newAgents,
            newApiKeys,
            oldConfig.experimental(),
            oldConfig.thinkingShortcut()
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

    @FXML
    public void returnToChat() {
        LOGGER.info("Retour vers chat depuis settings");
        if (mainController != null) {
            mainController.toggleSettings();
        }
    }

    // ===== UTILITY =====

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
