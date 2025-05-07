package it.polimi.ingsw.view.tui.components;

import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.GameController;
import it.polimi.ingsw.Player;
import it.polimi.ingsw.Position;
import it.polimi.ingsw.ShipBuilder;
import it.polimi.ingsw.Components.*; // For specific component types
import it.polimi.ingsw.Direction; // For rotation

import java.util.Map;
import java.util.Scanner;

public class ShipBuildingHandler {

    private final ConsoleManager console;
    private final GameController controller;
    private final Player currentPlayer; // Assuming we know the current player

    public ShipBuildingHandler(ConsoleManager console, GameController controller, Player currentPlayer) {
        this.console = console;
        this.controller = controller;
        this.currentPlayer = currentPlayer;
    }

    public void manageShipBuilding() {
        console.printMessage("=== Ship Building Phase ===");
        boolean buildingDone = false;

        while (!buildingDone) {
            displayShip();
            printShipBuildingMenu();
            String choice = console.readCommand().trim().toLowerCase();

            switch (choice) {
                case "p": // Place component
                    handlePlaceComponent();
                    break;
                case "r": // Remove component
                    handleRemoveComponent();
                    break;
                case "t": // Rotate component (t for turn)
                    handleRotateComponent();
                    break;
                case "v": // Validate and finish
                    if (handleValidateAndFinish()) {
                        buildingDone = true;
                    }
                    break;
                case "q": // Quit ship building (for now, just exits this phase)
                    console.printMessage("Exiting ship building...");
                    buildingDone = true;
                    break;
                default:
                    console.printError("Invalid choice. Please try again.");
                    break;
            }
        }
    }

    private void displayShip() {
        console.printMessage("\n--- Your Ship ---");
        ShipBuilder ship = currentPlayer.getShip();
        if (ship == null) {
            console.printError("No ship found for player: " + currentPlayer.getNickName());
            return;
        }

        // Simple text-based representation. This can be made much fancier.
        int width = ship.getWidth();
        int height = ship.getHeight();
        Map<Component, Position> components = ship.getComponents();

        // Create a grid
        String[][] grid = new String[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                grid[i][j] = "[ ]"; // Empty slot
            }
        }

        // Fill grid with components
        for (Map.Entry<Component, Position> entry : components.entrySet()) {
            Component comp = entry.getKey();
            Position pos = entry.getValue();
            String compSymbol = getComponentSymbol(comp);
            if (pos.getY() >= 0 && pos.getY() < height && pos.getX() >= 0 && pos.getX() < width) {
                grid[pos.getY()][pos.getX()] = "[" + compSymbol + "]";
            }
        }
        
