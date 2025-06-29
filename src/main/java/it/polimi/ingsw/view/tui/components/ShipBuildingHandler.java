package it.polimi.ingsw.view.tui.components;

import it.polimi.ingsw.Player;
import it.polimi.ingsw.Position;
import it.polimi.ingsw.ShipBuilder;
import it.polimi.ingsw.Components.*; // For specific component types
import it.polimi.ingsw.Direction; // For rotation
import it.polimi.ingsw.network.client.GeneralClientIf;
import it.polimi.ingsw.network.messages.RotationType; // <<< ADD THIS IMPORT

import java.util.*;
import java.io.IOException;

public class ShipBuildingHandler {

    private final ConsoleManager console;
    // private final GameController controller; // Keep for local validation/display logic if needed, or remove
    private final Player currentPlayer; // Assuming we know the current player
    private final GeneralClientIf simpleClient; // Added socket client
    private final GalaxyTruckerTUI parentTUI; // Reference to parent TUI for state checking

    private final Random random = new Random();
    private Component currentComponent = null;
    private boolean waitingForServerResponse = false;
    private List<Component> stashedComponents = new ArrayList<>();


    public ShipBuildingHandler(ConsoleManager console, GeneralClientIf simpleClient, Player currentPlayer, GalaxyTruckerTUI parentTUI) {
        this.console = console;
        this.simpleClient = simpleClient;
        this.currentPlayer = currentPlayer;
        this.parentTUI = parentTUI;
        // this.controller = controller; // Remove if no longer needed
    }

    // Method to be called to redraw the ship display
    public void refreshDisplay() {
        console.printMessage("\n=== Ship Updated ===");
        displayShip();
        console.printMessage("===================");
    }

    // Method to redisplay the ship building menu after async operations
    public void refreshMenuDisplay() {
        if (!waitingForServerResponse) {
            console.printMessage("\n--- Menu Refreshed ---");
        }
        printShipBuildingMenu(currentComponent != null);
    }

    // Method to set the current component from async response
    public void setCurrentComponent(Component component) {
        this.currentComponent = component;
        waitingForServerResponse = false;
    }

    // Method to check if we should continue ship building
    private boolean shouldContinueBuilding() {
        return parentTUI.isInShipBuildingState();
    }

