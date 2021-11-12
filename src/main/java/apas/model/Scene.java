package apas.model;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Représente la scène sur laquelle évoluent les personnages (Character).
 * Il s'agit de la façade du modèle, toute modification du modèle devra se faire par cette classe (elles sont d'ailleurs
 * impossibles autrement).
 * Les dimensions de la scène sont calculées en fonction de la position des WayPoint les plus éloignés du point (0,0).
 * Cette classe est thread-safe.
 *
 * Quelques notions nécessaires :
 * * La vélocité est le facteur par lequel la vitesse de mouvement par défaut d'un personnage (définie par
 *   Movement.DEFAULT_MOVEMENT_PIXELS_TRAVERSED_PER_SECONDS) sera multiplié lors d'un déplacement.
 */
public class Scene {

	/**
	 * Hauteur de la scène.
	 */
	private final int height;

	/**
	 * Largeur de la scène.
	 */
	private final int width;

	/**
	 * Balises (WaiPoints) disponibles sur cette scène, les clés sont les nom des WayPoint et les valeurs sont les
	 * WayPoint associés.
	 */
	private final Map<String,WayPoint> wayPoints;

	/**
	 * State possibles pour les Character sur cette Scene.
	 */
	private final Map<String,State> availableStates;

	/**
	 * Characters présents sur la Scene, les clés sont les noms des Character.
	 */
	private final Map<String,Character> characters;

	/**
	 * Mouvements en cours sur cette Scene.
	 */
	private final Set<Movement> movements;

	/**
	 * Listeners à qui les changements de cette scène seront notifiés.
	 */
	private final Collection<SceneListener> listeners;

	/**
	 *  Evite de recréer un movement end listener a chaque nouveau mouvement créé
	 */
	private final MovementEndListener sceneMouvementEndListener;

	/**
	 * Constructeur.
	 * @param wayPoints Balises disponibles de la scène.
	 * @param availableStates State possibles pour les Character de la scène.
	 */
	public Scene(Set<WayPoint> wayPoints, Set<State> availableStates) {
		if(wayPoints.isEmpty())
			throw new IllegalArgumentException("There must be at least one wayPoint");

		if(availableStates.isEmpty())
			throw new IllegalArgumentException("There must be at least one available state");

		//calcul de la dimension de la scène
		int h = 0, w = 0;
		for(WayPoint wp : wayPoints){
			if(w < wp.getX()) w = wp.getX();
			if(h < wp.getY()) h = wp.getY();
		}
		this.height = h + 50;
		this.width = w + 50;

		this.wayPoints = wayPoints.stream().collect(Collectors.toUnmodifiableMap(WayPoint::getName, Function.identity()));
		this.availableStates = availableStates.stream().collect(Collectors.toUnmodifiableMap(State::getName, Function.identity()));
		this.characters = new ConcurrentHashMap<>();
		this.movements = Collections.synchronizedSet(new HashSet<>());
		this.listeners = new ConcurrentLinkedDeque<>();

		sceneMouvementEndListener = m -> {
			movements.remove(m);
			listeners.forEach(l -> l.onMovementEnd(m));
		};
	}

	/**
	 * Accesseur pour la hauteur de la scène.
	 * @return La hauteur de la scène.
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Accesseur pour la largeur de la scène.
	 * @return La largeur de la scène.
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Donne tous les WayPoints de this.
	 * @return Une collection de WayPoint Immuable.
	 */
	public Collection<WayPoint> getWayPoints() {
		return wayPoints.values();
	}

	/**
	 * Donne tous les State possibles pour les Character de this.
	 * @return Un ensemble de State Immuable.
	 */
	public Collection<State> getAvailableStates() {
		return availableStates.values();
	}

	/**
	 * Donne tous les Character sur this.
	 * @return Une collection de Character Immuable.
	 */
	public Collection<Character> getAllCharacters() {
		return Collections.unmodifiableCollection(characters.values());
	}

	/**
	 * Récupère un Character de this.
	 * @param name Nom du Character à récupérer.
	 * @return Le Character portant le nom name si il existe sur this, null sinon.
	 */
	public Character getCharacter(String name) {
		return characters.get(name);
	}

	/**
	 * Ajoute un Character avec une vélocité de Character.STANDARD_VELOCITY sur this et le notifie à ses SceneListener.
	 * @param name Nom du Character à créer.
	 * @param wayPoint Le nom du WayPoint où va apparaitre le Character.
	 * @param state Le nom du State initial du Character.
	 * @return Le Character qui vient d'être créé.
	 */
	public Character addCharacter(String name, String wayPoint, String state){
		return addCharacter(name, wayPoint, state, Character.STANDARD_VELOCITY);
	}

	/**
	 * Ajoute un Character sur this et le notifie à ses SceneListener.
	 * @param name Nom du Character à créer.
	 * @param wayPoint Le nom du WayPoint où va apparaitre le Character.
	 * @param state Le nom du State initial du Character.
	 * @param velocity Vélocité du personnage à créer. Doit être strictement supérieur à 0.
	 * @return Le Character qui vient d'être créé.
	 */
	public Character addCharacter(String name, String wayPoint, String state, double velocity) {
		WayPoint wp = wayPoints.get(wayPoint);
		State s = availableStates.get(state);
		if(Objects.isNull(wp))
			throw new IllegalArgumentException("No WayPoint with name : " + wayPoint);

		if(Objects.isNull(s))
			throw new IllegalArgumentException("No state with name : " + state);

		Character c = new Character(name, wp, s, velocity);
		if(!Objects.isNull(characters.put(name,c)))
			throw new IllegalStateException("Character " + name + " is already on the Scene");

		listeners.forEach(sl -> sl.onCharacterAdded(c));
		return c;
	}

