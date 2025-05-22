package it.polimi.ingsw.view.gui.components;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Label for displaying grid coordinates
 */
public class CoordinateLabel extends StackPane {

    public CoordinateLabel(String text) {
        setPrefSize(30, 30);

        Rectangle background = new Rectangle(30, 30);
        background.setFill(Color.LIGHTGRAY);
        background.setStroke(Color.GRAY);
        background.setStrokeWidth(1);

        Label label = new Label(text);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        label.setAlignment(Pos.CENTER);

        getChildren().addAll(background, label);
    }
}