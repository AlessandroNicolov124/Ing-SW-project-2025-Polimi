package it.polimi.ingsw.view.gui.scenes;

import it.polimi.ingsw.GameController;
import it.polimi.ingsw.Player;
import it.polimi.ingsw.view.gui.GalaxyTruckerFX;
import it.polimi.ingsw.view.gui.utils.GameStateManager;
import it.polimi.ingsw.view.gui.utils.NetworkUtils;
import it.polimi.ingsw.view.tui.components.TuiState;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Scene for setting up connection parameters and username
 * Now integrated with GameStateManager and uses TuiState
 */
public class ConnectionScene {

    private final GalaxyTruckerFX application;
    private final GameController controller;
    private final GameStateManager stateManager;
    private Scene scene;

    // Form fields
    private TextField usernameField;
    private ToggleGroup connectionTypeGroup;
    private TextField serverAddressField;
    private TextField serverPortField;
    private Button connectButton;
    private Label statusLabel;
    private Label currentStateLabel;

    // Connection state
    private String lastUsername = "";
    private String lastConnectionType = "rmi";
    private String lastServerAddress = "localhost";
    private int lastServerPort = 1099;

    public ConnectionScene(GalaxyTruckerFX application, GameController controller, GameStateManager stateManager) {
        this.application = application;
        this.controller = controller;
        this.stateManager = stateManager;
        createScene();
        updateUIForCurrentState();
    }

