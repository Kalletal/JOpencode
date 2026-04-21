package ai.opencode.core.session;

import ai.opencode.storage.DatabaseManager;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gère la persistance et la récupération des sessions de conversation.
 * Cette classe fait le pont entre l'orchestrateur d'agents et la base de données SQLite.
 */
public class SessionManager {
    private static final Logger LOGGER = Logger.getLogger(SessionManager.class.getName());
    private final DatabaseManager dbManager;

    public SessionManager(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Crée une nouvelle session dans la base de données.
     * 
     * @param title Le titre de la session.
     * @return L'identifiant unique de la session créée.
     */
    public String createSession(String title) {
        String sessionId = UUID.randomUUID().toString();
        String sql = "INSERT INTO sessions(id, title) VALUES(?, ?)";
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, sessionId);
            pstmt.setString(2, title);
            pstmt.executeUpdate();
            return sessionId;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la création de la session", e);
            return null;
        }
    }

    /**
     * Enregistre un message dans la session.
     * 
     * @param sessionId L'ID de la session.
     * @param role Le rôle du message (USER, ASSISTANT, TOOL).
     * @param content Le contenu du message.
     */
    public void saveMessage(String sessionId, String role, String content) {
        String sql = "INSERT INTO messages(id, session_id, role, content) VALUES(?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, UUID.randomUUID().toString());
            pstmt.setString(2, sessionId);
            pstmt.setString(3, role);
            pstmt.setString(4, content);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la sauvegarde du message", e);
        }
    }

    /**
     * Récupère l'historique complet d'une session.
     * 
     * @param sessionId L'ID de la session.
     * @return Une liste de messages ordonnée par date.
     */
    public List<MessageRecord> getSessionHistory(String sessionId) {
        List<MessageRecord> history = new ArrayList<>();
        String sql = "SELECT role, content FROM messages WHERE session_id = ? ORDER BY timestamp ASC";
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, sessionId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                history.add(new MessageRecord(rs.getString("role"), rs.getString("content")));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération de l'historique", e);
        }
        return history;
    }

    /**
     * Record simple pour transporter les données de session depuis la DB.
     */
    public record SessionRecord(String id, String title) {}

    /**
     * Record simple pour transporter les données de message depuis la DB.
     */
    public record MessageRecord(String role, String content) {}

    /**
     * Récupère toutes les sessions enregistrées.
     * 
     * @return Une liste de sessions ordonnée par date de création décroissante.
     */
    public List<SessionRecord> getAllSessions() {
        List<SessionRecord> sessions = new ArrayList<>();
        String sql = "SELECT id, title FROM sessions ORDER BY created_at DESC";
        
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                sessions.add(new SessionRecord(rs.getString("id"), rs.getString("title")));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération des sessions", e);
        }
        return sessions;
    }

    /**
     * Met à jour le titre d'une session.
     * 
     * @param sessionId L'ID de la session.
     * @param newTitle Le nouveau titre.
     */
    public void updateSessionTitle(String sessionId, String newTitle) {
        String sql = "UPDATE sessions SET title = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, newTitle);
            pstmt.setString(2, sessionId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la mise à jour du titre de session", e);
        }
    }

    /**
     * Supprime une session et tous ses messages associés.
     * 
     * @param sessionId L'ID de la session à supprimer.
     */
    public void deleteSession(String sessionId) {
        String sqlSession = "DELETE FROM sessions WHERE id = ?";
        String sqlMessages = "DELETE FROM messages WHERE session_id = ?";
        
        try {
            dbManager.getConnection().setAutoCommit(false);
            try (PreparedStatement pstmtS = dbManager.getConnection().prepareStatement(sqlSession);
                 PreparedStatement pstmtM = dbManager.getConnection().prepareStatement(sqlMessages)) {
                
                pstmtS.setString(1, sessionId);
                pstmtS.executeUpdate();
                
                pstmtM.setString(1, sessionId);
                pstmtM.executeUpdate();
                
                dbManager.getConnection().commit();
            } catch (SQLException e) {
                dbManager.getConnection().rollback();
                throw e;
            } finally {
                dbManager.getConnection().setAutoCommit(true);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la suppression de la session " + sessionId, e);
        }
    }
}
