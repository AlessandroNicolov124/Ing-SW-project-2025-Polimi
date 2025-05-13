package it.polimi.ingsw.view.tui.components;

import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.GameController;
import it.polimi.ingsw.Player;
import it.polimi.ingsw.Position;
import it.polimi.ingsw.ShipBuilder;
import it.polimi.ingsw.Components.*; // For specific component types
import it.polimi.ingsw.Direction; // For rotation
import it.polimi.ingsw.network.socket.client.SocketClient; // Added import
import it.polimi.ingsw.network.socket.messages.RotationType; // <<< ADD THIS IMPORT

import java.sql.Connection;
import java.util.*;
import java.io.IOException;

public class ShipBuildingHandler {

    private final ConsoleManager console;
    // private final GameController controller; // Keep for local validation/display logic if needed, or remove
    private final Player currentPlayer; // Assuming we know the current player
    private final SocketClient socketClient; // Added socket client

    private final Random random = new Random();
    private Queue<Component> componentDeck; // La pila di componenti pescati casualmente


    public ShipBuildingHandler(ConsoleManager console, SocketClient socketClient, Player currentPlayer) {
        this.console = console;
        this.socketClient = socketClient;
        this.currentPlayer = currentPlayer;
        this.componentDeck = new LinkedList<>();
        // this.controller = controller; // Remove if no longer needed
    }

    // Method to be called to redraw the ship display
    public void refreshDisplay() {
        console.printMessage("[DEBUG] Refreshing ship display due to network update..."); // Optional debug message
        displayShip();
    }

    public void manageShipBuilding() {
        console.printMessage("=== Ship Building Phase ===");
        console.printMessage("Generating random component deck...");

        // Inizializza il mazzo di componenti casuali
        initializeComponentDeck();

        boolean buildingDone = false;

        //il primo elemento è sempre la cabina di pilotaggio
        Component currentComponent = createCabin(true);
        displayComponent(currentComponent);
        handlePlaceComponent(currentComponent);
        currentComponent = null;

        while (!buildingDone) {
            displayShip();

            // Se non abbiamo un componente corrente, peschiamone uno
            if (currentComponent == null && !componentDeck.isEmpty()) {
                currentComponent = componentDeck.poll();
                console.printMessage("\nYou drew: " + currentComponent.getClass().getSimpleName());
            } else if (currentComponent == null && componentDeck.isEmpty()) {
                console.printMessage("\nNo more components available!");
            }

            if (currentComponent != null) {
                console.printMessage("Current component: " + currentComponent.getClass().getSimpleName());
                displayComponent(currentComponent);
            }

            printShipBuildingMenu(currentComponent != null);
            String choice = console.readCommand().trim().toLowerCase();

            switch (choice) {
                case "p": // Place component
                    if (currentComponent != null) {
                        handlePlaceComponent(currentComponent);
                        currentComponent = null; // Component is now placed (or request sent)
                    } else {
                        console.printError("No component available to place!");
                    }
                    break;
                case "r": // Remove component from ship
                    handleRemoveComponent(); // This now sends a network request
                    break;
                case "tc": // Rotate CURRENT (held) component
                    if (currentComponent != null) {
                        currentComponent = handleRotateHeldComponent(currentComponent); // Client-side rotation
                    } else {
                        console.printError("No current component to rotate!");
                    }
                    break;
                case "ts": // Rotate component ON SHIP
                    handleRotateComponentOnShip(); // This now sends a network request
                    break;
                case "s": // Skip current component
                    if (currentComponent != null) {
                        console.printMessage("Skipping " + currentComponent.getClass().getName());
                        currentComponent = null;
                    } else {
                        console.printError("No component to skip!");
                    }
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
                case "debugskip": // DEBUG: Force skip ship building validation
                    console.printMessage("[DEBUG] Forcing skip of ship validation. Proceeding to next phase.");
                    buildingDone = true; // Exit the loop, bypassing validation
                    break;
                default:
                    console.printError("Invalid choice. Please try again.");
                    break;
            }
        }
    }

    private void displayComponent(Component currentComponent) {
        // Create a grid
        String[][] grid = new String[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                grid[i][j] = "[  ]"; // Empty slot
            }
        }
        grid[1][1] = "[" + getComponentSymbol(currentComponent) + "]";
        grid[1][0] = "[" + getConnectionSymbol(currentComponent.getConnectorByDirection(Direction.NORTH))  + "]";
        grid[1][2] = "[" + getConnectionSymbol(currentComponent.getConnectorByDirection(Direction.SOUTH))  + "]";
        grid[2][1] = "[" + getConnectionSymbol(currentComponent.getConnectorByDirection(Direction.EAST))  + "]";
        grid[0][1] = "[" + getConnectionSymbol(currentComponent.getConnectorByDirection(Direction.WEST))  + "]";


