package apas.model;

/**
 * Permet de réagir aux changements qui surviennent dans Scene.
 *
 * ATTENTION, Les méthodes de SceneListener peuvent être appelés depuis des thread différents, faites donc très
 * attention à la synchronisation.
 *
 * Note: En javafx, toutes les opérations sur l'interface graphique doivent être faite depuis le JavaFX application
 * thread, pour cela utilisez Platform.runLater(Runnable) ou utilisez les classe Task et Worker dans l'implémentation
 * des méthodes de SceneListener).
 */
public interface SceneListener {

	/**
	 * Appelée lorsqu'un Character est ajouté à la Scene observée.
	 * @param character Character qui vient d'être ajouté à la Scene observée.
	 */
	void onCharacterAdded(Character character);

	/**
	 * Appelée lorsqu'un Character est supprimé de la Scene observée.
	 * @param character Character qui vient d'être supprimé de la Scene observée.
	 */
	void onCharacterRemoved(Character character);

	/**
	 * Appelée lorsqu'un Mouvement commence dans la Scene observée.
	 * @param movement Mouvement qui vient d'être créé dans la Scene observée.
	 */
	void onMovementStart(Movement movement);

	/**
	 * Appelée lorsqu'un Mouvement termine dans la Scene observée.
	 * @param movement Mouvement qui vient d'être terminé dans la Scene observée.
	 */
	void onMovementEnd(Movement movement);

	/**
	 * Appelée lorsque le State d'un Character change dans la Scene observée.
	 * @param character Character dont le State vient de changer.
	 * @param oldState Ancien State de Character.
	 */
	void onStateChanged(Character character, State oldState);
}
