package ai.opencode.gui;

import ai.opencode.storage.AppConfig;
import ai.opencode.storage.ConfigManager;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.shape.Rectangle;
import javafx.scene.control.*;
import javafx.scene.Node;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import java.util.concurrent.atomic.AtomicInteger;
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
    @FXML private ComboBox<String> languageSelector;
    @FXML private Slider fontSizeSlider;
    @FXML private Label fontSizeLabel;

    @FXML private Node btnLLMPreference;
    @FXML private Node btnVoixParole;
    @FXML private Node btnHistoriqueChats;
    @FXML private Node btnDefaultPrompt;
    @FXML private Node btnAgentSkills;
    @FXML private Node btnInterface;
    @FXML private VBox panelLLMPreference;
    @FXML private VBox panelHistoriqueChats;
    @FXML private VBox panelDefaultPrompt;
    @FXML private VBox panelInterface;
    @FXML private VBox panelAgentSkills;
    @FXML private VBox panelVoixParole;
    @FXML private VBox panelGeneric;
    @FXML private Label genericPanelTitle;

    @FXML private Label chevronLLM;
    @FXML private Label chevronAdmin;
    @FXML private Label chevronAppearance;
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

    public SettingsController(ConfigManager configManager, Scene scene) {
        this.configManager = configManager;
        this.currentScene = scene;
    }

   @FXML
    public void initialize() {
        LOGGER.info("=== SettingsController initialize() called ===");
        
      loadLLMSettings();
        setupModelSelectors();
        setupThemeSelector();
        setupFontSizeSlider();
        setupLanguageSelector();
      showPanel(panelLLMPreference);
        
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

   private void setupFontSizeSlider() {
        fontSizeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (fontSizeLabel != null) {
                fontSizeLabel.setText(String.valueOf(newVal.intValue()));
            }
        });
    }

    private void setupLanguageSelector() {
        List<String> languages = Arrays.asList(
            "English",
            "中文（普通话）",
            "हिन्दी",
            "Español",
            "Français",
            "العربية",
            "বাংলা",
            "Português",
            "Русский",
            "اردو"
        );
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

    // ===== ACCORDION SIDEBAR =====

    @FXML
    public void applyTheme() {
        String theme = themeSelector.getValue();
        if (currentScene != null) {
            currentScene.getStylesheets().clear();
            if ("Clair".equals(theme)) {
                try {
                    String lightCssPath = getClass().getResource("/css/style-light.css").toExternalForm();
                    currentScene.getStylesheets().add(lightCssPath);
                } catch (Exception e) {
                    LOGGER.warning("Impossible de charger le thème clair: " + e.getMessage());
                }
            } else {
                try {
                    String darkCssPath = getClass().getResource("/css/style.css").toExternalForm();
                    currentScene.getStylesheets().add(darkCssPath);
                } catch (Exception e) {
                    LOGGER.warning("Impossible de charger le thème sombre: " + e.getMessage());
                }
            }
            Platform.runLater(() -> applyThemeColors(currentScene.getRoot(), "Sombre".equals(theme)));
        }
        
        Map<String, Object> expConfig = new HashMap<>(configManager.getConfig().experimental());
        expConfig.put("theme", theme);
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

    private void applyThemeColors(javafx.scene.Node node, boolean isDark) {
        if (node == null) return;
        
        if (node instanceof javafx.scene.layout.Region region) {
            String bg = region.getStyle().contains("-fx-background-color") 
                ? region.getStyle() : "";
            if (!bg.contains("#") || bg.matches(".*-fx-background-color:\\s*#[a-fA-F0-9]+.*")) {
                if (isDark) {
                    region.setStyle(region.getStyle().replaceAll("-fx-background-color:\\s*#ffffff[^;]*", "-fx-background-color: #1a1a1a"));
                    region.setStyle(region.getStyle().replaceAll("-fx-background-color:\\s*#f5f5f5[^;]*", "-fx-background-color: #1a1a1a"));
                    region.setStyle(region.getStyle().replaceAll("-fx-background-color:\\s*#f0f0f0[^;]*", "-fx-background-color: #1a1a1a"));
                    region.setStyle(region.getStyle().replaceAll("-fx-background-color:\\s*#e8e8e8[^;]*", "-fx-background-color: transparent"));
                    region.setStyle(region.getStyle().replaceAll("-fx-background-color:\\s*#2d2d2d[^;]*", "-fx-background-color: #2d2d2d"));
                } else {
                    region.setStyle(region.getStyle().replaceAll("-fx-background-color:\\s*#1a1a1a[^;]*", "-fx-background-color: #f0f0f0"));
                    region.setStyle(region.getStyle().replaceAll("-fx-background-color:\\s*#2d2d2d[^;]*", "-fx-background-color: #2d2d2d"));
                    region.setStyle(region.getStyle().replaceAll("-fx-background-color:\\s*transparent(?!;)", "-fx-background-color: transparent"));
                }
            }
        }
        
        if (node instanceof javafx.scene.control.Label label) {
            String style = label.getStyle();
            if (isDark) {
                style = style.replaceAll("-fx-text-fill:\\s*#[a-fA-F0-9]{6}", "-fx-text-fill: #cccccc");
                style = style.replaceAll("-fx-text-fill:\\s*white", "-fx-text-fill: #cccccc");
                style = style.replaceAll("-fx-text-fill:\\s*#ffffff", "-fx-text-fill: #cccccc");
            } else {
                style = style.replaceAll("-fx-text-fill:\\s*#[a-fA-F0-9]{6}", "-fx-text-fill: #333333");
                style = style.replaceAll("-fx-text-fill:\\s*black", "-fx-text-fill: #1a1a1a");
                style = style.replaceAll("-fx-text-fill:\\s*#000000", "-fx-text-fill: #1a1a1a");
            }
            label.setStyle(style);
        }
        
        if (node instanceof javafx.scene.control.ComboBox<?> comboBox) {
            if (isDark) {
                String cs = comboBox.getStyle();
                cs = cs.replaceAll("-fx-background-color:\\s*#[a-fA-F0-9]+", "-fx-background-color: #2d2d2d");
                cs = cs.replaceAll("-fx-text-fill:\\s*#[a-fA-F0-9]+", "-fx-text-fill: white");
                cs = cs.replaceAll("-fx-border-color:\\s*#[a-fA-F0-9]+", "-fx-border-color: #444444");
                comboBox.setStyle(cs);
            } else {
                String cs = comboBox.getStyle();
                cs = cs.replaceAll("-fx-background-color:\\s*#[a-fA-F0-9]+", "-fx-background-color: #ffffff");
                cs = cs.replaceAll("-fx-text-fill:\\s*#[a-fA-F0-9]+", "-fx-text-fill: #1a1a1a");
                cs = cs.replaceAll("-fx-border-color:\\s*#[a-fA-F0-9]+", "-fx-border-color: #cccccc");
                comboBox.setStyle(cs);
            }
        }
        
        for (javafx.scene.Node child : getAllChildren(node)) {
            applyThemeColors(child, isDark);
        }
    }

    private java.util.List<javafx.scene.Node> getAllChildren(javafx.scene.Node node) {
        java.util.List<javafx.scene.Node> children = new java.util.ArrayList<>();
        if (node instanceof javafx.scene.layout.Pane pane) {
            for (javafx.scene.Node child : pane.getChildren()) {
                children.add(child);
            }
        } else if (node instanceof javafx.scene.control.Labeled labeled && labeled.getContentDisplay() == javafx.scene.control.ContentDisplay.GRAPHIC_ONLY) {
            if (labeled.getGraphic() != null) children.add(labeled.getGraphic());
        } else if (node instanceof javafx.scene.Parent parent) {
            for (javafx.scene.Node child : parent.getChildrenUnmodifiable()) {
                children.add(child);
            }
        }
        return children;
    }

    @FXML
    public void applyLanguage() {
        String language = languageSelector.getValue();
        if (language != null) {
            Map<String, Object> expConfig = new HashMap<>(configManager.getConfig().experimental());
            expConfig.put("language", language);
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

    // ===== UTILITY =====

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