	/**
	 * Retire un Character de this et le notifie à ses SceneListener.
	 * @param name Nom du Character à retirer.
	 */
	public void removeCharacter(String name) {
		Character c = characters.remove(name);
		if(Objects.isNull(c))
			throw new IllegalArgumentException("No Character with name : " + name);

		listeners.forEach(sl -> sl.onCharacterRemoved(c));
	}

	/**
	 * Débute le mouvement d'un Character en utilisant la vélocité de ce Character vers un autre WayPoint de this et le
	 * notifie à ses SceneListener.
	 * @param character Nom du Character a faire bouger.
	 * @param targetWayPoint Nom du WayPoint vers lequel faire bouger character.
	 * @return L'objet Mouvement correspondant au déplacement créé.
	 */
	public Movement moveCharacter(String character, String targetWayPoint){
		return moveCharacter(character, targetWayPoint, null);
	}

	/**
	 * Débute le mouvement d'un Character vers un autre WayPoint de this et le notifie à ses SceneListener.
	 * @param character Nom du Character a faire bouger.
	 * @param targetWayPoint Nom du WayPoint vers lequel faire bouger character.
	 * @param velocity Vélocité du personnage lors du mouvement (utilisé à la place de la vélocité par défaut du
	 *                 personnage). Doit être strictement supérieur à 0.
	 * @return L'objet Mouvement correspondant au déplacement créé.
	 */
	public Movement moveCharacter(String character, String targetWayPoint, double velocity){
		return moveCharacter(character, targetWayPoint, velocity, null);
	}

	/**
	 * Débute le mouvement d'un Character en utilisant la vélocité de ce Character vers un autre WayPoint de this et le
	 * notifie à ses SceneListener.
	 * @param character Nom du Character a faire bouger.
	 * @param targetWayPoint Nom du WayPoint vers lequel faire bouger character.
	 * @param listener Listener qui sera appelé a la fin du mouvement.
	 * @return L'objet Mouvement correspondant au déplacement créé.
	 */
	public Movement moveCharacter(String character, String targetWayPoint, MovementEndListener listener) {
		Character c = characters.get(character);
		if(Objects.isNull(c))
			throw new IllegalArgumentException("No Character with name : " + character);
		return moveCharacter(character, targetWayPoint, c.getVelocity(), listener);
	}

	/**
	 * Débute le mouvement d'un Character vers un autre WayPoint de this et le notifie à ses SceneListener.
	 * @param character Nom du Character a faire bouger.
	 * @param targetWayPoint Nom du WayPoint vers lequel faire bouger character.
	 * @param velocity Vélocité du personnage lors du mouvement (utilisé à la place de la vélocité par défaut du
	 *                 personnage). Doit être strictement supérieur à 0.
	 * @param listener Listener qui sera appelé a la fin du mouvement.
	 * @return L'objet Mouvement correspondant au déplacement créé.
	 */
	public Movement moveCharacter(String character, String targetWayPoint, double velocity, MovementEndListener listener) {
		Character c = characters.get(character);
		if(Objects.isNull(c))
			throw new IllegalArgumentException("No Character with name : " + character);
		return moveCharacter(c, targetWayPoint, velocity, listener);
	}

	/**
	 * Sert à ne pas avoir a réécrire le code dans les méthodes qui surchargent cette méthode.
	 * Débute le mouvement d'un Character vers un autre WayPoint de this et le notifie à ses SceneListener.
	 * @param character Character a faire bouger.
	 * @param targetWayPoint Nom du WayPoint vers lequel faire bouger character.
	 * @param velocity Vélocité du personnage lors du mouvement (utilisé à la place de la vélocité par défaut du
	 *                 personnage). Doit être strictement supérieur à 0.
	 * @param listener Listener qui sera appelé a la fin du mouvement.
	 * @return L'objet Mouvement correspondant au déplacement créé.
	 */
	private Movement moveCharacter(Character character, String targetWayPoint, double velocity, MovementEndListener listener){
		WayPoint targetWP = wayPoints.get(targetWayPoint);

		if(Objects.isNull(targetWP))
			throw new IllegalArgumentException("No WayPoint with name : " + targetWayPoint);

		if(character.isMoving())
			throw new IllegalStateException("Character " + character + " is already moving");

		Movement movement;
		if(Objects.isNull(listener))
			movement = new Movement(character, (WayPoint) character.getPosition(), targetWP, velocity, sceneMouvementEndListener);
		else
			movement = new Movement(character, (WayPoint) character.getPosition(), targetWP, velocity, sceneMouvementEndListener, listener);

		movements.add(movement);
		listeners.forEach(sl -> sl.onMovementStart(movement));
		movement.start();

		return movement;
	}


	/**
	 * Met à jour le State d'un Character de this et le notifie à ses SceneListener.
	 * @param character Nom du Character dont on veut modifier le State.
	 * @param newState Nom du State à affecter a character.
	 */
	public void updateState(String character, String newState) {
		Character c = characters.get(character);
		State s = availableStates.get(newState);

		if(Objects.isNull(c))
			throw new IllegalArgumentException("No Character with name : " + character);

		if(Objects.isNull(s))
			throw new IllegalArgumentException("No State with name : " + newState);

		State oldState = c.getState();
		c.setState(s);

		listeners.forEach(sl -> sl.onStateChanged(c, oldState));
	}

	/**
	 * Ajoute un SceneListener à qui sera notifié les changements dans this.
	 * Ne vérifie pas si il y a des doublons.
	 * @param listener SceneListener à ajouter.
	 */
	public void addSceneListener(SceneListener listener) {
		Objects.requireNonNull(listener, "listener can't be null");
		listeners.add(listener);
	}
}
