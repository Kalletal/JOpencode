package ai.opencode.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.nio.file.Paths;
import java.io.File;

/**
 * Gère la connexion et l'initialisation de la base de données SQLite.
 * Cette classe s'assure que les tables nécessaires (sessions, logs, todos) 
 * sont créées au démarrage de l'application.
 */
public class DatabaseManager {
    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());
    private static final String DB_FILE_NAME = "opencode-java.db";
    private static DatabaseManager instance;
    private Connection connection;

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private DatabaseManager() {
        initDatabase();
    }

    private void initDatabase() {
        try {
            String userHome = System.getProperty("user.home");
            String dbPath = Paths.get(userHome, ".opencode", DB_FILE_NAME).toAbsolutePath().toString();
            String url = "jdbc:sqlite:" + dbPath;

            if (this.connection != null && !this.connection.isClosed()) {
                return;
            }

            this.connection = DriverManager.getConnection(url);
            LOGGER.info("Connexion établie à la base de données : " + dbPath);
            createTables();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'initialisation de la base de données", e);
        }
    }

    private void createTables() {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS sessions (id TEXT PRIMARY KEY, title TEXT, created_at DATETIME DEFAULT CURRENT_TIMESTAMP)");
            stmt.execute("CREATE TABLE IF NOT EXISTS messages (id TEXT PRIMARY KEY, session_id TEXT, role TEXT, content TEXT, timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY(session_id) REFERENCES sessions(id))");
            stmt.execute("CREATE TABLE IF NOT EXISTS todos (id TEXT PRIMARY KEY, content TEXT, status TEXT, priority TEXT, updated_at DATETIME DEFAULT CURRENT_TIMESTAMP)");
            LOGGER.info("Tables de la base de données initialisées.");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la création des tables", e);
        }
    }

    public synchronized Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                initDatabase();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la vérification de la connexion", e);
        }
        return connection;
    }

    public synchronized void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Erreur lors de la fermeture de la connexion DB", e);
        }
    }
}
