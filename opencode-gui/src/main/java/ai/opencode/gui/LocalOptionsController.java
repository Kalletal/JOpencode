package ai.opencode.gui;

import ai.opencode.storage.AppConfig;
import ai.opencode.storage.ConfigManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.util.HashMap;
import java.util.Map;

/**
 * Contrôleur pour la fenêtre des options avancées du modèle local.
 */
public class LocalOptionsController {
    @FXML private ComboBox<String> compactionCombo;
    @FXML private CheckBox readAccessCheck;
    @FXML private CheckBox writeAccessCheck;
    @FXML private TextField mcpConfigField;
    @FXML private CheckBox toolsActivationCheck;

    private Stage stage;
    private ConfigManager configManager;

    public void initialize() {
        compactionCombo.getItems().addAll("Aucune", "Faible", "Moyenne", "Forte", "Extreme");
        
        // Valeurs par défaut si configManager n'est pas encore injecté
        compactionCombo.getSelectionModel().select(1);
        readAccessCheck.setSelected(true);
        writeAccessCheck.setSelected(false);
        toolsActivationCheck.setSelected(true);
    }

    public void setConfigManager(ConfigManager configManager) {
        this.configManager = configManager;
        loadSettings();
    }

    private void loadSettings() {
        if (configManager == null) return;
        
        AppConfig config = configManager.getConfig();
        Map<String, Object> exp = config.experimental();
        
        if (exp.containsKey("local_compaction")) {
            compactionCombo.setValue((String) exp.get("local_compaction"));
        }
        if (exp.containsKey("local_read_access")) {
            readAccessCheck.setSelected((Boolean) exp.get("local_read_access"));
        }
        if (exp.containsKey("local_write_access")) {
            writeAccessCheck.setSelected((Boolean) exp.get("local_write_access"));
        }
        if (exp.containsKey("local_mcp_config")) {
            mcpConfigField.setText((String) exp.get("local_mcp_config"));
        }
        if (exp.containsKey("local_tools_active")) {
            toolsActivationCheck.setSelected((Boolean) exp.get("local_tools_active"));
        }
    }

    @FXML
    public void save() {
        if (configManager == null) {
            System.err.println("ConfigManager non initialisé");
            close();
            return;
        }

        AppConfig oldConfig = configManager.getConfig();
        Map<String, Object> newExperimental = new HashMap<>(oldConfig.experimental());
        
        newExperimental.put("local_compaction", compactionCombo.getValue());
        newExperimental.put("local_read_access", readAccessCheck.isSelected());
        newExperimental.put("local_write_access", writeAccessCheck.isSelected());
        newExperimental.put("local_mcp_config", mcpConfigField.getText());
        newExperimental.put("local_tools_active", toolsActivationCheck.isSelected());

        AppConfig newConfig = new AppConfig(
            oldConfig.username(),
            oldConfig.defaultAgent(),
            oldConfig.agents(),
            oldConfig.apiKeys(),
            newExperimental,
            oldConfig.thinkingShortcut()
        );

        configManager.setConfig(newConfig);
        close();
    }

    @FXML
    public void cancel() {
        close();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private void close() {
        if (stage != null) {
            stage.close();
        }
    }
}
