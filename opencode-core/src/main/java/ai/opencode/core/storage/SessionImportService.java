package ai.opencode.core.storage;

import ai.opencode.core.session.SessionManager;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import com.google.gson.*;

/**
 * Service pour importer des sessions depuis la version originale d'Opencode.
 */
public class SessionImportService {
    private static final String DEFAULT_SESSIONS_PATH = System.getProperty("user.home") + "/.opencode/sessions";
    private final Gson gson = new Gson();

    /**
     * Scanne le répertoire par défaut pour trouver les fichiers de session.
     * @return Une liste de fichiers de session trouvés.
     */
    public List<File> scanForSessions() {
        File sessionDir = new File(DEFAULT_SESSIONS_PATH);
        if (!sessionDir.exists() || !sessionDir.isDirectory()) {
            return Collections.emptyList();
        }
        
        File[] files = sessionDir.listFiles((dir, name) -> name.endsWith(".json"));
        return files == null ? Collections.emptyList() : Arrays.asList(files);
    }

    /**
     * Extrait le titre d'une session depuis son fichier JSON.
     * @param file Le fichier JSON de la session.
     * @return Le titre de la session ou un titre par défaut.
     */
    public String getSessionTitle(File file) {
        try (Reader reader = new FileReader(file)) {
            JsonObject json = gson.fromJson(reader, JsonObject.class);
            return json.has("title") ? json.get("title").getAsString() : "Session importée";
        } catch (Exception e) {
            return file.getName();
        }
    }

    /**
     * Importe une session à partir d'un fichier JSON dans la base de données.
     * @param file Le fichier JSON de la session.
     * @param sessionManager Le gestionnaire de sessions pour la persistance.
     * @throws IOException Si la lecture du fichier échoue.
     */
    public void importSession(File file, SessionManager sessionManager) throws IOException {
        try (Reader reader = new FileReader(file)) {
            JsonObject json = gson.fromJson(reader, JsonObject.class);
            
            String title = json.has("title") ? json.get("title").getAsString() : "Session importée";
            
            // Créer la session dans la DB
            String sessionId = sessionManager.createSession(title);
            
            if (sessionId != null && json.has("messages") && json.get("messages").isJsonArray()) {
                JsonArray messagesArray = json.getAsJsonArray("messages");
                for (JsonElement element : messagesArray) {
                    JsonObject msgObj = element.getAsJsonObject();
                    String role = msgObj.has("role") ? msgObj.get("role").getAsString() : "user";
                    String content = msgObj.has("content") ? msgObj.get("content").getAsString() : "";
                    
                    sessionManager.saveMessage(sessionId, role, content);
                }
            }
        }
    }
}
