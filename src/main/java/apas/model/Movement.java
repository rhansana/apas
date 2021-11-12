package apas.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Position mobile, soit un déplacement entre deux WayPoints.
 *
 * Lors de la construction d'un mouvement, celui ci devient automatiquement la nouvelle Position de character, et à la
 * fin de ce mouvement, le WayPoint d'arrivée devient automatiquement la Position du Character concerné.
 *
 * L'égalité de deux mouvements repose sur les WayPoints de départ et d'arrivée, sur le Character ainsi que sur la durée
 * totale du mouvement uniquement.
 *
 * Immuable en dehors du package apas.model car les méthodes de mise à jour sont déclarées package private.
 */
public class Movement implements Position {

	/**
	 * Timer utilisé pour mettre à jour les mouvements a interval de temps régulier.
	 */
	private static final Timer movementUpdateTimer = new Timer();

	/**
	 * Temps en millisecondes entre chaque mise à jour des mouvements.
	 */
	public static final long MOVEMENT_REFRESH_RATE = 16; // 60 fois par secondes

	/**
	 * Nombre par défaut de pixels traversés par seconde pour un Character en mouvement.
	 */
	public static final int DEFAULT_MOVEMENT_PIXELS_TRAVERSED_PER_SECONDS = 100;

	/**
	 * Point de départ du mouvement.
	 */
	private final WayPoint start;

	/**
	 * Point d'arivée du mouvement.
	 */
	private final WayPoint end;

	/**
	 * Character concerné par le movement.
	 */
	private final Character character;

	/**
	 * Durée totale pour aller de start à end.
	 */
	private final Duration delay;

	/**
	 * Temps restant avant la fin du mouvement.
	 */
	private Duration remainingTime;

	/**
	 * Date de la dernière mise à jour de remainingTime.
	 */
	private LocalDateTime lastRemainingTimeUpdate;

	/**
	 * Listeners qui seront avertis lors de la fin du mouvement this.
	 */
	private final MovementEndListener[] listeners;

	/**
	 * progression du déplacement entre 0 et 1, plus character est proche de 0, plus on est proche de start et
	 * inversement
	 */
	private double progression;

	/**
	 * Position courrante du character lors de ce mouvement.
	 */
	private int x, y;

	/**
	 * Constructeur permettant d'utiliser un vélocité différente de celle du personnage concerné.
	 * This devient automatiquement la nouvelle position de character.
	 * @param character Character qui va se déplacer.
	 * @param start Point de départ du mouvement.
	 * @param end Point d'arivée du mouvement.
	 * @param velocity Vélocité du personnage lors du mouvement (utilisé à la place de la vélocité par défaut du
	 *                 personnage). Doit être strictement supérieur à 0.
	 * @param listeners Listeners qui seront appelées à la fin de ce mouvement.
	 */
	Movement(Character character, WayPoint start, WayPoint end, double velocity, MovementEndListener... listeners) {
		if(velocity <= 0.0)
			throw new IllegalArgumentException("velocity must be strictly greater than 0");

		this.character = character;
		this.start = start;
		this.end = end;
		this.delay = Duration.ofMillis(
				Math.round(
						( Math.sqrt( Math.pow(end.getX() - start.getX(),2) + Math.pow(end.getY() - start.getY(),2) ) * 1000 )
								/ (DEFAULT_MOVEMENT_PIXELS_TRAVERSED_PER_SECONDS * velocity)
				)
		);
		this.remainingTime = delay;
		character.setPosition(this);
		this.listeners = listeners;

		this.progression = 0.0;
		this.x = start.getX();
		this.y = start.getY();
	}

	/**
	 * Accesseur pour le point de départ.
	 * @return Le point de départ.
	 */
	public WayPoint getStart() {
		return start;
	}

	/**
	 * Accesseur pour le point d'arrivée.
	 * @return Le point d'arrivée.
	 */
	public WayPoint getEnd() {
		return end;
	}

	/**
	 * Accesseur pour le Character.
	 * @return Le Character de this.
	 */
	public Character getCharacter(){
		return character;
	}

	/**
	 * Accesseur pour la durée totale du déplacement.
	 * @return La durée totale du déplacement.
	 */
	public Duration getDelay() {
		return delay;
	}

	/**
	 * Accesseur pour la durée restante du déplacement.
	 * @return La durée restante du déplacement.
	 */
	public Duration getRemainingTime() {
		return remainingTime;
	}

	/**
	 * Donne la progression du déplacement entre 0 et 1, plus character est proche de 0, plus on est proche de start et
	 * inversement.
	 * @return Progression entre 0 et 1 du déplacement.
	 */
	public double progression() {
		return progression;
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
	 * @return false si Character est arrivé à end, true sinon.
	 */
	public boolean isMoving() {
		return !remainingTime.isZero();
	}


	/**
	 * Lance le mouvement en programmant une MouvementUpdateTask qui va mettre ce mouvement automatiquement à jour à
	 * interval de temps régulier.
	 */
	void start(){
		lastRemainingTimeUpdate = LocalDateTime.now();
		movementUpdateTimer.scheduleAtFixedRate(new MovementUpdateTask(this), MOVEMENT_REFRESH_RATE, MOVEMENT_REFRESH_RATE);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Movement movement = (Movement) o;
		return start.equals(movement.start) &&
				end.equals(movement.end) &&
				character.equals(movement.character) &&
				delay.equals(movement.delay);
	}

	@Override
	public int hashCode() {
		return Objects.hash(start, end, character, delay);
	}



	/*
	==================================================================================================================
	================================================= CLASSES INTERNES ===============================================
	==================================================================================================================
	*/

	/**
	 * Tâche permettant de mettre à jour un Mouvement de façon régulière dans un Timer.
	 * Quand le mouvement se termine, la position du character de sera mis à jour avec WayPoint end, les
	 * MovementEndListeners seront notifiés de la fin du mouvement, et la tâche sera annulée.
	 */
	private static class MovementUpdateTask extends TimerTask{
		private final Movement mvt;

		public MovementUpdateTask(Movement mvt){
			super();
			this.mvt = mvt;
		}

		@Override
		public void run() {
			LocalDateTime now = LocalDateTime.now();
			mvt.remainingTime = mvt.remainingTime.minus(Duration.between(mvt.lastRemainingTimeUpdate, now));
			mvt.lastRemainingTimeUpdate = now;
			if(mvt.remainingTime.compareTo(Duration.ZERO) <= 0) {
				mvt.progression = 1.0;
				mvt.x = mvt.end.getX();
				mvt.y = mvt.end.getY();
				mvt.character.setPosition(mvt.end);
				mvt.remainingTime = Duration.ZERO;
				cancel();
				for (MovementEndListener l : mvt.listeners)
					l.onMovementEnd(mvt); //TODO peut poser problème sur la mise a jour si le listener est lourd a executer voir ExecutorService
			}else {
				mvt.progression = Math.abs((double) mvt.remainingTime.toMillis() / mvt.delay.toMillis() - 1);
				mvt.x = (int) Math.round((mvt.end.getX() - mvt.start.getX()) * mvt.progression + mvt.start.getX());
				mvt.y = (int) Math.round((mvt.end.getY() - mvt.start.getY()) * mvt.progression + mvt.start.getY());
			}
		}
	}
}
