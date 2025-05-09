package it.polimi.ingsw.view.tui.components;

import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.GameController;
import it.polimi.ingsw.Player;
import it.polimi.ingsw.Position;
import it.polimi.ingsw.ShipBuilder;
import it.polimi.ingsw.Components.*; // For specific component types
import it.polimi.ingsw.Direction; // For rotation

import java.sql.Connection;
import java.util.*;

public class ShipBuildingHandler {

    private final ConsoleManager console;
    private final GameController controller;
    private final Player currentPlayer; // Assuming we know the current player

    private final Random random = new Random();
    private Queue<Component> componentDeck; // La pila di componenti pescati casualmente


    public ShipBuildingHandler(ConsoleManager console, GameController controller, Player currentPlayer) {
        this.console = console;
        this.controller = controller;
        this.currentPlayer = currentPlayer;
        this.componentDeck = new LinkedList<>();
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
                        // Se piazzato con successo, il componente diventa null per pescarne uno nuovo
                        currentComponent = null;
                    } else {
                        console.printError("No component available to place!");
                    }
                    break;
                case "r": // Remove component
                    handleRemoveComponent();
                    break;
                case "t": // Rotate component (t for turn)
                    //handleRotateComponent();
                    if (currentComponent != null) {
                        handleRotateComponent(currentComponent);
                    } else {
                        console.printError("No component to rotate!");
                    }
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


    private void printShipBuildingMenu(boolean hasComponent) {
        console.printMessage("\nShip Building Options:");
        if (hasComponent) {
            console.printMessage(" (p) Place Current Component");
            console.printMessage(" (t) Rotate Current Component");
            console.printMessage(" (s) Skip Current Component");
        } else {
            console.printMessage(" (p) [No component available]");
            console.printMessage(" (t) [No component available]");
            console.printMessage(" (s) [No component available]");
        }
        console.printMessage(" (r) Remove Component from Ship");
        console.printMessage(" (v) Validate and Finish Ship");
        console.printMessage(" (q) Quit Ship Building");
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

        // 4. Call controller
        boolean placementSuccess = controller.addComponentForPlayer(currentPlayer, component, pos);

        if (placementSuccess) {
            console.printMessage(component.getClass().getSimpleName() + " successfully placed at (" + pos.getX() + "," + pos.getY() + ").");
        } else {
            console.printError("Failed to place " + component.getClass().getSimpleName() + " at (" + pos.getX() + "," + pos.getY() + "). Check position and connections.");
            handlePlaceComponent(component);
        }
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
        Component compToRemove = currentPlayer.getShip().getComponentAt(posToRemove);

        if (compToRemove != null) {
            boolean res = controller.removeComponentForPlayer(currentPlayer, compToRemove);
            console.printMessage("Attempted to remove component at (" + x + "," + y + "): result: " + res);
        } else {
            console.printError("No component found at (" + x + "," + y + ").");
        }
    }

    private void handleRotateComponent(Component component) {
        console.printMessage("--- Rotate Component ---");
        String rotationType = console.askQuestion("Rotate (c)lockwise, (a)nti-clockwise, or (o)pposite?").toLowerCase();

        // Salva lo stato attuale del componente per verificare le modifiche
        // Nota: potrebbe essere necessario implementare una funzione di copia profonda

        switch (rotationType) {
            case "c":
                controller.rotateComponentClockwiseForPlayer(currentPlayer, component);
                break;
            case "a":
                controller.rotateComponentCounterClockwiseForPlayer(currentPlayer, component);
                break;
            case "o":
                controller.rotateComponentOppositeForPlayer(currentPlayer, component);
                break;
            default:
                console.printError("Invalid rotation type.");
                return;
        }

        console.printMessage("Component rotated " +
                (rotationType.equals("c") ? "clockwise" :
                        rotationType.equals("a") ? "anti-clockwise" : "opposite"));
    }

//    private void handleRotateComponent() {
//        console.printMessage("--- Rotate Component ---");
//        int x = -1, y = -1;
//        try {
//            x = Integer.parseInt(console.askQuestion("Enter X coordinate of component to rotate:"));
//            y = Integer.parseInt(console.askQuestion("Enter Y coordinate of component to rotate:"));
//        } catch (NumberFormatException e) {
//            console.printError("Invalid coordinates.");
//            return;
//        }
//        Position posToRotate = new Position(x,y);
//        Component compToRotate = currentPlayer.getShip().getComponentAt(posToRotate);
//
//        if (compToRotate == null) {
//            console.printError("No component found at (" + x + "," + y + ").");
//            return;
//        }
//
//        String rotationType = console.askQuestion("Rotate (c)lockwise, (a)nti-clockwise, or (o)pposite?").toLowerCase();
//        switch (rotationType) {
//            case "c":
//                controller.rotateComponentClockwiseForPlayer(currentPlayer, compToRotate);
//                break;
//            case "a":
//                controller.rotateComponentCounterClockwiseForPlayer(currentPlayer, compToRotate);
//                break;
//            case "o":
//                controller.rotateComponentOppositeForPlayer(currentPlayer, compToRotate);
//                break;
//            default:
//                console.printError("Invalid rotation type.");
//                return;
//        }
//        console.printMessage("Attempted to rotate component at (" + x + "," + y + ").");
//    }

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
        return engine;
    }

    private DoubleEngine createDoubleEngine() {
        DoubleEngine engine = new DoubleEngine();
        initializeRandomConnectors(engine);
        return engine;
    }

    private SingleCannon createSingleCannon() {
        //il cannone punta con il costruttore sempre verso nord
        SingleCannon cannon = new SingleCannon();
        initializeRandomConnectors(cannon);
        while(!cannon.validateFiringDirection(cannon.getFiringDirection())){
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
        while(!cannon.validateFiringDirection(cannon.getFiringDirection())){
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