        // Add cooordinates
        console.printMessage("  X:0  1  2  3  4  5  6  7  8  9"); // Assuming max width 10 for now
        // Print grid
        for (int i = 0; i < height; i++) {
            StringBuilder rowStr = new StringBuilder("Y:" + i + " ");
            for (int j = 0; j < width; j++) {
                rowStr.append(grid[i][j]);
            }
            console.printMessage(rowStr.toString());
        }
        console.printMessage("--------------------");
    }

    private String getComponentSymbol(Component component) {
        if (component instanceof Cabin) return "C";
        if (component instanceof SingleEngine) return "E";
        if (component instanceof DoubleEngine) return "E"; // Could be E2
        if (component instanceof SingleCannon) return "S"; // S for single cannon
        if (component instanceof DoubleCannon) return "D"; // D for double cannon
        if (component instanceof CargoHolds) return "H"; // H for holds
        if (component instanceof BatteryCompartment) return "B";
        if (component instanceof ShieldGenerator) return "G";
        if (component instanceof LifeSupportSystem) return "L";
        if (component instanceof StructuralModule) return "X"; // For structural
        return "?"; // Unknown
    }


    private void printShipBuildingMenu() {
        console.printMessage("\nShip Building Options:");
        console.printMessage(" (p) Place Component");
        console.printMessage(" (r) Remove Component");
        console.printMessage(" (t) Rotate Component");
        console.printMessage(" (v) Validate and Finish Ship");
        console.printMessage(" (q) Quit Ship Building");
        console.printMessage("Enter your choice: ");
    }

    private void handlePlaceComponent() {
        console.printMessage("--- Place Component ---");
        // 1. Ask for component type
        String typeStr = console.askQuestion("Enter component type (e.g., Cabin, SingleEngine, DoubleCannon):");
        
        // 2. Ask for position
        int x = -1, y = -1;
        try {
            x = Integer.parseInt(console.askQuestion("Enter X coordinate:"));
            y = Integer.parseInt(console.askQuestion("Enter Y coordinate:"));
        } catch (NumberFormatException e) {
            console.printError("Invalid coordinates.");
            return;
        }
        Position pos = new Position(x, y);

        // 3. Create and initialize component (This is crucial and complex)
        Component newComponent = createAndInitializeComponent(typeStr);
        if (newComponent == null) {
            console.printError("Invalid component type or failed to initialize.");
            return;
        }

        // 4. Call controller
        boolean placementSuccess = controller.addComponentForPlayer(currentPlayer, newComponent, pos);
        
        if (placementSuccess) {
            console.printMessage(typeStr + " successfully placed at (" + x + "," + y + ").");
        } else {
            console.printError("Failed to place " + typeStr + " at (" + x + "," + y + "). Check position and connections.");
        }
    }

    private Component createAndInitializeComponent(String typeStr) {
        // This is a simplified factory. A more robust solution would use a proper factory pattern
        // or reflection, and handle connector initialization more dynamically.
        // For now, using default/sensible connector setups.
        Component component = null;
        switch (typeStr.toLowerCase()) {
            case "cabin":
                boolean isStartingCabin = currentPlayer.getShip().getComponents().isEmpty(); // First component is starting cabin
                component = new Cabin(isStartingCabin);
                // Cabins usually have universal connectors or manage them internally
                break;
            case "singleengine":
                Engine singleEngine = new SingleEngine();
                // Example: Default for a single engine might be Double on NORTH, Null elsewhere
                singleEngine.initializeConnectors(new DoubleConnectorType(), new NullConnectorType(), new NullConnectorType(), new NullConnectorType());
                component = singleEngine;
                break;
            case "doubleengine":
                Engine doubleEngine = new DoubleEngine();
                // Example: Default for a double engine
                doubleEngine.initializeConnectors(new DoubleConnectorType(), new NullConnectorType(), new NullConnectorType(), new NullConnectorType());
                component = doubleEngine;
                break;
            case "singlecannon":
                SingleCannon singleCannon = new SingleCannon(Direction.NORTH); // Default firing NORTH
                // Cannons might need specific connector setups based on design
                singleCannon.initializeConnectors(new SingleConnectorType(), new NullConnectorType(), new NullConnectorType(), new NullConnectorType());
                component = singleCannon;
                break;
            case "doublecannon":
                DoubleCannon doubleCannon = new DoubleCannon(Direction.NORTH); // Default firing NORTH
                doubleCannon.initializeConnectors(new DoubleConnectorType(), new NullConnectorType(), new NullConnectorType(), new NullConnectorType());
                component = doubleCannon;
                break;
            // Add more cases for other components (CargoHolds, BatteryCompartment, etc.)
            // Each will require specific initialization, especially for connectors.
            default:
                console.printError("Unknown component type: " + typeStr);
                return null;
        }
        return component;
    }

    private void handleRemoveComponent() {
        console.printMessage("--- Remove Component ---");
        int x = -1, y = -1;
        try {
            x = Integer.parseInt(console.askQuestion("Enter X coordinate of component to remove:"));
            y = Integer.parseInt(console.askQuestion("Enter Y coordinate of component to remove:"));
        } catch (NumberFormatException e) {
            console.printError("Invalid coordinates.");
            return;
        }
        Position posToRemove = new Position(x,y);
        Component compToRemove = currentPlayer.getShip().getComponentAt(posToRemove);

        if (compToRemove != null) {
            controller.removeComponentForPlayer(currentPlayer, compToRemove);
            console.printMessage("Attempted to remove component at (" + x + "," + y + ").");
        } else {
            console.printError("No component found at (" + x + "," + y + ").");
        }
    }

    private void handleRotateComponent() {
        console.printMessage("--- Rotate Component ---");
        int x = -1, y = -1;
        try {
            x = Integer.parseInt(console.askQuestion("Enter X coordinate of component to rotate:"));
            y = Integer.parseInt(console.askQuestion("Enter Y coordinate of component to rotate:"));
        } catch (NumberFormatException e) {
            console.printError("Invalid coordinates.");
            return;
        }
        Position posToRotate = new Position(x,y);
        Component compToRotate = currentPlayer.getShip().getComponentAt(posToRotate);

        if (compToRotate == null) {
            console.printError("No component found at (" + x + "," + y + ").");
            return;
        }

        String rotationType = console.askQuestion("Rotate (c)lockwise, (a)nti-clockwise, or (o)pposite?").toLowerCase();
        switch (rotationType) {
            case "c":
                controller.rotateComponentClockwiseForPlayer(currentPlayer, compToRotate);
                break;
            case "a":
                controller.rotateComponentCounterClockwiseForPlayer(currentPlayer, compToRotate);
                break;
            case "o":
                controller.rotateComponentOppositeForPlayer(currentPlayer, compToRotate);
                break;
            default:
                console.printError("Invalid rotation type.");
                return;
        }
        console.printMessage("Attempted to rotate component at (" + x + "," + y + ").");
    }

    private boolean handleValidateAndFinish() {
        console.printMessage("Validating ship...");
        boolean isValid = controller.validateShipForPlayer(currentPlayer);
        if (isValid) {
            console.printMessage("Ship is valid! Construction complete.");
            // Here, you might want to signal the main TUI loop to change state
            return true;
        } else {
            console.printError("Ship is NOT valid. Please check your design.");
            // For example: Ensure all parts are connected, has engine and cabin, cannons not blocked.
            return false;
        }
    }
} 