        // Print grid
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
        return switch (connector.getConnectorType()){
            case SingleConnectorType s -> "SC";
            case DoubleConnectorType s -> "DC";
            case UniversalConnectorType s -> "UC";
            case NullConnectorType s -> "NC";
            default -> "??";
        };
    }

    private void initializeComponentDeck() {
        // Questa è una semplice implementazione. Potrebbe essere necessario bilanciarla
        // in base alle regole del gioco e alla rarità dei componenti.
        List<Component> allComponents = new ArrayList<>();

        // Aggiungi un mix di componenti (potrebbe essere configurato in base al livello di difficoltà)
        for (int i = 0; i < 4; i++) allComponents.add(createCabin(false));
        for (int i = 0; i < 8; i++) allComponents.add(createSingleEngine());
        for (int i = 0; i < 4; i++) allComponents.add(createDoubleEngine());
        for (int i = 0; i < 8; i++) allComponents.add(createSingleCannon());
        for (int i = 0; i < 4; i++) allComponents.add(createDoubleCannon());
        for (int i = 0; i < 6; i++) allComponents.add(createNormalDoubleCargoHolds());
        for (int i = 0; i < 6; i++) allComponents.add(createNormalTripleCargoHolds());
        for (int i = 0; i < 6; i++) allComponents.add(createSpecialDoubleCargoHolds());
        for (int i = 0; i < 6; i++) allComponents.add(createSpecialTripleCargoHolds());
        for (int i = 0; i < 3; i++) allComponents.add(createBatteryCompartment());
        for (int i = 0; i < 3; i++) allComponents.add(createShieldGenerator());
        for (int i = 0; i < 3; i++) allComponents.add(createBrownLifeSupportSystem());
        for (int i = 0; i < 3; i++) allComponents.add(createPurpleLifeSupportSystem());
        for (int i = 0; i < 5; i++) allComponents.add(createStructuralModule());

        // Mescola la lista e converti in coda
        Collections.shuffle(allComponents);
        componentDeck = new LinkedList<>(allComponents);
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
                grid[i][j] = "[  ]"; // Empty slot
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
        console.printMessage("  X:  0   1   2   3   4   5   6   7   8   9"); // Assuming max width 10 for now
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
        return switch (component) {
            case Cabin c -> "CA";
            case SingleCannon sc -> "C1"; // S1 for single cannon
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
//        if (component instanceof Cabin) return "C";
//        if (component instanceof SingleEngine) return "E";
//        if (component instanceof DoubleEngine) return "E"; // Could be E2
//        if (component instanceof SingleCannon) return "S"; // S for single cannon
//        if (component instanceof DoubleCannon) return "D"; // D for double cannon
//        if (component instanceof CargoHolds) return "H"; // H for holds
//        if (component instanceof BatteryCompartment) return "B";
//        if (component instanceof ShieldGenerator) return "G";
//        if (component instanceof LifeSupportSystem) return "L";
//        if (component instanceof StructuralModule) return "X"; // For structural
//        return "?"; // Unknown
    }


    private void printShipBuildingMenu(boolean hasCurrentComponent) {
        console.printMessage("\n--- Ship Building Actions ---");
        if (hasCurrentComponent) {
            console.printMessage("P  - Place current component");
            console.printMessage("TC - Rotate current component (clockwise)");
            console.printMessage("S  - Skip current component (draw next)");
        } else {
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
        console.printMessage("Enter your choice: ");
    }

    private void handlePlaceComponent(Component component) {
        console.printMessage("--- Place Component ---");
        // 1. Ask for component type
        //String typeStr = console.askQuestion("Enter component type (e.g., Cabin, SingleEngine, DoubleCannon):");

        // 2. Ask for position
        int x = -1, y = -1;
        Position pos;

        // Se è una cabina iniziale, usa la posizione iniziale
        if (component instanceof Cabin cabin && cabin.isStartingCabin()) {
            pos = currentPlayer.getShip().getStartingPosition();
        }else{
            try {
                x = Integer.parseInt(console.askQuestion("Enter X coordinate:"));
                y = Integer.parseInt(console.askQuestion("Enter Y coordinate:"));
                pos = new Position(x, y);
            } catch (NumberFormatException e) {
                console.printError("Invalid coordinates.");
                return;
            }
        }

        // 4. Call network client to send message
        try {
             socketClient.placeComponent(component, pos);
             console.printMessage("Placement request sent for " + component.getClass().getSimpleName() + " at (" + pos.getX() + "," + pos.getY() + "). Waiting for server confirmation...");
             // Note: The actual success/failure message will come asynchronously
             // via the ComponentPlacedMessage handled by SocketClient's run loop.
             // The TUI should be updated based on that message.
        } catch (IOException | IllegalStateException e) {
            console.printError("Error sending placement request: " + e.getMessage());
            // Optionally, allow the user to retry or handle the error
            // handlePlaceComponent(component); // Example: Retry automatically (use with caution)
        }

        // boolean placementSuccess = controller.addComponentForPlayer(currentPlayer, component, pos);
        // if (placementSuccess) {
        //    console.printMessage(component.getClass().getSimpleName() + " successfully placed at (" + pos.getX() + "," + pos.getY() + ").");
        // } else {
        //    console.printError("Failed to place " + component.getClass().getSimpleName() + " at (" + pos.getX() + "," + pos.getY() + "). Check position and connections.");
        //    handlePlaceComponent(component); // Retry placing the same component
        // }
    }

//    private Component createAndInitializeComponent(String typeStr) {
//        // This is a simplified factory. A more robust solution would use a proper factory pattern
//        // or reflection, and handle connector initialization more dynamically.
//        // For now, using default/sensible connector setups.
//        Component component = null;
//        switch (typeStr.toLowerCase()) {
//            case "cabin":
//                boolean isStartingCabin = currentPlayer.getShip().getComponents().isEmpty(); // First component is starting cabin
//                component = new Cabin(isStartingCabin);
//                // Cabins usually have universal connectors or manage them internally
//                break;
//            case "singleengine":
//                Engine singleEngine = new SingleEngine();
//                // Example: Default for a single engine might be Double on NORTH, Null elsewhere
//                singleEngine.initializeConnectors(new DoubleConnectorType(), new NullConnectorType(), new NullConnectorType(), new NullConnectorType());
//                component = singleEngine;
//                break;
//            case "doubleengine":
//                Engine doubleEngine = new DoubleEngine();
//                // Example: Default for a double engine
//                doubleEngine.initializeConnectors(new DoubleConnectorType(), new NullConnectorType(), new NullConnectorType(), new NullConnectorType());
//                component = doubleEngine;
//                break;
//            case "singlecannon":
//                SingleCannon singleCannon = new SingleCannon(Direction.NORTH); // Default firing NORTH
//                // Cannons might need specific connector setups based on design
//                singleCannon.initializeConnectors(new SingleConnectorType(), new NullConnectorType(), new NullConnectorType(), new NullConnectorType());
//                component = singleCannon;
//                break;
//            case "doublecannon":
//                DoubleCannon doubleCannon = new DoubleCannon(Direction.NORTH); // Default firing NORTH
//                doubleCannon.initializeConnectors(new DoubleConnectorType(), new NullConnectorType(), new NullConnectorType(), new NullConnectorType());
//                component = doubleCannon;
//                break;
//            // Add more cases for other components (CargoHolds, BatteryCompartment, etc.)
//            // Each will require specific initialization, especially for connectors.
//            default:
//                console.printError("Unknown component type: " + typeStr);
//                return null;
//        }
//        return component;
//    }

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
            socketClient.removeComponent(posToRemove);
            console.printMessage("Remove request sent for position (" + x + "," + y + "). Waiting for server confirmation...");
            // Success/failure message handled asynchronously by ComponentRemovedMessage
        } catch (IOException | IllegalStateException e) {
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
            socketClient.rotateComponent(posToRotate, rotationType);
            console.printMessage("Rotation request sent for position (" + x + "," + y + "). Waiting for server confirmation...");
        } catch (IOException | IllegalStateException e) {
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
        console.printMessage("Sending validation request to server...");
        try {
            socketClient.validateShip();
            // We sent the request. Now we need to wait for the ShipValidationResultMessage.
            // The TUI loop cannot simply proceed here.
            // For now, we just return false and let the message handler update the state later.
            console.printMessage("Validation request sent. Waiting for server response...");
            // The actual result (isValid) is not available synchronously here.
            // The TUI state needs to be updated when ShipValidationResultMessage arrives.
            return false; // Indicate building is NOT done yet, waiting for server.
        } catch (IOException | IllegalStateException e) {
            console.printError("Error sending validation request: " + e.getMessage());
            return false; // Building not done.
        }

        // Old synchronous logic:
        // boolean isValid = controller.validateShipForPlayer(currentPlayer);
        // if (isValid) {
        //    console.printMessage("Ship is valid! Construction complete.");
        //    return true;
        // } else {
        //    console.printError("Ship is NOT valid. Please check your design.");
        //    return false;
        // }
    }

    // Factory methods per creare i diversi tipi di componenti
    private Cabin createCabin(boolean isStarting) {
        Cabin cabin = new Cabin(isStarting);
        if(!isStarting){
            // Inizializza i connettori in modo casuale o predefinito
            initializeRandomConnectors(cabin);
        }
        return cabin;
    }

    private SingleEngine createSingleEngine() {
        SingleEngine engine = new SingleEngine();
        initializeRandomConnectors(engine);
        while(!engine.validateEngineDirection(engine.getComponentDirection())){
            engine.deleteConnection();
            initializeRandomConnectors(engine);
        }
        return engine;
    }

    private DoubleEngine createDoubleEngine() {
        DoubleEngine engine = new DoubleEngine();
        initializeRandomConnectors(engine);
        while(!engine.validateEngineDirection(engine.getComponentDirection())){
            engine.deleteConnection();
            initializeRandomConnectors(engine);
        }
        return engine;
    }

    private SingleCannon createSingleCannon() {
        //il cannone punta con il costruttore sempre verso nord
        SingleCannon cannon = new SingleCannon();
        initializeRandomConnectors(cannon);
        while(!cannon.validateFiringDirection(cannon.getComponentDirection())){
            cannon.deleteConnection();
            initializeRandomConnectors(cannon);
        }
        return cannon;
    }

    private DoubleCannon createDoubleCannon() {
//        Direction direction = getRandomDirection();
//        DoubleCannon cannon = new DoubleCannon(direction);
//        initializeRandomConnectors(cannon);
//        return cannon;
        //il cannone punta con il costruttore sempre verso nord
        DoubleCannon cannon = new DoubleCannon();
        initializeRandomConnectors(cannon);
        while(!cannon.validateFiringDirection(cannon.getComponentDirection())){
            cannon.deleteConnection();
            initializeRandomConnectors(cannon);
        }
        return cannon;
    }

    private CargoHolds createNormalDoubleCargoHolds() {
        CargoHolds cargoHold = new NormalCargoHolds(CargoHoldsSize.DOUBLE);
        initializeRandomConnectors(cargoHold);
        return cargoHold;
    }
    private CargoHolds createNormalTripleCargoHolds() {
        CargoHolds cargoHold = new NormalCargoHolds(CargoHoldsSize.TRIPLE);
        initializeRandomConnectors(cargoHold);
        return cargoHold;
    }

    private CargoHolds createSpecialDoubleCargoHolds() {
        CargoHolds cargoHold = new SpecialCargoHolds(CargoHoldsSize.DOUBLE);
        initializeRandomConnectors(cargoHold);
        return cargoHold;
    }
    private CargoHolds createSpecialTripleCargoHolds() {
        CargoHolds cargoHold = new SpecialCargoHolds(CargoHoldsSize.TRIPLE);
        initializeRandomConnectors(cargoHold);
        return cargoHold;
    }

    private BatteryCompartment createBatteryCompartment() {
        BatteryCompartment battery = new BatteryCompartment();
        initializeRandomConnectors(battery);
        return battery;
    }

    private ShieldGenerator createShieldGenerator() {
        ShieldGenerator shield = new ShieldGenerator();
        initializeRandomConnectors(shield);
        return shield;
    }

    private LifeSupportSystem createPurpleLifeSupportSystem() {
        LifeSupportSystem lifeSupportSystem = new LifeSupportSystem(LifeSupportSystemType.PURPLE);
        initializeRandomConnectors(lifeSupportSystem);
        return lifeSupportSystem;
    }
    private LifeSupportSystem createBrownLifeSupportSystem() {
        LifeSupportSystem lifeSupportSystem = new LifeSupportSystem(LifeSupportSystemType.BROWN);
        initializeRandomConnectors(lifeSupportSystem);
        return lifeSupportSystem;
    }

    private StructuralModule createStructuralModule() {
        StructuralModule structuralModule = new StructuralModule();
        initializeRandomConnectors(structuralModule);
        return structuralModule;
    }

    private Direction getRandomDirection() {
        Direction[] directions = Direction.values();
        return directions[random.nextInt(directions.length)];
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
} 