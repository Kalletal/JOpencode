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

public class WelcomeTitle extends Pane {
    private final Path masterClip = new Path();
    private final Pane rippleContainer = new Pane();
    private final Random random = new Random();
    
    // Chemins de la partie basse (couleur #3B3634) - stockés explicitement
    private final List<Path> bottomPaths = new ArrayList<>();
    
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
        
        // --- "OPEN" part ---
        addBottomPath("M18 30H6V18H18V30Z");
        addBottomPath("M48 30H36V18H48V30Z");
        addBottomPath("M84 24V30H66V24H84Z");
        addBottomPath("M108 36H96V18H108V36Z");
        
        addPath("M18 12H6V30H18V12ZM24 36H0V6H24V36Z", "#656363", true);
        addPath("M36 30H48V12H36V30ZM54 36H36V42H30V6H54V36Z", "#656363", true);
        addPath("M84 24H66V30H84V36H60V6H84V24ZM66 18H78V12H66V18Z", "#656363", true);
        addPath("M108 12H96V36H90V6H108V12ZM114 36H108V12H114V36Z", "#656363", true);
        
        // --- "code" part ---
        addBottomPath("M144 30H126V18H144V30Z");
        addBottomPath("M168 30H156V18H168V30Z");
        addBottomPath("M198 30H186V18H198V30Z");
        addBottomPath("M234 24V30H216V24H234Z");
        
        addPath("M144 12H126V30H144V36H120V6H144V12Z", "#FFFFFF", true);
        addPath("M168 12H156V30H168V12ZM174 36H150V6H174V36Z", "#FFFFFF", true);
        addPath("M198 12H186V30H198V12ZM204 36H180V6H198V0H204V36Z", "#FFFFFF", true);
        addPath("M216 12V18H228V12H216ZM234 24H216V30H234V36H210V6H234V24Z", "#FFFFFF", true);
        
        this.getChildren().add(rippleContainer);
    }

      /** Applique la couleur appropriée au thème courant */
    public void updateForTheme(boolean isDark) {
        Color bottomColor = isDark ? Color.web("#3B3634") : Color.web("#645C5C");
        for (Path path : bottomPaths) {
            path.setFill(bottomColor);
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

    /** Crée un chemin de la partie basse (#3B3634) et le stocke explicitement */
    private void addBottomPath(String d) {
        Path path = createPath(d);
        path.setFill(Color.web("#3B3634"));
        bottomPaths.add(path);
        this.getChildren().add(path);
    }

    /** Crée un chemin avec une couleur donnée (pas stocké séparément) */
    private void addPath(String d, String colorHex, boolean addToClip) {
        Path path = createPath(d);
        path.setFill(Color.web(colorHex));
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
