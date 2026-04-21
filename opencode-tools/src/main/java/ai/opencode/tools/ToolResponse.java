package ai.opencode.tools;

import java.util.Map;
import java.util.List;

/**
 * Représente la réponse renvoyée par un outil après son exécution.
 * Cette structure permet de transmettre non seulement le texte final,
 * mais aussi des métadonnées et des fichiers joints (multimodalité).
 */
public record ToolResponse(
    String output,
    Map<String, Object> metadata,
    List<Attachment> attachments
) {}

/**
 * Représente un fichier joint renvoyé par un outil (ex: image, PDF).
 */
record Attachment(
    String fileName,
    String mimeType,
    byte[] data
) {}
