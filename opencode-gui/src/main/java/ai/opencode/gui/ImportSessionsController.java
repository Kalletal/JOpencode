package ai.opencode.gui;

import ai.opencode.core.session.SessionManager;
import ai.opencode.core.storage.SessionImportService;
import ai.opencode.storage.DatabaseManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Contrôleur pour la fenêtre d'importation des sessions.
 */
public class ImportSessionsController {
    @FXML private ListView<File> sessionsListView;
    
    private Stage stage;
    private final SessionImportService importService = new SessionImportService();
    private final SessionManager sessionManager = new SessionManager(DatabaseManager.getInstance());

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    public void initialize() {
        // Permettre la sélection multiple
        sessionsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
        // Charger les sessions détectées
        List<File> foundSessions = importService.scanForSessions();
        ObservableList<File> observableSessions = FXCollections.observableArrayList(foundSessions);
        sessionsListView.setItems(observableSessions);
        
        // Custom cell pour l'affichage et le style de sélection
        sessionsListView.setCellFactory(lv -> new ListCell<File>() {
            @Override
            protected void updateItem(File item, boolean selected) {
                if (item == null) {
                    setText(null);
                    setStyle("");
                    setGraphic(null);
                } else {
                    setText(importService.getSessionTitle(item));
                    
                    // Style personnalisé pour la sélection
                    if (selected) {
                        setStyle("-fx-background-color: #007acc; -fx-text-fill: white; -fx-background-radius: 5;");
                    } else {
                        setStyle("-fx-background-color: #2d2d2d; -fx-text-fill: #cccccc; -fx-background-radius: 5;");
                    }
                }
            }
        });
    }

    @FXML
    public void importSelected() {
        List<File> selectedSessions = sessionsListView.getSelectionModel().getSelectedItems();
        if (selectedSessions.isEmpty()) {
            return;
        }
        
        int importedCount = 0;
        for (File sessionFile : selectedSessions) {
            try {
                importService.importSession(sessionFile, sessionManager);
                importedCount++;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Importation réussie");
        alert.setHeaderText(null);
        alert.setContentText(importedCount + " session(s) ont été importées avec succès.");
        alert.showAndWait();
        
        close();
    }

    @FXML
    public void cancel() {
        close();
    }

    private void close() {
        if (stage != null) {
            stage.close();
        }
    }
}
