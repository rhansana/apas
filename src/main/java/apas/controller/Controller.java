package apas.controller;

import apas.model.*;
import apas.model.Character;
import apas.view.ProgramAnimation;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.util.Collection;

public class Controller implements SceneListener {

	private final Scene model;
	private final ProgramAnimation programAnimation;

	public Controller(Stage primaryStage, Scene model){
		this.model = model;
		this.model.addSceneListener(this);
		this.programAnimation = new ProgramAnimation(this);
		this.programAnimation.show(primaryStage);
	}


	/**
	 * @see SceneListener#onCharacterAdded(Character)
	 */
	public void onCharacterAdded(Character character) {
		Platform.runLater(() -> {
			programAnimation.addChar(character);
		});
	}


	/**
	 * @see SceneListener#onCharacterRemoved(Character)
	 */
	public void onCharacterRemoved(Character character) {
		Platform.runLater(() -> {
			programAnimation.supprChar(character);
	});
	}


	/**
	 * @see SceneListener#onMovementStart(Movement)
	 */
	public void onMovementStart(Movement movement) {
		Platform.runLater(() -> {
			programAnimation.startAnim(movement);
		});
	}


	/**
	 * @see SceneListener#onMovementEnd(Movement)
	 */
	public void onMovementEnd(Movement movement) {
		Platform.runLater(() -> {
			programAnimation.endAnim(movement);
		});
	}


	/**
	 * @see SceneListener#onStateChanged(Character, State)
	 */
	public void onStateChanged(Character character, State oldState) {
		Platform.runLater(() -> {
			programAnimation.changeState(character);
		});
	}

	public int getHeight(){
		return this.model.getHeight();
	}

	public int getWidth(){
		return this.model.getWidth();
	}

	public Collection<WayPoint> getLesWaypoints(){
		return this.model.getWayPoints();
	}

	public Collection<Character> getLesChar(){
		return this.model.getAllCharacters();
	}
}
