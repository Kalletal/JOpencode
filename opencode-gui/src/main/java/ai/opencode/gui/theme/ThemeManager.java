package ai.opencode.gui.theme;

import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.layout.Pane;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Labeled;
import javafx.scene.control.ContentDisplay;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * Gestionnaire centralisé du thème visuel de l'application.
 */
public class ThemeManager {
    private static final Logger LOGGER = Logger.getLogger(ThemeManager.class.getName());
    
    private String darkCssPath;
    private String lightCssPath;

    public ThemeManager() {
        try {
            Class<?> clazz = getClass();
            var darkUrl = clazz.getResource("/css/style.css");
            var lightUrl = clazz.getResource("/css/style-light.css");
            if (darkUrl != null) {
                darkCssPath = darkUrl.toExternalForm();
            } else {
                LOGGER.warning("CSS sombre non trouvé: /css/style.css");
            }
            if (lightUrl != null) {
                lightCssPath = lightUrl.toExternalForm();
            } else {
                LOGGER.warning("CSS clair non trouvé: /css/style-light.css");
            }
        } catch (Exception e) {
            LOGGER.severe("Erreur lors du chargement des chemins CSS: " + e.getMessage());
        }
    }

    /** Swap les feuilles de styles CSS de la scène. */
    public void swapStylesheets(Scene scene, boolean isDark) {
        if (scene == null) return;
        
        scene.getStylesheets().clear();
        
        try {
            if (isDark && darkCssPath != null) {
                scene.getStylesheets().add(darkCssPath);
            } else if (!isDark && lightCssPath != null) {
                scene.getStylesheets().add(lightCssPath);
            } else {
                LOGGER.warning(isDark ? "CSS sombre manquant" : "CSS clair manquant");
            }
        } catch (Exception e) {
            LOGGER.severe("Erreur lors du chargement des stylesheets: " + e.getMessage());
        }
    }

    /** Applique un thème à la scène donnée. */
    public void applyTheme(Scene scene, String themeName) {
        if (scene == null || scene.getRoot() == null) return;
        
        boolean isDark = !"Clair".equals(themeName);
        LOGGER.info("=== applyTheme: '" + themeName + "' (isDark=" + isDark + ") ===");
        swapStylesheets(scene, isDark);
        
        javafx.application.Platform.runLater(() -> {
            AtomicInteger counter = new AtomicInteger(0);
            AtomicInteger wtCounter = new AtomicInteger(0);
            traverseAndApply(scene.getRoot(), isDark, counter, wtCounter);
            LOGGER.info("applyTheme completed: " + counter.get() + " nodes visited, " + wtCounter.get() + " WelcomeTitle found/updated");
        });
    }

    private void traverseAndApply(Node node, boolean isDark, AtomicInteger totalNodes, AtomicInteger welcomeCount) {
        if (node == null) return;
        totalNodes.incrementAndGet();
        
        // Région — fond
        if (node instanceof javafx.scene.layout.Region region) {
            updateRegionBackground(region, isDark);
        }
        
        // Label — couleur texte
        if (node instanceof Label label) {
            updateLabelTextColor(label, isDark);
        }
        
        // ComboBox — fond, texte, bordure
        if (node instanceof ComboBox<?> comboBox) {
            updateComboBoxStyle(comboBox, isDark);
        }
        
        // Pane personnalisé — rafraîchir les couleurs internes
        if (refreshCustomPaneColors(node, isDark)) {
            welcomeCount.incrementAndGet();
            LOGGER.info("WelcomeTitle updated! isDark=" + isDark);
        }
        
        // Récursion enfants
        for (Node child : getAllChildren(node)) {
            traverseAndApply(child, isDark, totalNodes, welcomeCount);
        }
    }

    /** Retourne true si un WelcomeTitle a été trouvé et mis à jour */
    @SuppressWarnings("unchecked")
    private boolean refreshCustomPaneColors(Node node, boolean isDark) {
        String className = node.getClass().getName();
        if (className.contains("WelcomeTitle")) {
            try {
                java.lang.reflect.Method m = node.getClass().getMethod("updateForTheme", boolean.class);
                m.invoke(node, isDark);
                LOGGER.info("WelcomeTitle.updateForTheme(isDark=" + isDark + ") appelé par reflection — toutes les couleurs mises à jour");
                return true;
            } catch (Exception e) {
                LOGGER.severe("Impossible d'appeler updateForTheme sur WelcomeTitle: " + e.getMessage() + " | Classe: " + className);
            }
        }
        return false;
    }

