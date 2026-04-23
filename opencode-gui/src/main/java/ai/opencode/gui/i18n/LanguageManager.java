package ai.opencode.gui.i18n;

import java.util.*;
import java.util.logging.Logger;

/**
 * Gestionnaire centralisé de localisation pour l'application Opencode.
 * Charge les ResourceBundle depuis /i18n/messages*.properties et fournit
 * des méthodes utilitaires pour récupérer les traductions par clé.
 */
public class LanguageManager {
    private static final Logger LOGGER = Logger.getLogger(LanguageManager.class.getName());

    // Mapping entre le nom d'affichage dans le ComboBox et le code langue ResourceBundle
    private static final Map<String, String> DISPLAY_TO_LOCALE = new LinkedHashMap<>();
    static {
        DISPLAY_TO_LOCALE.put("English", "en");
        DISPLAY_TO_LOCALE.put("中文（普通话）", "zh");
        DISPLAY_TO_LOCALE.put("हिन्दी", "hi");
        DISPLAY_TO_LOCALE.put("Español", "es");
        DISPLAY_TO_LOCALE.put("Français", "fr");
        DISPLAY_TO_LOCALE.put("العربية", "ar");
        DISPLAY_TO_LOCALE.put("বাংলা", "bn");
        DISPLAY_TO_LOCALE.put("Português", "pt");
        DISPLAY_TO_LOCALE.put("Русский", "ru");
        DISPLAY_TO_LOCALE.put("اردو", "ur");
    }

    private ResourceBundle i18nBundle;
    private Locale currentLocale;
    private String displayLanguageName;

    /**
     * Charge un bundle de traduction à partir du code langue (ex: "en", "fr").
     * Le fallback se fait automatiquement vers messages.properties (Français).
     */
    public ResourceBundle loadBundle(String languageCode) {
        try {
            this.i18nBundle = ResourceBundle.getBundle("i18n.messages", Locale.forLanguageTag(languageCode));
            this.currentLocale = Locale.forLanguageTag(languageCode);
            LOGGER.info("Bundle chargé pour la langue : " + languageCode);
        } catch (MissingResourceException e) {
            // Fallback automatique vers le bundle par défaut (messages.properties / Français)
            LOGGER.warning("Bundle non trouvé pour '" + languageCode + "', fallback vers Français. Erreur : " + e.getMessage());
            try {
                this.i18nBundle = ResourceBundle.getBundle("i18n.messages", Locale.FRENCH);
                this.currentLocale = Locale.FRENCH;
            } catch (MissingResourceException ex) {
                LOGGER.severe("Impossible de charger le bundle de fallback (Français) : " + ex.getMessage());
                this.i18nBundle = null;
            }
        }
        return i18nBundle;
    }

    /**
     * Charge un bundle à partir du nom d'affichage (ex: "English" -> "en").
     */
    public ResourceBundle loadBundleByDisplayName(String displayName) {
        String localeCode = DISPLAY_TO_LOCALE.get(displayName);
        if (localeCode == null) {
            LOGGER.warning("Nom d'affichage inconnu '" + displayName + "', fallback vers Français");
            return loadBundle("fr");
        }
        this.displayLanguageName = displayName;
        return loadBundle(localeCode);
    }

    /**
     * Récupère une traduction par clé. Retourne la clé elle-même si non trouvée.
     */
    public String getTranslation(String key) {
        if (i18nBundle == null) {
            LOGGER.warning("Bundle non chargé, fallback sur la clé brute : " + key);
            return key;
        }
        try {
            return i18nBundle.getString(key);
        } catch (MissingResourceException e) {
            // La clé n'existe pas dans le bundle — retourner la clé brute comme dernier recours
            LOGGER.fine("Clé de traduction manquante : " + key);
            return key;
        }
    }

    /**
     * Vérifie si la langue courante est RTL (Arabe / Ourdou).
     */
    public boolean isRTL() {
        return currentLocale != null && ("ar".equals(currentLocale.getLanguage()) || "ur".equals(currentLocale.getLanguage()));
    }

    public Locale getCurrentLocale() {
        return currentLocale;
    }

    /**
     * Obtient le nom d'affichage actuel de la langue.
     */
    public String getDisplayLanguageName() {
        return displayLanguageName;
    }

    /**
     * Retourne tous les noms d'affichage des langues disponibles (ordre du ComboBox).
     */
    public static List<String> getAvailableDisplayNames() {
        return new ArrayList<>(DISPLAY_TO_LOCALE.keySet());
    }

    /**
     * Retourne le code locale pour un nom d'affichage donné.
     */
    public static String toLocaleCode(String displayName) {
        return DISPLAY_TO_LOCALE.get(displayName);
    }

    /**
     * Factory method statique — crée et charge un bundle pour une Locale donnée.
     */
    public static LanguageManager create(Locale locale) {
        LanguageManager manager = new LanguageManager();
        manager.loadBundle(locale.toLanguageTag());
        // Trouver le nom d'affichage correspondant
        for (Map.Entry<String, String> entry : DISPLAY_TO_LOCALE.entrySet()) {
            if (entry.getValue().equals(locale.getLanguage())) {
                manager.displayLanguageName = entry.getKey();
                break;
            }
        }
        return manager;
    }

    public ResourceBundle getResourceBundle() {
        return i18nBundle;
    }
}
