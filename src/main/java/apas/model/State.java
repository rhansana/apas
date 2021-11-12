package apas.model;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import javafx.util.Callback;

import java.util.Objects;

/**
 * État d'un Character. Cette class définit aussi l'apparence qu'aurra un Character dans l'interface grace à une
 * graphicFactory (fabrique de représentations).
 *
 * Les State sont immuable et l'egalité de deux State repose sur leurs noms uniquement.
 */
public class State {

	/**
	 * Nom de l'état.
	 */
	private final String name;

	/**
	 * Fabrique de représentation graphique pour les Characters ayant l'état this, prend en paramètre le Character dont
	 * on veut créer la représentation graphique, et doit retourner un Node javafx qui est cette représentation graphique.
	 */
	private final GraphicFactory graphicFactory;

	/**
	 * Constructeur.
	 * @param name Nom de l'état.
	 * @param graphicFactory Fabrique de représentation graphique pour les Characters ayant l'état qui est créé.
	 * @see GraphicFactory
	 */
	public State(String name, GraphicFactory graphicFactory) {
		Objects.requireNonNull(name, "State name can't be null");
		Objects.requireNonNull(graphicFactory, "State GraphicFactory can't be null");

		this.name = name;
		this.graphicFactory = graphicFactory;
	}

	/**
	 * Accesseur pour le nom de this.
	 * @return Le nom de this.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Crée la représentation graphique d'un Character.
	 * @param character Character dont on veut créer la représentation graphique.
	 * @return Représentation graphique de Character.
	 */
	Node buildGraphic(Character character){
		return graphicFactory.buildGraphic(character);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		State state = (State) o;
		return name.equals(state.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}

	/*
	==================================================================================================================
	======================================= CLASSES ET INTERFACES INTERNES ===========================================
	==================================================================================================================
	*/

	/**
	 * Permet de fabriquer la représentation graphique (à savoir un Node javafx) d'un Character, elle sera destinée être
	 * affiché à la position du Character dans l'interface graphique qui représente la scène ou il se trouve.
	 *
	 * State.ShapeGraphicFactory, State.ImageGraphicFactory implémentent GraphicFactory, et sont destinés a faciliter
	 * la création de représentation avec des formes de base ou des images.
	 *
	 * Le Node javafx renvoyé par buildGraphic peut être n'importe quoi qui appartienne à l'API de javafx, vous pouvez
	 * même mettre des boutons, etc et réagir aux évènement si vous le souhaitez.
	 */
	@FunctionalInterface
	public interface GraphicFactory {

		/**
		 * Crée la représentation graphique d'un Character.
		 * 	 *
		 * 	 *
		 * @param character Character dont on veut créer la représentation graphique.
		 * @return un Node javafx qui est la représentation graphique de Character.
		 */
		Node buildGraphic(Character character);
	}

	/*
	 * Les classes suivantes sont des implémentations de Callback<Character, Node> pour pouvoir créer plus facilement des
	 * fabriques pour des formes simples ou des images.
	 */
	public static abstract class ShapeGraphicFactoryBase implements GraphicFactory {

		private Paint fillColor;
		private Paint strokeColor;
		private double strokeWidth;

		public ShapeGraphicFactoryBase setFillColor(Paint paint) {
			this.fillColor = paint;
			return this;
		}

		public ShapeGraphicFactoryBase setStrokeColor(Paint paint){
			this.strokeColor = paint;
			return this;
		}

		public ShapeGraphicFactoryBase setStrokeWidth(double width){
			this.strokeWidth = width;
			return this;
		}

		protected abstract Shape buildShape();

		/**
		 * Crée la représentation graphique d'un Character.
		 * @param character Character dont on veut créer la représentation graphique.
		 * @return Représentation graphique de Character.
		 */
		@Override
		public Node buildGraphic(Character character) {
			Text name = new Text();
			name.setText(character.getName());
			Shape shape = buildShape();
			shape.setFill(Color.YELLOW);
			if(fillColor != null) shape.setFill(fillColor);
			if(strokeColor != null) shape.setStroke(strokeColor);
			if(strokeWidth != 0) shape.setStrokeWidth(strokeWidth);
			StackPane.setAlignment(name, Pos.CENTER);
			StackPane.setAlignment(shape, Pos.CENTER);
			StackPane graphic = new StackPane();
			graphic.getChildren().addAll(shape,name);
			return graphic;
		}
	}

	public static class RectangleGraphicFactory extends ShapeGraphicFactoryBase{

		private final double width, height;

		public RectangleGraphicFactory(){
			this(50.0,50.0);
		}

		public RectangleGraphicFactory(double width, double height){
			this.width = width;
			this.height = height;
		}

		@Override
		public Shape buildShape() {
			return new Rectangle(width, height);
		}
	}

	public static class CircleGraphicFactory extends ShapeGraphicFactoryBase{

		private final double radius;

		public CircleGraphicFactory(){
			this(30.0);
		}

		public CircleGraphicFactory(double radius){
			this.radius = radius;
		}

		@Override
		public Shape buildShape() {
			return new Circle(radius);
		}
	}

	public static class ImageGraphicFactory implements GraphicFactory {
		private final Image image;

		public ImageGraphicFactory(Image image){
			this.image = image;
		}

		/**
		 * @see GraphicFactory#buildGraphic(Character)
		 */
		@Override
		public Node buildGraphic(Character character) {
			ImageView iv = new ImageView(image);
			iv.setCache(true);
			return iv;
		}
	}
}