    private void updateRegionBackground(javafx.scene.layout.Region region, boolean isDark) {
        String currentStyle = region.getStyle() != null ? region.getStyle() : "";
        if (!currentStyle.contains("-fx-background-color")) return;
        
        if (isDark) {
            currentStyle = currentStyle.replaceAll("-fx-background-color:\\s*#ffffff[^;]*", "-fx-background-color: #1a1a1a");
            currentStyle = currentStyle.replaceAll("-fx-background-color:\\s*#f5f5f5[^;]*", "-fx-background-color: #1a1a1a");
            currentStyle = currentStyle.replaceAll("-fx-background-color:\\s*#f0f0f0[^;]*", "-fx-background-color: #1a1a1a");
            currentStyle = currentStyle.replaceAll("-fx-background-color:\\s*#e8e8e8[^;]*", "-fx-background-color: transparent");
            currentStyle = currentStyle.replaceAll("-fx-background-color:\\s*#2d2d2d[^;]*", "-fx-background-color: #2d2d2d");
        } else {
            currentStyle = currentStyle.replaceAll("-fx-background-color:\\s*#1a1a1a[^;]*", "-fx-background-color: #f0f0f0");
            currentStyle = currentStyle.replaceAll("-fx-background-color:\\s*#2d2d2d[^;]*", "-fx-background-color: #2d2d2d");
            currentStyle = currentStyle.replaceAll("-fx-background-color:\\s*transparent(?!;)", "-fx-background-color: transparent");
        }
        
        region.setStyle(currentStyle);
    }

    private void updateLabelTextColor(Label label, boolean isDark) {
        String style = label.getStyle() != null ? label.getStyle() : "";
        
        if (isDark) {
            style = style.replaceAll("-fx-text-fill:\\s*#[a-fA-F0-9]{6}", "-fx-text-fill: #cccccc");
            style = style.replaceAll("-fx-text-fill:\\s*white\\b", "-fx-text-fill: #cccccc");
            style = style.replaceAll("-fx-text-fill:\\s*#ffffff\\s*", "-fx-text-fill: #cccccc");
        } else {
            style = style.replaceAll("-fx-text-fill:\\s*#[a-fA-F0-9]{6}", "-fx-text-fill: #333333");
            style = style.replaceAll("-fx-text-fill:\\s*black\\b", "-fx-text-fill: #1a1a1a");
            style = style.replaceAll("-fx-text-fill:\\s*#000000\\s*", "-fx-text-fill: #1a1a1a");
        }
        
        label.setStyle(style);
    }

    @SuppressWarnings("unchecked")
    private void updateComboBoxStyle(ComboBox<?> comboBox, boolean isDark) {
        String cs = comboBox.getStyle() != null ? comboBox.getStyle() : "";
        
        if (isDark) {
            cs = cs.replaceAll("-fx-background-color:\\s*#[a-fA-F0-9]+", "-fx-background-color: #2d2d2d");
            cs = cs.replaceAll("-fx-text-fill:\\s*#[a-fA-F0-9]+", "-fx-text-fill: white");
            cs = cs.replaceAll("-fx-border-color:\\s*#[a-fA-F0-9]+", "-fx-border-color: #444444");
        } else {
            cs = cs.replaceAll("-fx-background-color:\\s*#[a-fA-F0-9]+", "-fx-background-color: #ffffff");
            cs = cs.replaceAll("-fx-text-fill:\\s*#[a-fA-F0-9]+", "-fx-text-fill: #1a1a1a");
            cs = cs.replaceAll("-fx-border-color:\\s*#[a-fA-F0-9]+", "-fx-border-color: #cccccc");
        }
        
        comboBox.setStyle(cs);
    }

    private List<Node> getAllChildren(Node node) {
        List<Node> children = new ArrayList<>();
        
        if (node instanceof Pane pane) {
            for (Node child : pane.getChildren()) {
                children.add(child);
            }
        } else if (node instanceof Labeled labeled && labeled.getContentDisplay() == ContentDisplay.GRAPHIC_ONLY) {
            Node graphic = labeled.getGraphic();
            if (graphic != null) children.add(graphic);
        } else if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                children.add(child);
            }
        }
        
        return children;
    }
}
