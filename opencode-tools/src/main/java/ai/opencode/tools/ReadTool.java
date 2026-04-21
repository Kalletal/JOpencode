package ai.opencode.tools;

import ai.opencode.storage.AppConfig;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Outil permettant de lire le contenu d'un fichier ou de lister le contenu d'un répertoire.
 * Implémente la pagination (offset/limit) pour éviter de saturer le contexte du LLM.
 */
public class ReadTool implements Tool {

    private static final int DEFAULT_LIMIT = 2000;
    private static final int MAX_LINE_LENGTH = 2000;
    private static final long MAX_BYTES = 50 * 1024;

    @Override
    public String getName() {
        return "read";
    }

    @Override
    public String getDescription() {
        return "Lit le contenu d'un fichier ou liste les entrées d'un répertoire. " +
               "Paramètres : filePath (absolu), offset (optionnel, commence à 1), limit (optionnel).";
    }

    @Override
    public ToolResponse execute(ToolRequest request, AppConfig config) throws ToolException {
        String filePathStr = (String) request.parameters().get("filePath");
        if (filePathStr == null) {
            throw new ToolException("Le paramètre 'filePath' est obligatoire.");
        }

        Path path = Paths.get(filePathStr);
        if (!Files.exists(path)) {
            throw new ToolException("Fichier ou répertoire non trouvé : " + filePathStr);
        }

        if (Files.isDirectory(path)) {
            return handleDirectory(path);
        } else {
            return handleFile(path, request);
        }
    }

    private ToolResponse handleDirectory(Path path) throws ToolException {
        try {
            List<String> entries = Files.list(path)
                                       .map(p -> p.getFileName().toString() + (Files.isDirectory(p) ? "/" : ""))
                                       .sorted()
                                       .collect(Collectors.toList());

            StringBuilder sb = new StringBuilder();
            sb.append("<path>").append(path).append("</path>\n");
            sb.append("<type>directory</type>\n");
            sb.append("<entries>\n").append(String.join("\n", entries)).append("\n</entries>");

            return new ToolResponse(sb.toString(), Map.of("entryCount", entries.size()), List.of());
        } catch (IOException e) {
            throw new ToolException("Erreur lors de la lecture du répertoire : " + e.getMessage(), e);
        }
    }

    private ToolResponse handleFile(Path path, ToolRequest request) throws ToolException {
        if (isBinaryFile(path)) {
            throw new ToolException("Impossible de lire un fichier binaire : " + path);
        }

        int offset = (int) request.parameters().getOrDefault("offset", 1);
        int limit = (int) request.parameters().getOrDefault("limit", DEFAULT_LIMIT);

        if (offset < 1) throw new ToolException("L'offset doit être supérieur ou égal à 1.");

        try {
            List<String> lines = new ArrayList<>();
            long totalBytes = 0;
            int lineCount = 0;
            boolean truncated = false;

            try (BufferedReader reader = Files.newBufferedReader(path)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lineCount++;
                    if (lineCount < offset) continue;
                    if (lines.size() >= limit) {
                        truncated = true;
                        break;
                    }

                    // Tronquer la ligne si elle est trop longue
                    String processedLine = line.length() > MAX_LINE_LENGTH 
                                            ? line.substring(0, MAX_LINE_LENGTH) + "... [tronqué]" 
                                            : line;
                    
                    totalBytes += processedLine.getBytes().length + 1;
                    if (totalBytes > MAX_BYTES) {
                        truncated = true;
                        break;
                    }
                    lines.add(lineCount + ": " + processedLine);
                }
            }

            StringBuilder sb = new StringBuilder();
            sb.append("<path>").append(path).append("</path>\n");
            sb.append("<type>file</type>\n");
            sb.append("<content>\n").append(String.join("\n", lines)).append("\n</content>");
            
            if (truncated) {
                sb.append("\n(Le contenu a été tronqué. Utilisez 'offset' pour lire la suite).");
            } else {
                sb.append("\n(Fin du fichier - total : ").append(lineCount).append(" lignes)");
            }

            return new ToolResponse(sb.toString(), Map.of("totalLines", lineCount, "truncated", truncated), List.of());
        } catch (IOException e) {
            throw new ToolException("Erreur lors de la lecture du fichier : " + e.getMessage(), e);
        }
    }

    private boolean isBinaryFile(Path path) {
        try {
            byte[] buffer = new byte[1024];
            try (InputStream is = Files.newInputStream(path)) {
                int read = is.read(buffer);
                if (read == -1) return false;
                int nonPrintable = 0;
                for (int i = 0; i < read; i++) {
                    byte b = buffer[i];
                    if (b == 0) return true;
                    if ((b < 9 || (b > 13 && b < 32)) && b != -1) {
                        nonPrintable++;
                    }
                }
                return (double) nonPrintable / read > 0.3;
            }
        } catch (IOException e) {
            return false;
        }
    }
}
