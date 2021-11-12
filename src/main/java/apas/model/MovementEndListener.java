package apas.model;

/**
 * Permet de réagir à la fin d'un Mouvement.
 */
@FunctionalInterface
public interface MovementEndListener {

    /**
     * Méthode appelée lorsque le Mouvement vient de se terminer.
     * @param movement Mouvement qui vient de se terminer.
     */
    void onMovementEnd(Movement movement);
}
