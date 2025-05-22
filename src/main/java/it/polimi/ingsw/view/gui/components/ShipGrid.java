package it.polimi.ingsw.view.gui.components;

import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.GameController;
import it.polimi.ingsw.Player;
import it.polimi.ingsw.Position;
import it.polimi.ingsw.view.gui.scenes.ShipBuildingScene;
import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;

import java.util.HashMap;
import java.util.Map;

/**
 * A grid representation of the player's ship
 */
public class ShipGrid extends GridPane {

    private static final int CELL_SIZE = 50;
    private static final int GRID_WIDTH = 10;
    private static final int GRID_HEIGHT = 8;

    private final GameController controller;
    private final Player player;
    private final Map<Position, ComponentTile> tiles;

    public ShipGrid(GameController controller, Player player) {
        this.controller = controller;
        this.player = player;
        this.tiles = new HashMap<>();

        setPadding(new Insets(10));
        setHgap(2);
        setVgap(2);

        initializeGrid();
    }

    private void initializeGrid() {
        // Create the grid cells
        for (int y = 0; y < GRID_HEIGHT; y++) {
            for (int x = 0; x < GRID_WIDTH; x++) {
                final int finalX = x;
                final int finalY = y;

                ComponentTile tile = new ComponentTile(CELL_SIZE);

                // Add coordinates as user data
                Position position = new Position(finalX, finalY);
                tile.setUserData(position);

                // Add click handler
                tile.setOnMouseClicked(event -> handleTileClick(position));

                // Store in our tiles map
                tiles.put(position, tile);

                // Add to grid
                add(tile, x, y);
            }
        }

        // Add coordinates labels
        for (int x = 0; x < GRID_WIDTH; x++) {
            add(new CoordinateLabel("" + x), x, GRID_HEIGHT);
        }

        for (int y = 0; y < GRID_HEIGHT; y++) {
            add(new CoordinateLabel("" + y), GRID_WIDTH, y);
        }
    }

    private void handleTileClick(Position position) {
        // Here you would implement placing the current component
        // This would interact with a method in the parent scene
        // For now just log the click
        System.out.println("Clicked position: " + position.getX() + "," + position.getY());

        // Notify the parent scene that this position was clicked
        // We need to traverse up to find the scene
        javafx.scene.Scene scene = getScene();
        if (scene != null && scene.getUserData() != null) {
            Object userData = scene.getUserData();
            if (userData instanceof ShipBuildingScene) {
                ShipBuildingScene shipBuildingScene =
                        (ShipBuildingScene) userData;
                shipBuildingScene.handlePlaceComponent(position);
            }
        }
    }

    public void updateShipView() {
        // Clear all tiles first
        for (ComponentTile tile : tiles.values()) {
            tile.clearComponent();
        }

        // Get the ship components from the player's ship
        Map<Component, Position> shipComponents = player.getShip().getComponents();

        // Update tiles based on ship components
        for (Map.Entry<Component, Position> entry : shipComponents.entrySet()) {
            Position pos = entry.getValue();
            Component comp = entry.getKey();

            ComponentTile tile = tiles.get(pos);
            if (tile != null) {
                tile.setComponent(comp);
            }
        }
    }

    public ComponentTile getTileAt(Position position) {
        return tiles.get(position);
    }

    public void highlightValidPositions(Component component) {
        // Clear any previous highlights
        clearHighlights();

        // Check each position and highlight valid ones
        for (Position pos : tiles.keySet()) {
            if (controller.addComponentForPlayer(player, component, pos)) {
                tiles.get(pos).setHighlighted(true);
            }
        }
    }

    public void clearHighlights() {
        for (ComponentTile tile : tiles.values()) {
            tile.setHighlighted(false);
        }
    }
}