package apas;

import java.util.*;

import apas.controller.Controller;
import apas.model.Scene;
import javafx.stage.Stage;
import apas.model.State;
import apas.model.WayPoint;

/**
 * Point d'entrée pour un programme APAS.
 * 1. Initialise l'interface graphique.
 * 2. Appelle init(Map&lt;String,WayPoint&gt;) qui doit être redéfinie pour initialiser les balises entre lesquels vont se
 *    déplacer les personnages, ainsi que les états possibles de ces personnages.
 * 2. Appelle la méthode mainThread(Scene) qui doit être redéfinie et qui doit créer et animer les personnages.
 *    Elle sera exécutée dans un thread different de celui de l'interface graphique.
 *
 * Quand l'appel à mainThead(Scene) est terminé, l'application n'est pas fermée, vous devez fermer la fenêtre pour cela.
 *
 * Notez que mainThread(Scene) et init(Map&lt;String, WayPoint&gt;,Map&lt;String,State&gt;) sont abstraite et doivent être redéfinie.
 *
 * Lors de la fermeture de la fenêtre contenant l'annimation, par défaut, tous les thread de l'application sont détruits
 * "brutalement". Si vous voulez libérer des ressources proprement lors de la fermeture de la fenêtre redéfinissez la
 * méthode stop() qui sera appelé lors de la fermeture de la fenêtre ou lors de l'appel à Platform.exit().
 *
 *
 * Pour stopper totalement l'application, utiliser Platform.exit() est préférable à System.exit(int) car sinon la méthode
 * stop ne sera pas appelée.
 *
 * Les sous classes de Application doivent être déclarés public et avoir un constructeur sans arguments.
 */
public abstract class Application extends javafx.application.Application {

    @Override
    public final void start(Stage primaryStage) {
        Set<WayPoint> wayPoints = new HashSet<>();
        Set<State> availableStates = new HashSet<>();
        init(wayPoints, availableStates);
        Scene scene = new Scene(wayPoints, availableStates);
        new Controller(primaryStage,scene);
        new Thread(() -> mainThread(scene)).start();
    }

    /**
     * Appelée lors de la fermeture de l'application (fermeture de la fenêtre de l'animation ou lors de l'appel à
     * Platform.exit()).
     *
     * Cette méthode appelle System.exit() pour détruire tout les threads qui sont encore en marche, redéfinissez cette
     * méthode pour une fermeture plus propre.
     * Redéfinissez et laissez cette méthode vide si vous voulez que les threads qui n'on pas terminé puissent continuer
     * tourner en arrière plan.
     */
    @Override
    public void stop() {
        System.exit(0);
    }

    /**
     * Doit initialiser les balises entre lesquelles vont se déplacer les personnages (Character).
     * @param wayPoints Ensemble à remplir avec les balises désirées.
     * @param availableStates Ensemble des états disponibles pour les personnages
     */
    public abstract void init(Set<WayPoint> wayPoints, Set<State> availableStates);

    /**
     * Doit contenir les instructions du thread principal.
     * C'est ici que vous devez écrire votre programme qui va créer et animer les personnages (classe Character) via les
     * services de la classe Scene.
     *
     * Cette méthode sera exécutée dans un Thread different que celui de l'interface graphique.
     * @param scene Scene sur laquelle vont évoluer  les personnages (type Character). Lorsqu'une méthode est appelée
     *              sur cet objet, l'interface se met automatiquement à jour.
     */
    public abstract void mainThread(Scene scene);
}
