package apas.view;

import apas.controller.Controller;
import apas.model.Character;
import apas.model.Movement;
import apas.model.WayPoint;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.*;

public class ProgramAnimation {

    /**
     * La vue qui sera afficher pour l'utilisateur et où les animations se feront.
     */
    private final Pane monPane;
    /**
     * Map pour savoir quelle representation graphique est associée à chaque character.
     */
    private final Map<Character,StackPane> lesNodes;
    /**
     * Liste des mouvements en cours sur la vue.
     */
    private final Set<Movement> lesMoves;
    /**
     * Timer associé à la vue qui effectuera les tâches qui lui seront affichés.
     * Ici, le rafraichissement de la position des characters au niveau de la vue.
     */
    private final Timer updateTimer;

    /**
     * Constructeur.
     * @param controller Controlleur de la vue.
     */
    public ProgramAnimation(Controller controller){
        this.monPane = new Pane();
        this.lesNodes = new HashMap<>();
        this.lesMoves = new HashSet<>();

        this.monPane.setMinHeight(controller.getHeight());
        this.monPane.setMinWidth(controller.getWidth());

        for(WayPoint w : controller.getLesWaypoints()) {
            Circle c = new Circle(10, Color.GREEN);
            Text t = new Text();
            t.setText(w.getName());
            StackPane stack = new StackPane();
            stack.getChildren().addAll(c, t);
            stack.setLayoutX(w.getX());
            stack.setLayoutY(w.getY());

            monPane.getChildren().add(stack);
        }

        for(Character c : controller.getLesChar())
                monPane.getChildren().add(c.graphic());

        updateTimer = new Timer();
    }

    /**
     * Creation du mouvement via TimerTask pour raffraichir la position de l objet associé au mouvement
     * Le raffraichissement se fera a un interval regulier sur le thread principal
     * @param move Mouvement qui va etre representer.
     */
    public void startAnim(Movement move) {
        lesMoves.add(move);
        TimerTask timerTask = new TimerTask(){

            @Override
            public void run() {
                Platform.runLater(() -> {
                    for(Movement movement : lesMoves) {
                        if(movement.isMoving()) {
                            StackPane stackPane = lesNodes.get(movement.getCharacter());
                            stackPane.setLayoutX(movement.getX());
                            stackPane.setLayoutY(movement.getY());
                        }
                    }
                });
            }
        };
        updateTimer.scheduleAtFixedRate(timerTask,Movement.MOVEMENT_REFRESH_RATE, Movement.MOVEMENT_REFRESH_RATE);
    }

    /**
     * A la fin du mouvement
     * on retire le mouvement de la liste des mouvements en cours sur la vue
     * @param move Mouvement en cours.
     */
    public void endAnim(Movement move){
        lesMoves.remove(move);
    }

    /**
     * Supprime le Character de la vue
     * @param c Character à supprimer de la vue.
     */
    public void supprChar(Character c){
        StackPane stack = lesNodes.get(c);
        if(!c.getPosition().isMoving()){
            monPane.getChildren().remove(stack);
        }
    }

    /**
     * Ajoute la forme associer au Character à la vue pour l'afficher
     * @param c Character à afficher.
     */
    public void addChar(Character c){
        StackPane graph = (StackPane) c.graphic();
        graph.setLayoutX(c.getPosition().getX());
        graph.setLayoutY(c.getPosition().getY());

        lesNodes.put(c,graph);

        monPane.getChildren().add(graph);
    }

    /**
     * Supprime l'ancienne représentation graphique et la change pour la nouvelle.
     * @param c Character dont on veut changer la représentation graphique.
     */
    public void changeState(Character c){
        StackPane stack = lesNodes.get(c);
        if(!c.getPosition().isMoving()){
            monPane.getChildren().remove(stack);
            addChar(c);
        }
    }

    /**
     * Affiche la stage en cours
     * @param stage Stage à afficher.
     */
    public void show(Stage stage){
        stage.setTitle("Animations de Séphamores");
        stage.setScene(new Scene(monPane,monPane.getMinWidth(), monPane.getMinHeight()));
        stage.show();
    }
}
