package ai.opencode.gui.i18n;

import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Singleton global d'accès aux traductions dans toute l'application.
 * Permet à n'importe quel contrôleur de récupérer des traductions
 * via getString(key) sans avoir à injecter le LanguageManager.
 */
public class GlobalLanguageController {
    private static final Logger LOGGER = Logger.getLogger(GlobalLanguageController.class.getName());

    private static volatile GlobalLanguageController instance;
    private ResourceBundle i18nBundle;
    private LanguageManager languageManager;

    // Constructeur privé pour le pattern singleton
    private GlobalLanguageController() {}

    /**
     * Obtient l'instance unique du contrôleur (double-checked locking).
     */
    public static GlobalLanguageController getInstance() {
        if (instance == null) {
            synchronized (GlobalLanguageController.class) {
                if (instance == null) {
                    instance = new GlobalLanguageController();
                }
            }
        }
        return instance;
    }

    /**
     * Initialise le contrôleur avec un LanguageManager pré-configuré.
     */
    public void init(LanguageManager langMgr) {
        this.languageManager = langMgr;
        this.i18nBundle = langMgr.getResourceBundle();
        LOGGER.info("GlobalLanguageController initialisé");
    }

    /**
     * Récupère une traduction par clé depuis n'importe quel contrôleur.
     */
    public static String getString(String key) {
        if (getInstance().i18nBundle != null) {
            try {
                return getInstance().i18nBundle.getString(key);
            } catch (java.util.MissingResourceException e) {
                LOGGER.fine("Clé manquante : " + key);
                return key;
            }
        }
        // Fallback : utiliser la clé brute si le bundle n'est pas chargé
        return key;
    }

    /**
     * Force le rechargement du bundle de traduction.
     */
    public void reloadBundle() {
        if (languageManager != null) {
            this.i18nBundle = languageManager.getResourceBundle();
        }
    }

    /**
     * Vérifie si la langue courante est RTL.
     */
    public static boolean isRTL() {
        GlobalLanguageController controller = getInstance();
        return controller.languageManager != null && controller.languageManager.isRTL();
    }

    /**
     * Obtient le code locale actuel.
     */
    public static String getCurrentLocaleCode() {
        GlobalLanguageController controller = getInstance();
        if (controller.languageManager != null && controller.languageManager.getCurrentLocale() != null) {
            return controller.languageManager.getCurrentLocale().toLanguageTag();
        }
        return "fr";
    }
}
