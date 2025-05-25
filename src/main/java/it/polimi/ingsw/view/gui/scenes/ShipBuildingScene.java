package it.polimi.ingsw.view.gui.scenes;

import it.polimi.ingsw.*;
import it.polimi.ingsw.Components.*;
import it.polimi.ingsw.network.client.GameClient;
import it.polimi.ingsw.network.commands.PlayerDTO;
import it.polimi.ingsw.network.commands.PlayerReadyCommand;
import it.polimi.ingsw.network.rmi.RmiClient;
import it.polimi.ingsw.network.socket.client.SocketClient;
import it.polimi.ingsw.view.gui.GalaxyTruckerFX;
import it.polimi.ingsw.view.gui.components.ComponentRenderer;
import it.polimi.ingsw.view.gui.components.ShipGrid;
import it.polimi.ingsw.view.gui.utils.GameStateManager;
import it.polimi.ingsw.view.tui.components.TuiState;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.*;

/**
 * Scene for building the spaceship
 */
public class ShipBuildingScene {

    private Random random = new Random();

    private final GalaxyTruckerFX application;
    private final GameController controller;
    private final GameStateManager stateManager;
    private Scene scene;

    private String username;
    private String connectionType;
    private String serverAddress;
    private int serverPort;

    private Player currentPlayer;
    private GameClient client;
    private int playerId;

    // UI Components
    private ShipGrid shipGrid;
    private ComponentRenderer currentComponentView;
    private Label statusLabel;
    private Label currentComponentLabel;
    private Label stateLabel;

    // Game state
    private Queue<Component> componentDeck;
    private Component currentComponent;

    public ShipBuildingScene(GalaxyTruckerFX application, GameController controller, GameStateManager stateManager,
                             String username, String connectionType,
                             String serverAddress, int serverPort) {
        this.application = application;
        this.controller = controller;
        this.stateManager = stateManager;
        this.username = username;
        this.connectionType = connectionType;
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;

        // Get current player from state manager
        this.currentPlayer = stateManager.getCurrentPlayer();
        if (this.currentPlayer == null) {
            // Fallback - find player by name or create new
            for (Player player : controller.getPlayers()) {
                if (player.getNickName().equals(username)) {
                    this.currentPlayer = player;
                    break;
                }
            }

            if (this.currentPlayer == null) {
                this.currentPlayer = new Player(0, username, false);
                controller.addPlayer(this.currentPlayer);
            }

            stateManager.setCurrentPlayer(this.currentPlayer);
        }

        initializeComponentDeck();
        createScene();

//        // Debug dello stato iniziale
//        displayShip();
    }

    private void createScene() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // Top: Header
        VBox topBox = new VBox(5);
        Label titleLabel = new Label("Ship Building Phase");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        // State indicator
        stateLabel = new Label("State: " + stateManager.getCurrentState());
        stateLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        stateLabel.setStyle("-fx-text-fill: blue;");

        statusLabel = new Label("Place components to build your ship");
        topBox.getChildren().addAll(titleLabel, stateLabel, statusLabel);
        topBox.setAlignment(Pos.CENTER);
        root.setTop(topBox);

        // Center: Ship grid
        shipGrid = new ShipGrid(controller, currentPlayer);

        // Create a scrollable container for the ship grid
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(shipGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        root.setCenter(scrollPane);

        // Right: Current component and controls
        VBox rightPanel = createRightPanel();
        root.setRight(rightPanel);

        // Bottom: Action buttons
        HBox bottomBox = createBottomPanel();
        root.setBottom(bottomBox);

        scene = new Scene(root);

        // Set this scene as user data for the ship grid to access
        scene.setUserData(this);

        // Start the ship building process
        drawFirstComponent();
    }

    private VBox createRightPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setMinWidth(200);
        panel.setAlignment(Pos.TOP_CENTER);

        // Current component label
        currentComponentLabel = new Label("Current Component");
        currentComponentLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        // Component view
        currentComponentView = new ComponentRenderer();
        currentComponentView.setPrefSize(150, 150);

        // Component rotation controls
        HBox rotationControls = new HBox(10);
        rotationControls.setAlignment(Pos.CENTER);

        Button rotateClockwiseBtn = new Button("↻");
        rotateClockwiseBtn.setOnAction(e -> rotateComponent("c"));

        Button rotateCounterBtn = new Button("↺");
        rotateCounterBtn.setOnAction(e -> rotateComponent("a"));

