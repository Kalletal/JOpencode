package ai.opencode.tools;

/**
 * Exception spécifique lancée lors de l'échec d'exécution d'un outil.
 * Elle permet de transmettre un message d'erreur clair au LLM pour qu'il puisse
 * tenter de corriger sa requête.
 */
public class ToolException extends Exception {
    public ToolException(String message) {
        super(message);
    }

    public ToolException(String message, Throwable cause) {
        super(message, cause);
    }
}
