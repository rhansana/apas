package apas.model;

/**
 * Position sur une Scene, sert notamment a la classe Character.
 */
public interface Position {

	/**
	 * Ordonnée.
	 * @return La position en Ordonnée.
	 */
	int getX();

	/**
	 * Abscisse.
	 * @return La position en abscisse.
	 */
	int getY();

	/**
	 * Teste si la Position est fixe ou mobile.
	 * @return true si la Position est mobile, false sinon.
	 */
	boolean isMoving();
}
