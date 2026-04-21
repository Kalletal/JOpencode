package ai.opencode.tools;

import ai.opencode.storage.AppConfig;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Outil permettant d'exécuter des commandes système.
 * Gère les timeouts et capture les flux de sortie (stdout/stderr).
 */
public class BashTool implements Tool {

    private static final long DEFAULT_TIMEOUT_MS = 120_000;

    @Override
    public String getName() {
        return "bash";
    }

    @Override
    public String getDescription() {
        return "Exécute une commande dans le terminal. " +
               "Paramètres : command (la commande), timeout (optionnel, ms), workdir (optionnel).";
    }

    @Override
    public ToolResponse execute(ToolRequest request, AppConfig config) throws ToolException {
        String command = (String) request.parameters().get("command");
        if (command == null) {
            throw new ToolException("Le paramètre 'command' est obligatoire.");
        }

        long timeout = ((Number) request.parameters().getOrDefault("timeout", DEFAULT_TIMEOUT_MS)).longValue();
        String workdir = (String) request.parameters().get("workdir");

        try {
            return runCommand(command, timeout, workdir);
        } catch (Exception e) {
            throw new ToolException("Échec de l'exécution de la commande : " + e.getMessage(), e);
        }
    }

    private ToolResponse runCommand(String command, long timeout, String workdir) throws IOException, InterruptedException, ExecutionException, TimeoutException {
        // Utilisation de ProcessBuilder pour lancer la commande
        // Sur Windows, on utilise 'cmd /c', sur Unix 'sh -c'
        ProcessBuilder pb = new ProcessBuilder();
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            pb.command("cmd.exe", "/c", command);
        } else {
            pb.command("sh", "-c", command);
        }

        if (workdir != null) {
            pb.directory(new File(workdir));
        }

        pb.redirectErrorStream(true); // Fusionne stderr dans stdout

        Process process = pb.start();

        // Lecture asynchrone du flux de sortie pour éviter le blocage du buffer
        CompletableFuture<String> outputFuture = CompletableFuture.supplyAsync(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                return sb.toString();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        try {
            // On attend la fin du processus ou le timeout
            boolean finished = process.waitFor(timeout, TimeUnit.MILLISECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new TimeoutException("La commande a dépassé le délai imparti de " + timeout + " ms");
            }

            String output = outputFuture.get(5, TimeUnit.SECONDS);
            int exitCode = process.exitValue();

            return new ToolResponse(output, Map.of("exitCode", exitCode), List.of());
        } catch (TimeoutException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Erreur lors de la récupération de la sortie : " + e.getMessage());
        }
    }
}
