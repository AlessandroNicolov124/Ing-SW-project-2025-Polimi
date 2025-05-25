package it.polimi.ingsw.view.gui.components;

import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.Direction;
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
    private static final int GRID_HEIGHT = 10;

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

        // Aggiorna la vista se ci sono già componenti (es. la starting cabin)
        if (!player.getShip().getComponents().isEmpty()) {
            updateShipView();
        }
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

    private void handleTileClick(Position clickedPosition) {
        // Here you would implement placing the current component
        // This would interact with a method in the parent scene
        // For now just log the click
        System.out.println("Clicked position: " + clickedPosition.getX() + "," + clickedPosition.getY());

        // Notify the parent scene that this position was clicked
        // We need to traverse up to find the scene
        javafx.scene.Scene scene = getScene();
        if (scene != null && scene.getUserData() != null) {
            Object userData = scene.getUserData();
            if (userData instanceof ShipBuildingScene) {

                ShipBuildingScene shipBuildingScene = (ShipBuildingScene) userData;
                // Se non ci sono componenti, è il primo click: forziamo la cabin al centro
                if (player.getShip().getComponents().isEmpty()) {

                    Position center = player.getShip().getStartingPosition();
                    //System.out.println("currentComponent: " + shipBuildingScene.getCurrentComponent());
                    shipBuildingScene.handlePlaceComponent(center); // ignora clickedPosition
                } else {
                    // Comportamento normale dopo il primo piazzamento
                    shipBuildingScene.handlePlaceComponent(clickedPosition);
                }
            }
        }
    }

    public void updateShipView() {
        System.out.println("=== UPDATING SHIP VIEW ===");

        // Clear all tiles first
        for (ComponentTile tile : tiles.values()) {
            tile.clearComponent();
        }

        // Get the ship components from the player's ship
        Map<Component, Position> shipComponents = player.getShip().getComponents();

        // Update tiles based on ship components
        for (Map.Entry<Component, Position> entry : shipComponents.entrySet()) {
            Component comp = entry.getKey();
            Position pos = entry.getValue();

            System.out.println("Placing component " + comp.getClass().getSimpleName() +
                    " at position (" + pos.getX() + "," + pos.getY() + ")");


            ComponentTile tile = tiles.get(pos);
            if (tile != null) {
                tile.setComponent(comp);
            }
        }
        System.out.println("=== SHIP VIEW UPDATE COMPLETE ===");
    }

    public ComponentTile getTileAt(Position position) {
        return tiles.get(position);
    }

    public void highlightValidPositions(Component component) {
        //System.out.println("Highlighting valid positions for " + component.getClass().getSimpleName());

        // Clear any previous highlights
        clearHighlights();

        // Check each position and highlight valid ones
        for (Position pos : tiles.keySet()) {
            // Crea una copia temporanea del componente per il test
            try {
                // Usa un metodo di controllo che non modifica lo stato della nave
                if (canPlaceComponentAt(component, pos)) {
                    tiles.get(pos).setHighlighted(true);
                    //System.out.println("Position (" + pos.getX() + "," + pos.getY() + ") is valid");
                }
            } catch (Exception e) {
                // Se c'è un errore nel controllo, ignora questa posizione
                System.err.println("Error checking position (" + pos.getX() + "," + pos.getY() + "): " + e.getMessage());
            }

//            if (controller.addComponentForPlayer(player, component, pos)) {
//                tiles.get(pos).setHighlighted(true);
//            }
        }
    }

    public void clearHighlights() {
        for (ComponentTile tile : tiles.values()) {
            tile.setHighlighted(false);
        }
    }

    // Metodo aggiuntivo per verificare se la posizione è valida
    private boolean isPositionValid(Position pos) {
        return pos.getX() >= 0 && pos.getX() < GRID_WIDTH &&
                pos.getY() >= 0 && pos.getY() < GRID_HEIGHT;
    }

    // Metodo helper per controllare se un componente può essere posizionato senza modificare la nave
    private boolean canPlaceComponentAt(Component component, Position position) {
        // Questo metodo dovrebbe controllare se la posizione è valida senza modificare lo stato
        // Verifica se la posizione è già occupata
        if(player.getShip().isPositionOccupied(position)){
            return false;
        }

        //Verifica se la posizione è nei limiti della griglia
        if (!isPositionValid(position)) {
            System.out.println("Position out of bounds");
            return false;
        }

        Map<Component, Position> currentComponents = player.getShip().getComponents();
        // Se è la prima componente (starting cabin), deve essere posizionata al centro
        if (currentComponents.isEmpty() && !position.equals(player.getShip().getStartingPosition())) {
            return false;
        }

        // Altrimenti, deve essere adiacente a un componente esistente
        // Se non è il primo componente, deve essere collegato a un componente esistente
        if (!currentComponents.isEmpty()) {
            boolean isConnected = true;

            for (Direction direction : Direction.values()) {
                Position adjacentPosition = direction.getAdjacentPosition(position);
                Component adjacentComponent = player.getShip().getComponentAt(adjacentPosition);

                if (adjacentComponent != null && !component.canConnectTo(adjacentComponent, direction)) {
                    isConnected = false;
                    break;
                }
            }

            if (!isConnected) {
                System.out.print(" not connected, ");
                return false;
            }
        }
        return true;
    }
}