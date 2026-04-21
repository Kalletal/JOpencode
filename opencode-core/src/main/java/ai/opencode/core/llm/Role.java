package ai.opencode.core.llm;

/**
 * Rôles possibles pour un message :
 * SYSTEM : Instructions de base.
 * USER : Message de l'utilisateur.
 * ASSISTANT : Réponse de l'agent.
 * TOOL : Résultat de l'exécution d'un outil.
 */
public enum Role {
    SYSTEM, USER, ASSISTANT, TOOL
}