        Button rotateOppositeBtn = new Button("↔");
        rotateOppositeBtn.setOnAction(e -> rotateComponent("o"));

        rotationControls.getChildren().addAll(rotateCounterBtn, rotateOppositeBtn, rotateClockwiseBtn);

        // Placement instructions
        Label instructionsLabel = new Label("Click on the grid to place the component");
        instructionsLabel.setWrapText(true);
        instructionsLabel.setStyle("-fx-text-fill: blue;");

        // Component info
        VBox componentInfoBox = new VBox(5);
        componentInfoBox.setPadding(new Insets(10));
        componentInfoBox.setStyle("-fx-border-color: gray; -fx-border-radius: 5;");

        Label infoTitle = new Label("Component Info");
        infoTitle.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        Label typeLabel = new Label("Type: -");
        Label connectorsLabel = new Label("Connectors: -");

        componentInfoBox.getChildren().addAll(infoTitle, typeLabel, connectorsLabel);

        panel.getChildren().addAll(
                currentComponentLabel,
                currentComponentView,
                rotationControls,
                instructionsLabel,
                componentInfoBox
        );

        return panel;
    }

    private HBox createBottomPanel() {
        HBox panel = new HBox(10);
        panel.setPadding(new Insets(10));
        panel.setAlignment(Pos.CENTER);

        Button skipBtn = new Button("Skip Component");
        skipBtn.setOnAction(e -> skipComponent());
        skipBtn.setStyle("-fx-background-color: orange; -fx-text-fill: white;");

        Button validateBtn = new Button("Validate Ship");
        validateBtn.setOnAction(e -> validateShip());
        validateBtn.setStyle("-fx-background-color: blue; -fx-text-fill: white;");

        Button readyBtn = new Button("I'm Ready");
        readyBtn.setOnAction(e -> playerReady());
        readyBtn.setStyle("-fx-background-color: green; -fx-text-fill: white;");

        Button backBtn = new Button("Back to Connection");
        backBtn.setOnAction(e -> application.showConnectionScene());
        backBtn.setStyle("-fx-background-color: gray; -fx-text-fill: white;");

        panel.getChildren().addAll(skipBtn, validateBtn, readyBtn, backBtn);
        return panel;
    }

    private void drawFirstComponent() {
        System.out.println("=== STARTING CABIN PLACEMENT ===");

        // Debug dello stato prima del posizionamento
        displayShip();

        // First component is always the cabin
        currentComponent = createCabin(true);

        updateCurrentComponentView();
        currentComponentLabel.setText("Starting Cabin (Auto-placed)");
        statusLabel.setText("Starting cabin will be auto-placed. Click to continue.");

        // Highlight valid positions for this component
        shipGrid.highlightValidPositions(currentComponent);

        System.out.println("=== CABIN PLACEMENT INITIATED ===");
    }

    private void drawNextComponent() {
        if (!componentDeck.isEmpty()) {
            currentComponent = componentDeck.poll();
            updateCurrentComponentView();
            currentComponentLabel.setText("Current: " + currentComponent.getClass().getSimpleName());
            statusLabel.setText("Place the " + currentComponent.getClass().getSimpleName() + " component on your ship.");

            // Highlight valid positions for this component
            shipGrid.highlightValidPositions(currentComponent);
        } else {
            currentComponent = null;
            currentComponentView.setComponent(null);
            currentComponentLabel.setText("No more components");
            statusLabel.setText("No more components available. Validate your ship when ready.");
            shipGrid.clearHighlights();

            System.out.println("No more components in deck");
        }
    }

    private void updateCurrentComponentView() {
        if (currentComponent != null) {
            currentComponentView.setComponent(currentComponent);
        } else {
            currentComponentView.setComponent(null);
        }
    }

    private void rotateComponent(String direction) {
        if (currentComponent == null) {
            statusLabel.setText("No component to rotate");
            return;
        }

        Component originalComponent = currentComponent;

        try {
            switch (direction) {
                case "c":
                    currentComponent = controller.rotateComponentClockwise(currentPlayer, currentComponent);
                    statusLabel.setText("Component rotated clockwise");
                    break;
                case "a":
                    currentComponent = controller.rotateComponentCounterClockwise(currentPlayer, currentComponent);
                    statusLabel.setText("Component rotated counter-clockwise");
                    break;
                case "o":
                    currentComponent = controller.rotateComponentOpposite(currentPlayer, currentComponent);
                    statusLabel.setText("Component rotated opposite");
                    break;
            }

            updateCurrentComponentView();

            // Update highlights for new rotation
            if (currentComponent != null) {
                shipGrid.highlightValidPositions(currentComponent);
            }

        } catch (Exception e) {
            statusLabel.setText("Error rotating component: " + e.getMessage());
            currentComponent = originalComponent; // Revert on error
        }
    }

    private void skipComponent() {
        if (currentComponent == null) {
            statusLabel.setText("No component to skip");
            return;
        }

        statusLabel.setText("Skipped " + currentComponent.getClass().getSimpleName());
        shipGrid.clearHighlights();
        drawNextComponent();
    }

    public void handlePlaceComponent(Position position) {
        if (currentComponent == null) {
            statusLabel.setText("No component to place");
            return;
        }

        try {
            boolean placementSuccess = controller.addComponentForPlayer(currentPlayer, currentComponent, position);

            if (placementSuccess) {
                statusLabel.setText(currentComponent.getClass().getSimpleName() + " placed at (" + position.getX() + "," + position.getY() + ")");
                shipGrid.updateShipView();
                shipGrid.clearHighlights();
                drawNextComponent();
            } else {
                statusLabel.setText("Cannot place " + currentComponent.getClass().getSimpleName() + " at (" + position.getX() + "," + position.getY() + "). Try another position.");
            }
        } catch (Exception e) {
            statusLabel.setText("Error placing component: " + e.getMessage());
        }
    }

    private void validateShip() {
        try {
            boolean isValid = controller.validateShipForPlayer(currentPlayer);

            if (isValid) {
                statusLabel.setText("✓ Ship is valid! You can mark yourself as ready when you're done.");
                statusLabel.setStyle("-fx-text-fill: green;");
            } else {
                statusLabel.setText("✗ Ship is NOT valid. Please check your design.");
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        } catch (Exception e) {
            statusLabel.setText("Error validating ship: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    private void playerReady() {
        // First validate the ship
        try {
            boolean isValid = controller.validateShipForPlayer(currentPlayer);

            if (!isValid) {
                statusLabel.setText("Cannot mark as ready: Ship is not valid");
                statusLabel.setStyle("-fx-text-fill: red;");
                return;
            }

            controller.setPlayerReady(currentPlayer);

            PlayerDTO playerDTO = new PlayerDTO(username);
            PlayerReadyCommand playerReadyCommand = new PlayerReadyCommand(playerDTO);

            // Send ready command to server
            if ("rmi".equals(connectionType)) {
                // RMI implementation
                RmiClient rmiClient = RmiClient.connectTo(serverAddress);
                if (rmiClient != null) {
                    // Implement the RMI player ready call
                    client = rmiClient;
                }
            } else {
                // Socket implementation
                SocketClient socketClient = new SocketClient(serverAddress, serverPort);
                new Thread(socketClient).start();
                socketClient.socketSetPlayerReady(playerReadyCommand);
                client = socketClient;
            }

            statusLabel.setText("✓ You are marked as ready. Waiting for game to start...");
            statusLabel.setStyle("-fx-text-fill: green;");

            // Check if we can transition to RUNNING state
            stateManager.checkForStateTransition();

            // If all players are ready, transition to game
            if (stateManager.areAllPlayersReady()) {
                Platform.runLater(() -> {
                    stateManager.setState(TuiState.RUNNING);
                    application.showGameScene();
                });
            } else {
                // Show waiting message with count
                int readyCount = stateManager.getReadyPlayerCount();
                int totalCount = stateManager.getTotalPlayerCount();
                statusLabel.setText(String.format("✓ Ready! Waiting for other players (%d/%d ready)...", readyCount, totalCount));
            }

        } catch (Exception e) {
            statusLabel.setText("Error setting player ready: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    // Component initialization methods - we'll reuse logic from ShipBuildingHandler.java
    private void initializeComponentDeck() {
        //Potrebbe essere necessario bilanciarla
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



    public Scene getScene() {
        return scene;
    }


    private void displayShip() {
        System.out.println("\n--- Your Ship ---");
        ShipBuilder ship = currentPlayer.getShip();
        if (ship == null) {
            System.out.println("No ship found for player: " + currentPlayer.getNickName());
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
        System.out.println("      " + getCoordinateHeader(width));

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
            System.out.println(rowStr.toString());
        }
        System.out.println("------------------------------------");
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

    public String getCurrentComponent() {
        return currentComponent.getClass().getSimpleName();
    }
}