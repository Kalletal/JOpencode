package ai.opencode.gui.components;

import javafx.animation.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.ClosePath;
import javafx.util.Duration;
import javafx.scene.media.AudioClip;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.Media;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class WelcomeTitle extends Pane {
    private static final Logger LOGGER = Logger.getLogger(WelcomeTitle.class.getName());

    private final Path masterClip = new Path();
    private final Pane rippleContainer = new Pane();
    private final Random random = new Random();
    
    // Chemins BAS par section — nécessaires pour updateForTheme()
    private final List<Path> leftBottomPaths = new ArrayList<>();
    private final List<Path> rightBottomPaths = new ArrayList<>();
    
    // Chemins HAUT par section — nécessaires pour updateForTheme()
    private final List<Path> leftTopPaths = new ArrayList<>();
    private final List<Path> rightTopPaths = new ArrayList<>();
    
    private AudioClip[] pulseSounds;
    private MediaPlayer chargePlayer;
    private PauseTransition longPressTimer;
    
    public WelcomeTitle() {
        setPrefSize(234, 84);
        setMinSize(234, 84);
        setMaxSize(234, 84); 

        rippleContainer.setPrefSize(468, 42);
        rippleContainer.setClip(masterClip);
        rippleContainer.setMouseTransparent(true);
        
        loadSounds();
        
        longPressTimer = new PauseTransition(Duration.millis(500));
        longPressTimer.setOnFinished(e -> {
            if (chargePlayer != null) {
                chargePlayer.setCycleCount(1);
                chargePlayer.setOnEndOfMedia(() -> playRandomPulse());
                chargePlayer.play();
            }
        });
        
        setOnMousePressed(e -> {
            if (!(e.getTarget() instanceof Path)) return;
            longPressTimer.play();
        });
        
        setOnMouseReleased(e -> {
            if (!(e.getTarget() instanceof Path)) return;
            
            longPressTimer.stop();
            if (chargePlayer != null) {
                chargePlayer.stop();
            }
            
            playRandomPulse();
            triggerRipple(e.getX(), e.getY());
        });
        
        // --- "OP" part (section GAUCHE) ---
        addBottomPath("M18 30H6V18H18V30Z", "left");
        addBottomPath("M48 30H36V18H48V30Z", "left");
        addBottomPath("M84 24V30H66V24H84Z", "left");
        addBottomPath("M108 36H96V18H108V36Z", "left");
        
        addPath("M18 12H6V30H18V12ZM24 36H0V6H24V36Z", "#656363", true, "left");
        addPath("M36 30H48V12H36V30ZM54 36H36V42H30V6H54V36Z", "#656363", true, "left");
        addPath("M84 24H66V30H84V36H60V6H84V24ZM66 18H78V12H66V18Z", "#656363", true, "left");
        addPath("M108 12H96V36H90V6H108V12ZM114 36H108V12H114V36Z", "#656363", true, "left");
        
        // --- "code" part (section DROITE) ---
        addBottomPath("M144 30H126V18H144V30Z", "right");
        addBottomPath("M168 30H156V18H168V30Z", "right");
        addBottomPath("M198 30H186V18H198V30Z", "right");
        addBottomPath("M234 24V30H216V24H234Z", "right");
        
        addPath("M144 12H126V30H144V36H120V6H144V12Z", "#FFFFFF", true, "right");
        addPath("M168 12H156V30H168V12ZM174 36H150V6H174V36Z", "#FFFFFF", true, "right");
        addPath("M198 12H186V30H198V12ZM204 36H180V6H198V0H204V36Z", "#FFFFFF", true, "right");
        addPath("M216 12V18H228V12H216ZM234 24H216V30H234V36H210V6H234V24Z", "#FFFFFF", true, "right");
        
        this.getChildren().add(rippleContainer);

        // Appliquer les couleurs initiales basées sur le thème sauvegardé
        applyInitialColors();
    }

    /** Lit config.json pour déterminer le thème initial et applique les couleurs correspondantes */
    private void applyInitialColors() {
        try {
            String home = System.getProperty("user.home");
            String configPath = Paths.get(home, ".opencode", "config.json").toString();
            boolean isDark = true; // défaut = sombre
            
            BufferedReader reader = new BufferedReader(new FileReader(configPath));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            
            String json = sb.toString();
            if (json.contains("\"theme\"")) {
                int themeIdx = json.indexOf("\"theme\"");
                int colonIdx = json.indexOf(":", themeIdx);
                int quoteStart = json.indexOf("\"", colonIdx);
                int quoteEnd = json.indexOf("\"", quoteStart + 1);
                String themeValue = json.substring(quoteStart + 1, quoteEnd).trim();
                isDark = !"Clair".equals(themeValue);
            }
            
            LOGGER.info("WelcomeTitle: thème détecté au démarrage = " + (isDark ? "Sombre" : "Clair"));
            updateForTheme(isDark);
        } catch (Exception e) {
            // Si impossible de lire config.json, utiliser le thème sombre par défaut
            LOGGER.fine("Impossible de lire config.json pour WelcomeTitle, utilisation du thème sombre par défaut. Erreur: " + e.getMessage());
            updateForTheme(true);
        }
    }

/** Applique les couleurs appropriées au thème courant — chaque section a ses propres couleurs */
    public void updateForTheme(boolean isDark) {
        if (isDark) {
            // ===== THÈME SOMBRE =====
            // Section GAUCHE ("OP") : HAUT or clair / BAS noir chaud
            for (Path p : leftTopPaths)       p.setFill(Color.web("#F5E6C8"));
            for (Path p : leftBottomPaths)     p.setFill(Color.web("#3B3634"));
            
            // Section DROITE ("CODE") : HAUT or / BAS noir très chaud
            for (Path p : rightTopPaths)      p.setFill(Color.web("#D4A843"));
            for (Path p : rightBottomPaths)   p.setFill(Color.web("#2A2220"));
        } else {
            // ===== THÈME CLAIR =====
            // Section GAUCHE ("OP") : HAUT bleu nuit profond / BAS gris moyen
            for (Path p : leftTopPaths)       p.setFill(Color.web("#2D3638"));
            for (Path p : leftBottomPaths)     p.setFill(Color.web("#8C8484"));
            
            // Section DROITE ("CODE") : HAUT bleu acier profond / BAS bleu-gris
            for (Path p : rightTopPaths)      p.setFill(Color.web("#1A2836"));
            for (Path p : rightBottomPaths)   p.setFill(Color.web("#7A8E9E"));
        }
    }

    private void loadSounds() {
        try {
            String[] pulseFiles = {"pulse-a.wav", "pulse-b.wav", "pulse-c.wav"};
            pulseSounds = new AudioClip[pulseFiles.length];
            for (int i = 0; i < pulseFiles.length; i++) {
                var res = getClass().getResource("/sounds/" + pulseFiles[i]);
                if (res != null) pulseSounds[i] = new AudioClip(res.toExternalForm());
            }

            var chargeRes = getClass().getResource("/sounds/charge.wav");
            if (chargeRes != null) {
                chargePlayer = new MediaPlayer(new Media(chargeRes.toExternalForm()));
            }
        } catch (Exception e) {
            // Fail silently
        }
    }

    private void playRandomPulse() {
        if (pulseSounds == null) return;
        int index = random.nextInt(pulseSounds.length);
        if (pulseSounds[index] != null) {
            pulseSounds[index].play();
        }
    }

    private void triggerRipple(double x, double y) {
        Circle ripple = new Circle(x, y, 0);
        ripple.setFill(Color.web("#FFA500", 1.0));
        ripple.setStroke(null);
        rippleContainer.getChildren().add(ripple);

        Timeline timeline = new Timeline();
        timeline.setCycleCount(1);
        
        KeyValue kvRadius = new KeyValue(ripple.radiusProperty(), 400, Interpolator.EASE_OUT);
        KeyValue kvOpacity = new KeyValue(ripple.opacityProperty(), 0, Interpolator.EASE_OUT);
        
        KeyFrame kf = new KeyFrame(Duration.millis(800), kvRadius, kvOpacity);
        timeline.getKeyFrames().add(kf);
        
        timeline.setOnFinished(event -> rippleContainer.getChildren().remove(ripple));
        timeline.play();
    }

    /** Crée un chemin de la partie basse et le stocke selon la section */
    private void addBottomPath(String d, String section) {
        Path path = createPath(d);
        path.setFill(Color.web("#3B3634"));
        if ("left".equals(section)) {
            leftBottomPaths.add(path);
        } else if ("right".equals(section)) {
            rightBottomPaths.add(path);
        }
        this.getChildren().add(path);
    }

    /** Crée un chemin avec une couleur donnée et le mémorise par section */
    private void addPath(String d, String colorHex, boolean addToClip, String section) {
        Path path = createPath(d);
        path.setFill(Color.web(colorHex));
        // Mémoriser dans la bonne liste selon la section
        if ("left".equals(section)) {
            leftTopPaths.add(path);
        } else if ("right".equals(section)) {
            rightTopPaths.add(path);
        }
        if (addToClip) {
            for (javafx.scene.shape.PathElement elem : path.getElements()) {
                masterClip.getElements().add(elem);
            }
        }
        this.getChildren().add(path);
    }

    private Path createPath(String d) {
        Path path = new Path();
        String[] commands = d.split("(?=[MVHZ])");
        double lastX = 0;
        double lastY = 0;

        for (String cmd : commands) {
            if (cmd == null || cmd.isEmpty()) continue;
            char type = cmd.charAt(0);
            String params = cmd.substring(1);

            if (type == 'M') {
                String[] parts = params.split("\\s+");
                lastX = Double.parseDouble(parts[0]);
                lastY = Double.parseDouble(parts[1]);
                PathElement element = new MoveTo(lastX, lastY);
                path.getElements().add(element);
            } else if (type == 'H') {
                lastX = Double.parseDouble(params);
                PathElement element = new LineTo(lastX, lastY);
                path.getElements().add(element);
            } else if (type == 'V') {
                lastY = Double.parseDouble(params);
                PathElement element = new LineTo(lastX, lastY);
                path.getElements().add(element);
            } else if (type == 'Z') {
                PathElement element = new ClosePath();
                path.getElements().add(element);
            }
        }
        
        return path;
    }
}