    public void manageShipBuilding() {
        console.printMessage("=== Ship Building Phase ===");
        
        // Display the initial ship
        displayShip();

        boolean buildingDone = false;

        // First element is always the starting cabin
        currentComponent = createCabin(true);
        console.printMessage("Placing starting cabin...");
        handlePlaceComponent(currentComponent);
        
        // After placing the starting cabin, automatically pick the first component
        console.printMessage("Picking first component...");
        handlePickComponent();
        
        boolean buildingDone = false;
        while (!buildingDone && shouldContinueBuilding()) {
            
            // Only show menu if not waiting for server response
            if (!waitingForServerResponse) {
                if (currentComponent != null) {
                    printShipBuildingMenu(true);
                } else {
                    printShipBuildingMenu(false);
                }

                String choice = console.readCommand().trim().toLowerCase();
                handleUserChoice(choice);
            } else {
                // Wait a bit for server response
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
          
    

    private void handleUserChoice(String choice) {
        switch (choice) {
            case "p": // Place component
                if (currentComponent != null) {
                    handlePlaceComponent(currentComponent);
                } else {
                    console.printError("No component available to place!");
                }
                break;
            case "r": // Remove component from ship
                handleRemoveComponent();
                break;
            case "tc": // Rotate CURRENT (held) component
                if (currentComponent != null) {
                    currentComponent = handleRotateHeldComponent(currentComponent);
                    displayComponent(currentComponent);
                } else {
                    console.printError("No current component to rotate!");
                }
                break;
            case "ts": // Rotate component ON SHIP
                handleRotateComponentOnShip();
                break;
            case "s": // Skip current component
                if (currentComponent != null) {
                    console.printMessage("Skipping " + currentComponent.getClass().getSimpleName());
                    currentComponent = null;
                } else {
                    console.printError("No component to skip!");
                }
                break;
            case "pick": // Pick a component
                if (currentComponent == null) {
                    handlePickComponent();
                } else {
                    console.printMessage("You already have a component: " + currentComponent.getClass().getSimpleName());
                }
                break;
            case "v": // Validate and finish
                handleValidateAndFinish();
                break;
            case "ds": // Display ship
                displayShip();
                break;
            case "dc": // Display current component
                if (currentComponent != null) {
                    displayComponent(currentComponent);
                } else {
                    console.printMessage("No current component to display.");
                }
                break;
            case "q": // Quit
                console.printMessage("Exiting ship building...");
                return;
            default:
                console.printError("Invalid choice. Please try again.");
                break;
        }
    }

    private void handlePickComponent() {
        waitingForServerResponse = true;
        try {
            simpleClient.pickComponentCard(currentPlayer.getId());
            console.printMessage("Requesting component from server...");
        } catch (Exception e) {
            console.printError("Error picking component: " + e.getMessage());
            waitingForServerResponse = false;
        }
    }   
    
    public void displayCurrentComponent() {
        if (currentComponent != null) {
            console.printMessage("\nCurrent component: " + currentComponent.getClass().getSimpleName());
            displayComponent(currentComponent);
        }
    }

    private void displayComponent(Component currentComponent) {
        console.printMessage("\n--- Current Component: " + currentComponent.getClass().getSimpleName() + " ---");
        
        // Create a 3x3 grid to display the component
        String[][] grid = new String[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                grid[i][j] = "    "; // 4 spaces for alignment
            }
        }
        
        // Place component symbol in center
        grid[1][1] = " " + getComponentSymbol(currentComponent) + " ";
        
        // Add connectors around the component
        grid[0][1] = " " + getConnectionSymbol(currentComponent.getConnectorByDirection(Direction.NORTH)) + " ";
        grid[1][2] = " " + getConnectionSymbol(currentComponent.getConnectorByDirection(Direction.EAST)) + " ";
        grid[2][1] = " " + getConnectionSymbol(currentComponent.getConnectorByDirection(Direction.SOUTH)) + " ";
        grid[1][0] = " " + getConnectionSymbol(currentComponent.getConnectorByDirection(Direction.WEST)) + " ";
        
        console.printMessage("--------------------");
        for (int i = 0; i < 3; i++) {
            StringBuilder rowStr = new StringBuilder();
            for (int j = 0; j < 3; j++) {
                rowStr.append(grid[i][j]);
            }
            console.printMessage(rowStr.toString());
        }
        console.printMessage("--------------------");
    }

    private String getConnectionSymbol(Connector connector) {
        if (connector == null) {
            return "  "; // Empty for no connector
        }

        return switch (connector.getConnectorType()) {
            case SingleConnectorType s -> "S-";
            case DoubleConnectorType d -> "D=";
            case UniversalConnectorType u -> "U≡";
            case NullConnectorType n -> "  "; // Empty for null connector
            default -> "??";
        };
    }

    private void displayShip() {
        synchronized (console) {  // Synchronize on console to prevent concurrent display
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

            // Create a larger grid where each component occupies a 3x3 area
            // This gives space to show connectors around each component
            int gridHeight = height * 3;
            int gridWidth = width * 3;
            String[][] grid = new String[gridHeight][gridWidth];
            for (int i = 0; i < gridHeight; i++) {
                for (int j = 0; j < gridWidth; j++) {
                    grid[i][j] = "    "; // Empty slot with 4 spaces
                }
            }

            // Fill grid with components
            for (Map.Entry<Component, Position> entry : components.entrySet()) {
                Component comp = entry.getKey();
                Position pos = entry.getValue();

                // Calculate the position in the expanded grid
                int baseY = pos.getY() * 3;
                int baseX = pos.getX() * 3;

                // Place component symbol in center
                String compSymbol = getComponentSymbol(comp);
                grid[baseY + 1][baseX + 1] = "[" + compSymbol + "]";

                // Add connectors around the component
                // North connector (above)
                Connector northConn  = comp.getConnectorByDirection(Direction.NORTH);
                if (northConn != null && northConn.getConnectorType().getClass() != NullConnectorType.class) {
                    grid[baseY][baseX + 1] = "[" + getConnectionSymbol(northConn) + "]";
                }

                // South connector (below)
                Connector southConn = comp.getConnectorByDirection(Direction.SOUTH);
                if (southConn != null && southConn.getConnectorType().getClass() != NullConnectorType.class) {
                    grid[baseY + 2][baseX + 1] = "[" + getConnectionSymbol(southConn) + "]";
                }

                // East connector (right)
                Connector eastConn = comp.getConnectorByDirection(Direction.EAST);
                if (eastConn != null && eastConn.getConnectorType().getClass() != NullConnectorType.class) {
                    grid[baseY + 1][baseX + 2] = "[" + getConnectionSymbol(eastConn) + "]";
                }

                // West connector (left)
                Connector westConn = comp.getConnectorByDirection(Direction.WEST);
                if (westConn != null && westConn.getConnectorType().getClass() != NullConnectorType.class) {
                    grid[baseY + 1][baseX] = "[" + getConnectionSymbol(westConn) + "]";
                }

            }

            // Add cooordinates
            console.printMessage("      " + getCoordinateHeader(width));

            for (int i = 0; i < gridHeight; i++) {
                StringBuilder rowStr = new StringBuilder();

                // Add Y coordinate label only for the middle row of each cell
                if (i % 3 == 1) {
                    rowStr.append("Y:" + (i / 3) + "   ");
                } else {
                    rowStr.append("      "); // Spacing to align with coordinates
                }

                // Add row content
                for (int j = 0; j < gridWidth; j++) {
                    rowStr.append(grid[i][j]);
                }
                console.printMessage(rowStr.toString());
            }
            console.printMessage("------------------------------------");
        }
    }

    private String getCoordinateHeader(int width) {
        StringBuilder header = new StringBuilder("      ");
        for (int i = 0; i < width; i++) {
            header.append("     " + i + "     "); // Center the coordinate number in the 3-cell width
        }
        return header.toString();
    }

    private String getComponentSymbol(Component component) {
        return switch (component) {
            case Cabin c -> "CA";
            case SingleCannon sc -> "C1";
            case DoubleCannon dc -> "C2";
            case SingleEngine se -> "E1";
            case DoubleEngine de -> "E2";
            case CargoHolds c -> "CH";
            case BatteryCompartment b -> "BC";
            case ShieldGenerator sg -> "SG";
            case LifeSupportSystem ls -> "LS";
            case StructuralModule sm -> "SM";
            default -> "??";
        };
    }


    private void printShipBuildingMenu(boolean hasCurrentComponent) {
        console.printMessage("\n--- Ship Building Actions ---");
        if (hasCurrentComponent) {
            console.printMessage("P  - Place current component");
            console.printMessage("TC - Rotate current component (clockwise)");
            console.printMessage("S  - Skip current component (draw next)");
        } else {
            console.printMessage("PICK - Pick a new component from deck");
            console.printMessage("No component to place/rotate. Draw one or manage ship.");
        }
        console.printMessage("R  - Remove component from ship");
        console.printMessage("TS - Rotate component on ship");
        console.printMessage("V  - Validate and Finish ship building");
        console.printMessage("Q  - Quit ship building (temporary)");
        console.printMessage("DS - Display Ship again");
        console.printMessage("DC - Display Current component again");
        console.printMessage("DEBUGSKIP - Force proceed (skip validation)"); // New debug option
        console.printMessage("---------------------------");
        console.printMessage("Enter your choice:");
    }

    private void handlePlaceComponent(Component component) {
        console.printMessage("--- Place Component ---");
        
        Position pos;

        // Se è una cabina iniziale, usa la posizione iniziale
        if (component instanceof Cabin cabin && cabin.isStartingCabin()) {
            pos = currentPlayer.getShip().getStartingPosition();
            console.printMessage("Placing starting cabin at default position: (" + pos.getX() + "," + pos.getY() + ")");
        } else {
            int x, y;
            try {
                x = Integer.parseInt(console.askQuestion("Enter X coordinate:"));
                y = Integer.parseInt(console.askQuestion("Enter Y coordinate:"));
                pos = new Position(x, y);
            } catch (NumberFormatException e) {
                console.printError("Invalid coordinates.");
                return;
            }
        }

        waitingForServerResponse = true;
        try {
            console.printMessage("Placement request sent for " + component.getClass().getSimpleName() + 
                               " at (" + pos.getX() + "," + pos.getY() + "). Waiting for server confirmation...");
            simpleClient.placeComponent(component, pos, currentPlayer.getId());
        } catch (Exception e) {
            console.printError("Error sending placement request: " + e.getMessage());
            waitingForServerResponse = false;
        }
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
        // Component compToRemove = currentPlayer.getShip().getComponentAt(posToRemove);

        // Send remove request via network
        try {
            simpleClient.removeComponent(posToRemove, currentPlayer.getId());
            console.printMessage("Remove request sent for position (" + x + "," + y + "). Waiting for server confirmation...");
            // Success/failure message handled asynchronously by RemoveComponentResponse
        } catch (IllegalStateException e) {
            console.printError("Error sending remove request: " + e.getMessage());
        } catch (Exception e) {
            console.printError("Error sending remove request: " + e.getMessage());
        }

        // if (compToRemove != null) {
        //    boolean res = controller.removeComponentForPlayer(currentPlayer, compToRemove);
        //    console.printMessage("Attempted to remove component at (" + x + "," + y + "): result: " + res);
        // } else {
        //    console.printError("No component found at (" + x + "," + y + ").");
        // }
    }

    // Modified handleRotateComponent
    // The 'component' argument is the one currently held by the player (drawn from deck), NOT the one on the ship.
    // This method now handles rotating a component already PLACED on the ship.
    private void handleRotateComponentOnShip() { 
        console.printMessage("--- Rotate Component on Ship ---");
        int x, y;
        try {
            x = Integer.parseInt(console.askQuestion("Enter X coordinate of component to rotate:"));
            y = Integer.parseInt(console.askQuestion("Enter Y coordinate of component to rotate:"));
        } catch (NumberFormatException e) {
            console.printError("Invalid coordinates.");
            return;
        }
        Position posToRotate = new Position(x, y);

        // Check if component exists locally first (optional, server will validate anyway)
        Component compToRotateCheck = currentPlayer.getShip().getComponentAt(posToRotate);
        if (compToRotateCheck == null) {
            console.printError("No component found at (" + x + "," + y + ") on your ship view.");
            // return; // Allow sending anyway, server has final say
        }

        String rotationTypeStr = console.askQuestion("Rotate (c)lockwise, (a)nti-clockwise, or (o)pposite?").toLowerCase();
        RotationType rotationType;
        switch (rotationTypeStr) {
            case "c": rotationType = RotationType.CLOCKWISE; break;
            case "a": rotationType = RotationType.ANTI_CLOCKWISE; break;
            case "o": rotationType = RotationType.OPPOSITE; break;
            default: console.printError("Invalid rotation type."); return;
        }

        try {
            simpleClient.rotateComponent(currentPlayer.getId(), posToRotate, rotationType);
            console.printMessage("Rotation request sent for position (" + x + "," + y + "). Waiting for server confirmation...");
        } catch (IOException | IllegalStateException e) {
            console.printError("Error sending rotation request: " + e.getMessage());
        }
        catch (Exception e) {
            console.printError("Error sending rotation request: " + e.getMessage());
        }
    }

    // This old method rotated the component the player was *holding*
    private Component handleRotateHeldComponent(Component component) {
        console.printMessage("--- Rotate Current Component ---");
        String rotationTypeStr = console.askQuestion("Rotate (c)lockwise, (a)nti-clockwise, or (o)pposite?").toLowerCase();
        
        // This rotation happens client-side ONLY, before placing.
        // We need the component rotation logic locally or accessible here.
        // Let's assume Component class has rotate methods, or we call local utility.
        // For now, we modify the component directly.

        RotationType rotationType = null;
        switch (rotationTypeStr) {
            case "c":
                component.setComponentDirection(component.getComponentDirection().getClockwise());
                // Also rotate connectors
                component.getConnections().forEach(conn -> conn.setDirection(conn.getDirection().getClockwise()));
                rotationType = RotationType.CLOCKWISE;
                break;
            case "a":
                component.setComponentDirection(component.getComponentDirection().getCounterClockwise());
                component.getConnections().forEach(conn -> conn.setDirection(conn.getDirection().getCounterClockwise()));
                rotationType = RotationType.ANTI_CLOCKWISE;
                break;
            case "o":
                component.setComponentDirection(component.getComponentDirection().getOpposite());
                component.getConnections().forEach(conn -> conn.setDirection(conn.getDirection().getOpposite()));
                rotationType = RotationType.OPPOSITE;
                break;
            default:
                console.printError("Invalid rotation type.");
                return component; // Return unchanged component
        }
        
        console.printMessage("Component rotated locally " + rotationType + ". Ready to place.");
        return component;
    }

    private boolean handleValidateAndFinish() {
        console.printMessage("Validating ship...");
        waitingForServerResponse = true;
        try {
            simpleClient.validateShip(currentPlayer.getId());
            console.printMessage("Validation request sent. Waiting for server response...");
            return true;
        } catch (Exception e) {
            console.printError("Error sending validation request: " + e.getMessage());
            waitingForServerResponse = false;
            return false;
        }
    }

    private Cabin createCabin(boolean isStarting) {
        Cabin cabin = new Cabin(isStarting);
        if(!isStarting){
            // Inizializza i connettori in modo casuale o predefinito
            initializeRandomConnectors(cabin);
        }
        return cabin;
    }

    private void initializeRandomConnectors(Component component) {
        // Inizializza i connettori in modo casuale
        // Questa è una versione semplificata, potresti voler implementare
        // regole più complesse per la generazione dei connettori
        ConnectorType[] possibleConnectors = {
                new SingleConnectorType(),
                new DoubleConnectorType(),
                new UniversalConnectorType(),
                new NullConnectorType()
        };

        ConnectorType north = possibleConnectors[random.nextInt(possibleConnectors.length)];
        ConnectorType east = possibleConnectors[random.nextInt(possibleConnectors.length)];
        ConnectorType south = possibleConnectors[random.nextInt(possibleConnectors.length)];
        ConnectorType west = possibleConnectors[random.nextInt(possibleConnectors.length)];

        component.initializeConnectors(north, east, south, west);
    }

    public void componentPlacedSuccessfully() {
        currentComponent = null;
        waitingForServerResponse = false;
        console.printMessage("Component placed successfully!");
    }

    public Component getCurrentComponent() {
        return currentComponent;
    }

} 
