package it.polimi.ingsw.view.gui.components;

import it.polimi.ingsw.Components.Component;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Visual representation of a ship component tile
 */
public class ComponentTile extends StackPane {

    private Rectangle background;
    private ComponentRenderer renderer;
    private boolean isHighlighted = false;

    public ComponentTile(int size) {
        setPrefSize(size, size);

        // Create the background
        background = new Rectangle(size, size);
        background.setFill(Color.LIGHTGRAY);
        background.setStroke(Color.BLACK);
        background.setStrokeWidth(1);

        // Create the component renderer (will be empty until a component is set)
        renderer = new ComponentRenderer();
        renderer.setPrefSize(size, size);

        getChildren().addAll(background, renderer);
    }

    public void setComponent(Component component) {
        if (component == null) {
            clearComponent();
            return;
        }

        renderer.setComponent(component);
        background.setFill(Color.WHITE);
    }

    public void clearComponent() {
        renderer.setComponent(null);
        background.setFill(Color.LIGHTGRAY);
        setHighlighted(false);
    }

    public void setHighlighted(boolean highlighted) {
        this.isHighlighted = highlighted;
        if (highlighted) {
            background.setStroke(Color.GREEN);
            background.setStrokeWidth(2);
        } else {
            background.setStroke(Color.BLACK);
            background.setStrokeWidth(1);
        }
    }

    public boolean isHighlighted() {
        return isHighlighted;
    }
}