package ai.opencode.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gère la persistance de la configuration de l'application dans un fichier JSON.
 * Ce manager permet de charger la configuration au démarrage et de la sauvegarder
 * lors de modifications via l'interface graphique.
 */
public class ConfigManager {
    private static final Logger LOGGER = Logger.getLogger(ConfigManager.class.getName());
    private static final String CONFIG_FILE_NAME = "config.json";
    
    private final ObjectMapper objectMapper;
    private final File configFile;
    private AppConfig currentConfig;

    public ConfigManager() {
        this.objectMapper = new ObjectMapper();
        // Active l'indentation pour que le fichier JSON soit lisible par l'humain
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        
        // Le fichier est stocké dans le répertoire utilisateur (.opencode/config.json)
        String userHome = System.getProperty("user.home");
        this.configFile = Paths.get(userHome, ".opencode", CONFIG_FILE_NAME).toFile();
        
        loadConfig();
    }

    /**
     * Charge la configuration depuis le disque. 
     * Si le fichier n'existe pas, une configuration par défaut est créée.
     */
    public void loadConfig() {
        try {
            if (configFile.exists()) {
                currentConfig = objectMapper.readValue(configFile, AppConfig.class);
                LOGGER.info("Configuration chargée avec succès depuis " + configFile.getAbsolutePath());
            } else {
                currentConfig = AppConfig.createDefault();
                saveConfig();
                LOGGER.info("Aucun fichier de configuration trouvé. Création d'un fichier par défaut.");
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement de la configuration, utilisation des valeurs par défaut", e);
            currentConfig = AppConfig.createDefault();
        }
    }

    /**
     * Sauvegarde la configuration actuelle sur le disque.
     */
    public void saveConfig() {
        try {
            // Création du dossier parent si nécessaire (.opencode)
            File parentDir = configFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            objectMapper.writeValue(configFile, currentConfig);
            LOGGER.info("Configuration sauvegardée avec succès.");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la sauvegarde de la configuration", e);
        }
    }

    public AppConfig getConfig() {
        return currentConfig;
    }

    public void setConfig(AppConfig newConfig) {
        this.currentConfig = newConfig;
        saveConfig();
    }
}