    private void createScene() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(20));

        // Title
        Label titleLabel = new Label("Connection Setup");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        // Current state indicator
        currentStateLabel = new Label();
        currentStateLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        currentStateLabel.setStyle("-fx-text-fill: blue;");

        // Form container
        GridPane formGrid = createFormGrid();

        // Controls
        HBox controlsBox = createControlsBox();

        // Status label for feedback
        statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: red;");
        statusLabel.setWrapText(true);

        root.getChildren().addAll(titleLabel, currentStateLabel, formGrid, controlsBox, statusLabel);

        scene = new Scene(root);
    }

    private GridPane createFormGrid() {
        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(15);
        formGrid.setAlignment(Pos.CENTER);

        // Username
        Label usernameLabel = new Label("Username:");
        usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        usernameField.setPrefWidth(200);
        formGrid.add(usernameLabel, 0, 0);
        formGrid.add(usernameField, 1, 0);

        // Connection type
        Label connectionTypeLabel = new Label("Connection Type:");
        connectionTypeGroup = new ToggleGroup();

        RadioButton rmiRadio = new RadioButton("RMI");
        rmiRadio.setToggleGroup(connectionTypeGroup);
        rmiRadio.setSelected(true);

        RadioButton socketRadio = new RadioButton("Socket");
        socketRadio.setToggleGroup(connectionTypeGroup);

        HBox connectionTypeBox = new HBox(10);
        connectionTypeBox.getChildren().addAll(rmiRadio, socketRadio);

        formGrid.add(connectionTypeLabel, 0, 1);
        formGrid.add(connectionTypeBox, 1, 1);

        // Server address
        Label serverAddressLabel = new Label("Server Address:");
        serverAddressField = new TextField();
        serverAddressField.setPromptText("localhost");
        serverAddressField.setPrefWidth(200);
        formGrid.add(serverAddressLabel, 0, 2);
        formGrid.add(serverAddressField, 1, 2);

        // Server port
        Label serverPortLabel = new Label("Server Port:");
        serverPortField = new TextField();
        serverPortField.setPrefWidth(200);
        formGrid.add(serverPortLabel, 0, 3);
        formGrid.add(serverPortField, 1, 3);

        // Update port based on connection type
        connectionTypeGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            RadioButton selected = (RadioButton) newVal;
            if ("RMI".equals(selected.getText())) {
                serverPortField.setPromptText("1099");
            } else {
                serverPortField.setPromptText("7777");
            }
        });
        rmiRadio.setSelected(true); // Trigger the listener

        return formGrid;
    }

    private HBox createControlsBox() {
        HBox controlsBox = new HBox(15);
        controlsBox.setAlignment(Pos.CENTER);

        // Connect button
        connectButton = new Button("Connect");
        connectButton.setPrefWidth(120);
        connectButton.setStyle("-fx-background-color: green; -fx-text-fill: white; -fx-font-weight: bold;");
        connectButton.setOnAction(e -> handleConnect());

        // Back button
        Button backButton = new Button("Back");
        backButton.setPrefWidth(80);
        backButton.setStyle("-fx-background-color: gray; -fx-text-fill: white;");
        backButton.setOnAction(e -> goBack());

        // Skip to ship building (for testing)
        Button skipButton = new Button("Skip to Ship Building");
        skipButton.setPrefWidth(150);
        skipButton.setStyle("-fx-background-color: orange; -fx-text-fill: white;");
        skipButton.setOnAction(e -> skipToShipBuilding());

        controlsBox.getChildren().addAll(backButton, connectButton, skipButton);
        return controlsBox;
    }

    private void updateUIForCurrentState() {
        TuiState currentState = stateManager.getCurrentState();

        switch (currentState) {
            case ASK_USERNAME:
                currentStateLabel.setText("Step 1: Enter your username");
                usernameField.setDisable(false);
                connectionTypeGroup.getToggles().forEach(toggle ->
                        ((RadioButton)toggle).setDisable(true));
                serverAddressField.setDisable(true);
                serverPortField.setDisable(true);
                connectButton.setText("Next");
                break;

            case ASK_NET:
                currentStateLabel.setText("Step 2: Choose connection type");
                usernameField.setDisable(true);
                connectionTypeGroup.getToggles().forEach(toggle ->
                        ((RadioButton)toggle).setDisable(false));
                serverAddressField.setDisable(true);
                serverPortField.setDisable(true);
                connectButton.setText("Next");
                break;

            case ASK_ADDRESS:
                currentStateLabel.setText("Step 3: Enter server details");
                usernameField.setDisable(true);
                connectionTypeGroup.getToggles().forEach(toggle ->
                        ((RadioButton)toggle).setDisable(true));
                serverAddressField.setDisable(false);
                serverPortField.setDisable(false);
                connectButton.setText("Connect");
                break;

            case CONNECTING:
                currentStateLabel.setText("Connecting to server...");
                disableAllFields();
                connectButton.setText("Connecting...");
                connectButton.setDisable(true);
                break;

            default:
                currentStateLabel.setText("Connection Setup");
                enableAllFields();
                connectButton.setText("Connect");
                connectButton.setDisable(false);
        }
    }

    private void disableAllFields() {
        usernameField.setDisable(true);
        connectionTypeGroup.getToggles().forEach(toggle ->
                ((RadioButton)toggle).setDisable(true));
        serverAddressField.setDisable(true);
        serverPortField.setDisable(true);
    }

    private void enableAllFields() {
        usernameField.setDisable(false);
        connectionTypeGroup.getToggles().forEach(toggle ->
                ((RadioButton)toggle).setDisable(false));
        serverAddressField.setDisable(false);
        serverPortField.setDisable(false);
    }

    private void handleConnect() {
        TuiState currentState = stateManager.getCurrentState();

        switch (currentState) {
            case ASK_USERNAME:
                handleUsernameStep();
                break;
            case ASK_NET:
                handleNetworkTypeStep();
                break;
            case ASK_ADDRESS:
                handleAddressStep();
                break;
            default:
                // Default behavior - try full connection
                handleFullConnection();
        }
    }

    private void handleUsernameStep() {
        String username = usernameField.getText().trim();

        if (username.isEmpty()) {
            statusLabel.setText("Username cannot be empty");
            return;
        }

        if (username.length() < 2) {
            statusLabel.setText("Username must be at least 2 characters long");
            return;
        }

        lastUsername = username;
        statusLabel.setText("");
        stateManager.setState(TuiState.ASK_NET);
        updateUIForCurrentState();
    }

    private void handleNetworkTypeStep() {
        RadioButton selectedRadio = (RadioButton) connectionTypeGroup.getSelectedToggle();
        if (selectedRadio == null) {
            statusLabel.setText("Please select a connection type");
            return;
        }

        lastConnectionType = selectedRadio.getText().toLowerCase();
        statusLabel.setText("");
        stateManager.setState(TuiState.ASK_ADDRESS);
        updateUIForCurrentState();
    }

    private void handleAddressStep() {
        String serverAddress = serverAddressField.getText().trim();
        if (serverAddress.isEmpty()) {
            serverAddress = "localhost";
        }

        String portText = serverPortField.getText().trim();
        int serverPort;
        if (portText.isEmpty()) {
            serverPort = lastConnectionType.equals("rmi") ? 1099 : 7777;
        } else {
            try {
                serverPort = Integer.parseInt(portText);
                if (serverPort <= 0 || serverPort > 65535) {
                    statusLabel.setText("Port must be between 1 and 65535");
                    return;
                }
            } catch (NumberFormatException e) {
                statusLabel.setText("Invalid port number");
                return;
            }
        }

        lastServerAddress = serverAddress;
        lastServerPort = serverPort;

        // Now proceed with actual connection
        performConnection();
    }

    private void handleFullConnection() {
        // Validate all fields at once
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            statusLabel.setText("Username cannot be empty");
            return;
        }

        RadioButton selectedRadio = (RadioButton) connectionTypeGroup.getSelectedToggle();
        if (selectedRadio == null) {
            statusLabel.setText("Please select a connection type");
            return;
        }
        String connectionType = selectedRadio.getText().toLowerCase();

        String serverAddress = serverAddressField.getText().trim();
        if (serverAddress.isEmpty()) {
            serverAddress = "localhost";
        }

        String portText = serverPortField.getText().trim();
        int serverPort;
        if (portText.isEmpty()) {
            serverPort = connectionType.equals("rmi") ? 1099 : 7777;
        } else {
            try {
                serverPort = Integer.parseInt(portText);
                if (serverPort <= 0 || serverPort > 65535) {
                    statusLabel.setText("Port must be between 1 and 65535");
                    return;
                }
            } catch (NumberFormatException e) {
                statusLabel.setText("Invalid port number");
                return;
            }
        }

        // Store values and connect
        lastUsername = username;
        lastConnectionType = connectionType;
        lastServerAddress = serverAddress;
        lastServerPort = serverPort;

        performConnection();
    }

    private void performConnection() {
        stateManager.setState(TuiState.CONNECTING);
        updateUIForCurrentState();
        statusLabel.setText("Connecting to " + lastServerAddress + ":" + lastServerPort + " using " + lastConnectionType.toUpperCase() + "...");

        // Create player for local controller
        Player currentPlayer = new Player(0, lastUsername, false);
        controller.addPlayer(currentPlayer);
        stateManager.setCurrentPlayer(currentPlayer);

        // Use NetworkUtils for async connection
        NetworkUtils.connectAsync(lastConnectionType, lastServerAddress, lastServerPort, lastUsername, result -> {
            if (result.isSuccess()) {
                // Connection successful
                statusLabel.setText("Connected successfully!");
                statusLabel.setStyle("-fx-text-fill: green;");

                // Move to waiting for players state
                stateManager.setState(TuiState.WAIT_PLAYER_READY);
                application.showWaitingForPlayersScene();

            } else {
                // Connection failed
                statusLabel.setText("Connection failed: " + result.getErrorMessage());
                statusLabel.setStyle("-fx-text-fill: red;");

                // Reset UI to allow retry
                stateManager.setState(TuiState.ASK_ADDRESS);
                updateUIForCurrentState();
            }
        });
    }

    private void goBack() {
        TuiState currentState = stateManager.getCurrentState();

        switch (currentState) {
            case ASK_NET:
                stateManager.setState(TuiState.ASK_USERNAME);
                break;
            case ASK_ADDRESS:
                stateManager.setState(TuiState.ASK_NET);
                break;
            case CONNECTING:
                stateManager.setState(TuiState.ASK_ADDRESS);
                break;
            default:
                // Go back to intro
                application.showIntroScene();
                return;
        }

        updateUIForCurrentState();
        statusLabel.setText("");
    }

    private void skipToShipBuilding() {
        // For testing purposes - create a local player and skip to ship building
        if (lastUsername.isEmpty()) {
            lastUsername = "TestPlayer";
        }

        Player testPlayer = new Player(0, lastUsername, false);
        controller.addPlayer(testPlayer);
        stateManager.setCurrentPlayer(testPlayer);

        stateManager.setState(TuiState.SHIP_BUILDING);
        application.showShipBuildingScene(lastUsername, "socket", "localhost", 7777);
    }

    public Scene getScene() {
        return scene;
    }
}