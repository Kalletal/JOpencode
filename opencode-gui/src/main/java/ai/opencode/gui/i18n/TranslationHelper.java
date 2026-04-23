package ai.opencode.gui.i18n;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Utilitaire statique pour appliquer les traductions du ResourceBundle
 * sur l'arborescence de nœuds JavaFX. Supporte deux mécanismes :
 * 1. Map explicite peuplée par le contrôleur via registerTranslation()
 * 2. Parcours récursif standard des Labels dans un VBox/HBox
 */
public class TranslationHelper {
    private static final Logger LOGGER = Logger.getLogger(TranslationHelper.class.getName());

    // Map Label -> clé i18n, peuplée par le contrôleur
    private static final Map<Label, String> LABEL_TO_I18N_KEY = new ConcurrentHashMap<>();

    /**
     * Applique les traductions récursivement à tous les Labels dans le panneau donné.
     */
    public static void applyTranslations(Node parent, ResourceBundle bundle) {
        if (parent == null || bundle == null) return;

        if (parent instanceof Label label) {
            String key = resolveKey(label);
            if (key != null && !key.isEmpty()) {
                try {
                    String translation = bundle.getString(key);
                    label.setText(translation);
                } catch (java.util.MissingResourceException e) {
                    LOGGER.fine("Clé manquante : " + key);
                }
            }
        }

        // Récursion sur les enfants
        if (parent instanceof Parent p) {
            for (Node child : p.getChildrenUnmodifiable()) {
                applyTranslations(child, bundle);
            }
        } else if (parent instanceof javafx.scene.control.Labeled labeled) {
            Node graphic = labeled.getGraphic();
            if (graphic != null) {
                applyTranslations(graphic, bundle);
            }
        }
    }

    /**
     * Traduit un Label unique par une clé donnée.
     */
    public static void translateLabel(Label label, ResourceBundle bundle, String key) {
        if (label == null || bundle == null || key == null) return;
        try {
            label.setText(bundle.getString(key));
        } catch (java.util.MissingResourceException e) {
            LOGGER.fine("Clé manquante : " + key);
        }
    }

    /**
     * Enregistre une correspondance explicite entre un Label et sa clé i18n.
     */
    public static void registerTranslation(Label label, String i18nKey) {
        if (label != null && i18nKey != null) {
            LABEL_TO_I18N_KEY.put(label, i18nKey);
        }
    }

    private static String resolveKey(Label label) {
        // 1. Vérifier la map explicite peuplée par le contrôleur
        String key = LABEL_TO_I18N_KEY.get(label);
        if (key != null) return key;

        // 2. Fallback : vérifier si l'ID du node (fx:id) est une clé valide dans le bundle
        // JavaFX ne met pas fx:id dans getId() automatiquement, mais on peut utiliser lookup
        return null;
    }
}
