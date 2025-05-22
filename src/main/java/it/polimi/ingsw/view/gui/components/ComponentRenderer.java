package it.polimi.ingsw.view.gui.components;

import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.Components.Connector;
import it.polimi.ingsw.Direction;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * Renders a ship component graphically
 */
public class ComponentRenderer extends Pane {

    private Component component;

    public ComponentRenderer() {
        // Empty constructor
    }

    public void setComponent(Component component) {
        this.component = component;
        redraw();
    }

    public Component getComponent() {
        return component;
    }

    private void redraw() {
        // Clear existing graphics
        getChildren().clear();

        if (component == null) {
            return;
        }

        double width = getPrefWidth();
        double height = getPrefHeight();

        // If width and height not set, use default size
        if (width <= 0) width = 40;
        if (height <= 0) height = 40;

        // Draw the component background based on type
        Rectangle background = new Rectangle(width, height);
        background.setArcWidth(10);
        background.setArcHeight(10);

        // Set color based on component type
        setComponentColor(background);

        getChildren().add(background);

        // Draw connectors
        drawConnectors(width, height);

        // Draw component identifier
        drawComponentIdentifier(width, height);
    }

    private void setComponentColor(Rectangle background) {
        String componentType = component.getClass().getSimpleName();

        if (componentType.contains("Cabin")) {
            background.setFill(Color.LIGHTBLUE);
        } else if (componentType.contains("Engine")) {
            background.setFill(Color.ORANGE);
        } else if (componentType.contains("Cannon")) {
            background.setFill(Color.RED);
        } else if (componentType.contains("Shield")) {
            background.setFill(Color.LIGHTYELLOW);
        } else if (componentType.contains("Life")) {
            background.setFill(Color.LIGHTGREEN);
        } else {
            background.setFill(Color.LIGHTGRAY);
        }

        background.setStroke(Color.BLACK);
        background.setStrokeWidth(1);
    }

    private void drawConnectors(double width, double height) {
        // Draw connectors at each side if present
        double centerX = width / 2;
        double centerY = height / 2;

        // Get connectors from the component
        for (Direction dir : Direction.values()) {
            Connector connector = component.getConnectorByDirection(dir);
            if (connector != null /*&& connector.isConnectable()*/) {
                // Draw connector
                switch (dir) {
                    case NORTH:
                        drawConnector(centerX, 0, centerX, centerY, dir);
                        break;
                    case EAST:
                        drawConnector(width, centerY, centerX, centerY, dir);
                        break;
                    case SOUTH:
                        drawConnector(centerX, height, centerX, centerY, dir);
                        break;
                    case WEST:
                        drawConnector(0, centerY, centerX, centerY, dir);
                        break;
                }
            }
        }
    }

    private void drawConnector(double startX, double startY, double endX, double endY, Direction dir) {
        // Draw a line representing the connector
        Line line = new Line(startX, startY, (startX + endX) / 2, (startY + endY) / 2);
        line.setStrokeWidth(3);
        line.setStroke(Color.BLACK);

        getChildren().add(line);

        // Draw a circle at the connection point
        Circle circle = new Circle((startX + endX) / 2, (startY + endY) / 2, 3);
        circle.setFill(Color.BLACK);

        getChildren().add(circle);
    }

    private void drawComponentIdentifier(double width, double height) {
        // Draw a letter or symbol to identify the component type
        String identifier = getComponentIdentifier();

        Text text = new Text(identifier);
        text.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        text.setFill(Color.BLACK);

        // Center the text
        double textWidth = text.getBoundsInLocal().getWidth();
        double textHeight = text.getBoundsInLocal().getHeight();

        text.setX((width - textWidth) / 2);
        text.setY((height + textHeight) / 2);

        getChildren().add(text);
    }

    private String getComponentIdentifier() {
        String componentType = component.getClass().getSimpleName();

        if (componentType.contains("Cabin")) {
            return "C";
        } else if (componentType.contains("Engine")) {
            return "E";
        } else if (componentType.contains("Cannon")) {
            return "W";
        } else if (componentType.contains("Shield")) {
            return "S";
        } else if (componentType.contains("Life")) {
            return "L";
        } else {
            return "?";
        }
    }
}