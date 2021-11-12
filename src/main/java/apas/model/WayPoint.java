package apas.model;

import java.util.Objects;

/**
 * Balise permettant aux Character de se repérer sur une Scene.
 * Immuable.
 * L'egalité de deux WayPoint repose sur leurs noms uniquement.
 */
public class WayPoint implements Position {

	/**
	 * Nom de la balise.
	 */
	private final String name;

	/**
	 * Position en ordonnée de la balise.
	 */
	private final int x;

	/**
	 * Position en abscisse de la balise.
	 */
	private final int y;

	/**
	 * Constructeur.
	 * @param name Nom de la balise.
	 * @param x Position en ordonnée de la balise.
	 * @param y Position en abscisse de la balise.
	 */
	public WayPoint(String name, int x, int y) {
		Objects.requireNonNull(name, "WayPoint name can't be null");
		if(x <= 0 || y <= 0) throw new IllegalArgumentException("WayPoint coords must be greater than 0");

		this.name = name;
		this.x = x;
		this.y = y;
	}

	/**
	 * Accesseur pour le nom de this.
	 * @return Nom de this.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @see Position#getX()
	 */
	public int getX() {
		return x;
	}

	/**
	 * @see Position#getY()
	 */
	public int getY() {
		return y;
	}

	/**
	 * @see Position#isMoving()
	 */
	public boolean isMoving() {
		return false;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		WayPoint wayPoint = (WayPoint) o;
		return name.equals(wayPoint.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}
}