package ai.opencode.gui.theme;

import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.layout.Pane;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.Labeled;
import javafx.scene.control.ContentDisplay;
import javafx.scene.text.Text;
import javafx.scene.control.Separator;

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
        
        // Région — fond ET bordure
        if (node instanceof javafx.scene.layout.Region region) {
            updateRegionBackground(region, isDark);
            updateBorderColor(region, isDark);
        }
        
        // Label — couleur texte
        if (node instanceof Label label) {
            updateLabelTextColor(label, isDark);
        }
        
        // ComboBox — fond, texte, bordure
        if (node instanceof ComboBox<?> comboBox) {
            updateComboBoxStyle(comboBox, isDark);
        }
        
        // Text — couleur du texte
        if (node instanceof Text textNode) {
            updateTextTextColor(textNode, isDark);
        }
        
        // TextField / PasswordField — couleur texte
        if (node instanceof TextField textField) {
            updateTextFieldColor(textField, isDark);
        }
        if (node instanceof PasswordField passwordField) {
            updateTextFieldColor(passwordField, isDark);
        }
        
        // TextArea — couleur texte
        if (node instanceof TextArea textArea) {
            updateTextAreaColor(textArea, isDark);
        }
        
        // Button — couleur texte
        if (node instanceof Button button) {
            updateButtonTextColor(button, isDark);
        }
        
       // CheckBox — couleur texte
        if (node instanceof CheckBox checkBox) {
            updateCheckboxTextColor(checkBox, isDark);
        }
        
        // Separator — couleur de la ligne séparatrice
        if (node instanceof Separator separator) {
            updateSeparatorColor(separator, isDark);
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
        
      // Déterminer la nouvelle couleur de fond en fonction du thème
        Color newBgColor;
        if (isDark) {
            if (currentStyle.contains("#ffffff") || currentStyle.contains("#f5f5f5") || 
                currentStyle.contains("#f0f0f0") || currentStyle.contains("transparent")) {
                newBgColor = Color.web("#1a1a1a");
            } else if (currentStyle.matches("(?i).*#[3-7][0-9a-fA-F]{5,6}.*") ||
                       currentStyle.contains("#2d2d2d")) {
                newBgColor = Color.web("#2d2d2d");
            } else {
                LOGGER.fine("updateRegionBackground: no dark color to convert for " + region.getClass().getSimpleName());
                return;
            }
        } else {
            if (currentStyle.contains("#1a1a1a") || currentStyle.contains("#2d2d2d") ||
                currentStyle.contains("#3c3c3c") || currentStyle.contains("#444444")) {
                newBgColor = Color.WHITE;
            } else if (currentStyle.contains("#cccccc") || currentStyle.contains("#e8e8e8")) {
                newBgColor = Color.web("#e8e8e8");
            } else {
                LOGGER.fine("updateRegionBackground: no dark color to convert for " + region.getClass().getSimpleName());
                return;
            }
        }
        
        region.setBackground(new javafx.scene.layout.Background(
            new javafx.scene.layout.BackgroundFill(newBgColor, 
                javafx.scene.layout.CornerRadii.EMPTY, 
                javafx.geometry.Insets.EMPTY)));
        // Forcer la mise à jour visuelle IMMÉDIATE
        region.applyCss();
    }

   private void updateBorderColor(javafx.scene.layout.Region region, boolean isDark) {
        String cs = region.getStyle() != null ? region.getStyle() : "";
        if (!cs.contains("-fx-border-color")) return;
        
        Color bc;
        if (isDark && (cs.contains("white") || cs.contains("#444444") || cs.contains("#555555") || cs.contains("#cccccc"))) {
            bc = Color.web("#444444");
        } else if (!isDark && (cs.contains("#2d2d2d") || cs.contains("#3c3c3c") || cs.contains("#444444") || cs.contains("#555555"))) {
            bc = Color.web("#cccccc");
        } else {
            return;
        }
        
        // Utiliser setStyle pour les bordures — plus simple et fiable que Border API
        String newStyle = cs.replaceAll("-fx-border-color:\\s*#[a-fA-F0-9]+", "-fx-border-color: " + 
                (isDark ? "#444444" : "#cccccc"));
        newStyle = newStyle.replaceAll("-fx-border-color:\\s*white\\b", "-fx-border-color: " + 
                (isDark ? "#555555" : "#bbbbbb"));
        region.setStyle(newStyle);
    }

    private void updateLabelTextColor(Label label, boolean isDark) {
        String style = label.getStyle() != null ? label.getStyle() : "";
        
        // Capturer le textFill actuel (attribut FXML textFill="...") pour l'ajouter au style inline
        javafx.scene.paint.Paint currentTextFill = label.getTextFill();
        if (currentTextFill instanceof Color colorFill && !style.contains("-fx-text-fill")) {
            String hex = String.format("%06x", 
                (int)(colorFill.getRed() * 255),
                (int)(colorFill.getGreen() * 255),
                (int)(colorFill.getBlue() * 255));
            style = "-fx-text-fill: #" + hex + "; " + style;
        }
        
        if (isDark) {
            // === THÈME SOMBRE — tous les textes → clair ===
            style = style.replaceAll("-fx-text-fill:\\s*#[a-fA-F0-9]{6}", "-fx-text-fill: #cccccc");
            style = style.replaceAll("-fx-text-fill:\\s*white(?=[^;]*[^a-z]|$)", "-fx-text-fill: #cccccc");
            style = style.replaceAll("-fx-text-fill:\\s*#ffffff\\s*", "-fx-text-fill: #cccccc");
            style = style.replaceAll("-fx-text-fill:\\s*derive\\(-fx-control-inner-background, \\+6%\\)", "-fx-text-fill: derive(-fx-control-inner-background, +45%)");
            style = style.replaceAll("-fx-text-fill:\\s*derive\\(-fx-control-inner-background, \\+18%\\)", "-fx-text-fill: derive(-fx-control-inner-background, +70%)");
            style = style.replaceAll("-fx-text-fill:\\s*derive\\(-fx-control-inner-background, \\+60%\\)", "-fx-text-fill: derive(-fx-control-inner-background, +80%)");
            style = style.replaceAll("-fx-text-fill:\\s*derive\\(-fx-control-inner-background, \\+65%\\)", "-fx-text-fill: derive(-fx-control-inner-background, +80%)");
            style = style.replaceAll("-fx-text-fill:\\s*derive\\(-fx-control-inner-background, \\+70%\\)", "-fx-text-fill: derive(-fx-control-inner-background, +90%)");
        } else {
            // === THÈME CLAIR — tous les textes → foncé ===
            style = style.replaceAll("-fx-text-fill:\\s*#[a-fA-F0-9]{6}", "-fx-text-fill: #333333");
            style = style.replaceAll("-fx-text-fill:\\s*white(?=[^;]*[^a-z]|$)", "-fx-text-fill: #1a1a1a");
            style = style.replaceAll("-fx-text-fill:\\s*#cccccc\\b", "-fx-text-fill: #444444");
            style = style.replaceAll("-fx-text-fill:\\s*#666666\\b", "-fx-text-fill: #555555");
            style = style.replaceAll("-fx-text-fill:\\s*derive\\(-fx-control-inner-background, \\+6%\\)", "-fx-text-fill: derive(-fx-control-inner-background, +20%)");
            style = style.replaceAll("-fx-text-fill:\\s*derive\\(-fx-control-inner-background, \\+18%\\)", "-fx-text-fill: derive(-fx-control-inner-background, +30%)");
            style = style.replaceAll("-fx-text-fill:\\s*derive\\(-fx-control-inner-background, \\+60%\\)", "-fx-text-fill: derive(-fx-control-inner-background, +70%)");
            style = style.replaceAll("-fx-text-fill:\\s*derive\\(-fx-control-inner-background, \\+65%\\)", "-fx-text-fill: derive(-fx-control-inner-background, +70%)");
        }
        
        label.setStyle(style);
    }

    @SuppressWarnings("unchecked")
    private void updateComboBoxStyle(ComboBox<?> comboBox, boolean isDark) {
        String cs = comboBox.getStyle() != null ? comboBox.getStyle() : "";
        
        if (isDark) {
            cs = cs.replaceAll("-fx-background-color:\\s*#[a-fA-F0-9]+", "-fx-background-color: #2d2d2d");
            cs = cs.replaceAll("-fx-text-fill:\\s*#[a-fA-F0-9]+", "-fx-text-fill: white");
            cs = cs.replaceAll("-fx-text-fill:\\s*white(?=[^;]*[^a-z]|$)", "-fx-text-fill: white");
            cs = cs.replaceAll("-fx-border-color:\\s*#[a-fA-F0-9]+", "-fx-border-color: #444444");
        } else {
            cs = cs.replaceAll("-fx-background-color:\\s*#[a-fA-F0-9]+", "-fx-background-color: #ffffff");
            cs = cs.replaceAll("-fx-text-fill:\\s*#[a-fA-F0-9]+", "-fx-text-fill: #1a1a1a");
            cs = cs.replaceAll("-fx-text-fill:\\s*white(?=[^;]*[^a-z]|$)", "-fx-text-fill: #1a1a1a");
            cs = cs.replaceAll("-fx-border-color:\\s*#[a-fA-F0-9]+", "-fx-border-color: #cccccc");
        }
        
        comboBox.setStyle(cs);
    }

    private void updateTextTextColor(Text textNode, boolean isDark) {
        if (isDark) {
            textNode.setFill(Color.WHITE);
        } else {
            textNode.setFill(Color.web("#1a1a1a"));
        }
    }

    private void updateTextFieldColor(TextField field, boolean isDark) {
        String cs = field.getStyle() != null ? field.getStyle() : "";
        if (isDark) {
            cs = cs.replaceAll("-fx-text-fill:\\s*#[a-fA-F0-9]+", "-fx-text-fill: white");
            cs = cs.replaceAll("-fx-text-fill:\\s*black\\b", "-fx-text-fill: white");
        } else {
            cs = cs.replaceAll("-fx-text-fill:\\s*#[a-fA-F0-9]+", "-fx-text-fill: #1a1a1a");
            cs = cs.replaceAll("-fx-text-fill:\\s*white\\b", "-fx-text-fill: #1a1a1a");
        }
        field.setStyle(cs);
    }

    private void updateTextAreaColor(TextArea area, boolean isDark) {
        String cs = area.getStyle() != null ? area.getStyle() : "";
        if (isDark) {
            cs = cs.replaceAll("-fx-text-fill:\\s*#[a-fA-F0-9]+", "-fx-text-fill: white");
            cs = cs.replaceAll("-fx-text-fill:\\s*black\\b", "-fx-text-fill: white");
        } else {
            cs = cs.replaceAll("-fx-text-fill:\\s*#[a-fA-F0-9]+", "-fx-text-fill: #1a1a1a");
            cs = cs.replaceAll("-fx-text-fill:\\s*white\\b", "-fx-text-fill: #1a1a1a");
        }
        area.setStyle(cs);
    }

    private void updateButtonTextColor(Button button, boolean isDark) {
        String cs = button.getStyle() != null ? button.getStyle() : "";
        if (isDark) {
            cs = cs.replaceAll("-fx-text-fill:\\s*#[a-fA-F0-9]+", "-fx-text-fill: white");
            cs = cs.replaceAll("-fx-text-fill:\\s*black\\b", "-fx-text-fill: white");
        } else {
            cs = cs.replaceAll("-fx-text-fill:\\s*#[a-fA-F0-9]+", "-fx-text-fill: #ffffff");
            cs = cs.replaceAll("-fx-text-fill:\\s*white\\b", "-fx-text-fill: #ffffff");
        }
        button.setStyle(cs);
    }

  private void updateCheckboxTextColor(CheckBox checkBox, boolean isDark) {
        String cs = checkBox.getStyle() != null ? checkBox.getStyle() : "";
        if (isDark) {
            cs = cs.replaceAll("-fx-text-fill:\\s*#[a-fA-F0-9]+", "-fx-text-fill: white");
            cs = cs.replaceAll("-fx-text-fill:\\s*black\\b", "-fx-text-fill: white");
        } else {
            cs = cs.replaceAll("-fx-text-fill:\\s*#[a-fA-F0-9]+", "-fx-text-fill: #1a1a1a");
            cs = cs.replaceAll("-fx-text-fill:\\s*white\\b", "-fx-text-fill: #1a1a1a");
        }
        checkBox.setStyle(cs);
    }

    private void updateSeparatorColor(Separator separator, boolean isDark) {
        String cs = separator.getStyle() != null ? separator.getStyle() : "";
        
        // Les séparateurs JavaFX utilisent -fx-background-color pour la ligne
        if (cs.contains("-fx-background-color")) {
            if (isDark) {
                cs = cs.replaceAll("-fx-background-color:\\s*#[Aa][Bb][Cc][Dd][Ee][Ff][^;]*", "-fx-background-color: #444444");
                cs = cs.replaceAll("-fx-background-color:\\s*#cccccc[^;]*", "-fx-background-color: #444444");
                cs = cs.replaceAll("-fx-background-color:\\s*e8e8e8[^;]*", "-fx-background-color: #444444");
            } else {
                cs = cs.replaceAll("-fx-background-color:\\s*#[3-7][0-9a-fA-F]{5,6}[^;]*", "-fx-background-color: #cccccc");
                cs = cs.replaceAll("-fx-background-color:\\s*#444444[^;]*", "-fx-background-color: #cccccc");
                cs = cs.replaceAll("-fx-background-color:\\s*#2d2d2d[^;]*", "-fx-background-color: #dddddd");
            }
            separator.setStyle(cs);
        }
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
