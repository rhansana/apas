package apas.model;

import javafx.scene.Node;

import java.util.Objects;

/**
 * Sert à représenter des personnages, ou toute autre entité pouvant se déplacer d'une balise (WayPoint) à une autre
 * dans une scène (représenté par la classe Scene).
 *
 * Immuable en dehors du package apas.model car les méthodes de mise à jour sont déclarées package private.
 *
 * L'égalité de deux personnages repose uniquement sur leurs noms.
 */
public class Character {

	/**
	 * Vélocité standard d'un personage.
	 */
	public static final double STANDARD_VELOCITY = 1.0;


	/**
	 * Nom du personnage.
	 */
	private final String name;

	/**
	 * État du personage.
	 */
	private State state;

	/**
	 * Position du personnage sur une scène.
	 */
	private Position position;

	/**
	 * Vélocité par défaut du personnage, c.à.d. le facteur par lequel la vitesse de mouvement par défaut d'un
	 * personnage sera multiplié.
	 * @see Movement#DEFAULT_MOVEMENT_PIXELS_TRAVERSED_PER_SECONDS
	 */
	private final double velocity;

	/**
	 * Constructeur.
	 * @param name Nom du personnage.
	 * @param position Position initiale du personnage.
	 * @param state État initial du personnage.
	 * @param velocity Vélocité par défaut du personnage, c.à.d. le facteur par lequel la vitesse de mouvement par
	 *                 défaut d'un personnage (définie par Movement.DEFAULT_MOVEMENT_PIXELS_TRAVERSED_PER_SECONDS)
	 *                 sera multiplié. Doit être strictement supérieur à 0.
	 */
	Character(String name, WayPoint position, State state, double velocity) {
		Objects.requireNonNull(name, "Character name can't be null");
		Objects.requireNonNull(position, "Character position can't be null");
		Objects.requireNonNull(state, "Character state can't be null");
		if(velocity <= 0.0)
			throw new IllegalArgumentException("velocity must be strictly greater than 0");

		this.name = name;
		this.position = position;
		this.state = state;
		this.velocity = velocity;
	}

	/**
	 * Accesseur pour le nom de this.
	 * @return Le nom de this.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Accesseur pour l'état de this.
	 * @return L'état de this.
	 */
	public State getState() {
		return state;
	}

	/**
	 * Mutateur pour la l'état de this.
	 * @param state Nouvel état attribué à this.
	 */
	void setState(State state) {
		this.state = state;
	}

	/**
	 * Accesseur pour la position de this.
	 * @return La position de this.
	 */
	public Position getPosition() {
		return position;
	}

	/**
	 * Mutateur pour la position de this.
	 * @param position Nouvelle position attribuée à this.
	 */
	void setPosition(Position position) {
		this.position = position;
	}

	/**
	 * Teste si this est en mouvement sur la scène.
	 * @return true si this est en mouvement, false sinon.
	 */
	public boolean isMoving() {
		return position.isMoving();
	}

	/**
	 * Donne la vélocité de this.
	 * @return La vélocité de this.
	 */
	public double getVelocity() {
		return velocity;
	}

	/**
	 * Crée la représentation graphique javafx de this.
	 * @return Un nouveau Node javafx correspondant à la représentation graphique de this.
	 */
	public Node graphic() {
		return state.buildGraphic(this);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Character character = (Character) o;
		return name.equals(character.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}
}
