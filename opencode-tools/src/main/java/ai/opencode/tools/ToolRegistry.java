package ai.opencode.tools;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registre central des outils disponibles pour l'application.
 * Permet d'ajouter, de supprimer et de récupérer des outils par leur nom.
 */
public class ToolRegistry {
    private final Map<String, Tool> tools = new ConcurrentHashMap<>();

    public ToolRegistry() {
        // Enregistrement des outils natifs par défaut
        registerTool(new ReadTool());
        registerTool(new BashTool());
    }

    /**
     * Ajoute un outil au registre.
     * @param tool L'outil à enregistrer.
     */
    public void registerTool(Tool tool) {
        tools.put(tool.getName(), tool);
    }

    /**
     * Récupère un outil par son nom.
     * @param name Le nom de l'outil.
     * @return L'outil correspondant ou null s'il n'existe pas.
     */
    public Tool getTool(String name) {
        return tools.get(name);
    }

    /**
     * Retourne la liste de tous les outils enregistrés.
     */
    public List<Tool> getAllTools() {
        return new ArrayList<>(tools.values());
    }
}
