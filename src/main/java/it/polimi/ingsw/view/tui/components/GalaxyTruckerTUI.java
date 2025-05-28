package it.polimi.ingsw.view.tui.components;

import java.io.IOException;
import java.util.List;
import it.polimi.ingsw.Player;
import it.polimi.ingsw.Position;
import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.network.client.GeneralClientIf;
import it.polimi.ingsw.network.client.listeners.GameListener;
import it.polimi.ingsw.network.rmi.RmiClient;
import it.polimi.ingsw.network.socket.client.SocketClient;
import it.polimi.ingsw.network.socket.messages.RotationType;



public class GalaxyTruckerTUI implements Runnable, GameListener {

    private final ConsoleManager console;
    private TuiState state;

    private String username;
    private String connectionType;
    private String serverAddress;
    private int serverPort;
    private int playerId;

    private GeneralClientIf client;
    private SocketClient socketClient;
    private Player currentPlayer;
    private ShipBuildingHandler activeShipBuildingHandler;
    private List<Integer> availablePositions;

    public GalaxyTruckerTUI()
    {
        this.socketClient = null;
        this.client = null;
        this.console = new ConsoleManager();
        this.state = TuiState.INTRO;
        this.currentPlayer = null;
        this.activeShipBuildingHandler = null;
    }

    @Override
    public void run() {
        try {
            mainLoop();
        } finally {
            console.close();
        }
    }

    private void mainLoop() {
        while (true) {
            switch (state) {
                case INTRO                -> handleIntroState();
                case ASK_USERNAME         -> handleUsernameState();
                case ASK_NET              -> handleConnectionTypeState();
                case ASK_ADDRESS          -> handleAddressState();
                case CONNECTING           -> handleConnectingState();
                case WAIT_PLAYER_READY    -> handlePlayerReady();
                case SHIP_BUILDING        -> handleShipBuildingState();
                case CHOOSE_START_POSITION -> handleChooseStartPositionState();
                case WAIT_FOR_ALL_PLAYERS_POSITIONED -> handleWaitForAllPlayersPositionedState();
                case RUNNING              -> handleRunningState();
                case TERMINATED           -> { return; }
            }
        }
    }

    private void handleIntroState() {
        console.printBanner();
        state = TuiState.ASK_USERNAME;
    }

    private void handleUsernameState() {
        username = console.askUsername();
        this.currentPlayer = new Player(0, username, false); // Initial local player, ID will be updated by server
        // controller.addPlayer(this.currentPlayer); // Client doesn't add to its local controller directly
        state = TuiState.ASK_NET;
    }

    private void handleConnectionTypeState() {
        connectionType = console.askConnectionType();
        state = TuiState.ASK_ADDRESS;
    }

    private void handleAddressState() {
        serverAddress = console.askServerAddress();
        serverPort    = console.askServerPort(connectionType);
        state = TuiState.CONNECTING;
    }

    private void handleConnectingState() {
        try {
            console.printConnectingMessage(connectionType, serverAddress, serverPort);

            if ("socket".equalsIgnoreCase(connectionType)) {
                socketClient = new SocketClient(serverAddress, serverPort, this);
                client = socketClient;
                new Thread(socketClient).start();

                playerId = client.addPlayer(username);
                this.currentPlayer = new Player(0, username, false);
                client.setLocalPlayer(this.currentPlayer);

            } else if ("rmi".equalsIgnoreCase(connectionType)) {
                client = RmiClient.connectTo(serverAddress, this);
                if (client == null) {
                    throw new IllegalStateException("Failed to connect to RMI server.");
                }
                playerId = client.addPlayer(username);
                this.currentPlayer = new Player(playerId, username, false);
                client.setLocalPlayer(currentPlayer);

            } else {
                throw new IllegalArgumentException("Invalid connection type: " + connectionType);
            }

            console.printMessage("Connected as " + username + " with Player ID: " + playerId + "!");
            state = TuiState.WAIT_PLAYER_READY;

        } catch (Exception e) {
            console.printError("Connection failed: " + e.getMessage());
            e.printStackTrace();
            state = TuiState.ASK_NET;
        }
    }

    public void handlePlayerReady() {
        try {
            console.AskAreYouReadyQuestion();
            String readyAnswer = console.readCommand().trim().toLowerCase();

            if ("yes".equals(readyAnswer)) {

                console.printMessage("You are marked as ready. Waiting for game to start...");
                state = TuiState.SHIP_BUILDING;
            } else {
                console.printMessage("Okay, not marking as ready yet.");
            }
        } catch (Exception e) {
            console.printError("Error setting player ready: " + e.getMessage());
            state = TuiState.ASK_NET;
        }
    }

    private void handleShipBuildingState() {
        if (this.currentPlayer == null) {
            console.printError("Cannot start ship building: current player not set.");
            state = TuiState.ASK_USERNAME;
            return;
        }
        activeShipBuildingHandler = new ShipBuildingHandler(console, client, this.currentPlayer);
        activeShipBuildingHandler.manageShipBuilding();
        activeShipBuildingHandler = null;

        console.printMessage("Ship building complete. Proceeding to choose starting position...");
        state = TuiState.CHOOSE_START_POSITION;
    }

    private void handleChooseStartPositionState() {
        if (this.currentPlayer == null) {
            console.printError("Cannot choose starting position: current player not set.");
            state = TuiState.ASK_USERNAME;
            return;
        }

        console.printMessage("\n=== Choose Your Starting Position ===");

        try {
            // Request available positions from server 
            if (socketClient != null) {
                // Use the async method directly for socket clients
                socketClient.requestAvailablePositionsAsync();
                console.printMessage("Requesting available positions from server...");
            } else {
                // Use the interface method for other client types (RMI)
                availablePositions = client.requestAvailablePositions();
                if (availablePositions != null) {
                    showAvailablePositions(availablePositions);
                }
            }
        } catch (Exception e) {
            console.printError("Error requesting available positions: " + e.getMessage());
            state = TuiState.TERMINATED;
            return;
        }

        boolean positionChosen = false;
        while (!positionChosen) {
            if (availablePositions == null || availablePositions.isEmpty()) {
                console.printMessage("Waiting for server to provide available positions...");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                continue;
            }

            String input = console.askQuestion("Enter your desired starting position " + availablePositions + ": ");

            try {
                int chosenPosition = Integer.parseInt(input);
                
                // Validate position selection - use local data when connected to server
                boolean isValid;
                if (socketClient != null) {
                    isValid = availablePositions.contains(chosenPosition);
                } else if (availablePositions != null) {
                    // For clients that provide positions synchronously (RMI)
                    isValid = availablePositions.contains(chosenPosition);
                } else {
                    try {
                        isValid = client.isPositionSelectionValid(chosenPosition);
                    } catch (Exception e) {
                        console.printError("Error validating position: " + e.getMessage());
                        isValid = false;
                    }
                }
                if (!isValid) {
                    console.printError("Position " + chosenPosition + " is not available. Please choose from: " + availablePositions);
                    continue;
                }

                try {
                    if (socketClient != null) {
                        socketClient.selectPositionAsync(chosenPosition);
                        console.printMessage("Position request sent to server. Waiting for confirmation...");
                        // The response will be handled by notifyPositionResponse
                        positionChosen = true;
                        // Don't transition state yet - wait for server confirmation
                    } else {
                        // Use interface method for other client types (RMI)
                        boolean success = client.selectPosition(chosenPosition);
                        if (success) {
                            console.printMessage("Position " + chosenPosition + " selected successfully!");
                            positionChosen = true;
                            state = TuiState.WAIT_FOR_ALL_PLAYERS_POSITIONED;
                        } else {
                            console.printError("Failed to select position " + chosenPosition + ". It might be taken or invalid.");
                        }
                    }
                } catch (UnsupportedOperationException e) {
                    // Handle cases where client doesn't support synchronous position selection
                    console.printError("Offline mode not currently supported. Please use network connection.");
                    positionChosen = true; // Exit the loop to prevent infinite loop
                    state = TuiState.TERMINATED;
                } catch (Exception e) {
                    console.printError("Error selecting position: " + e.getMessage());
                }
            } catch (NumberFormatException e) {
                console.printError("Invalid input. Please enter a number.");
            }
        }
    }

    private void handleWaitForAllPlayersPositionedState() {
        console.printMessage("Waiting for all players to choose their positions...");
        
        // For non-socket clients, check if all players are positioned
        if (socketClient == null) {
            try {
                boolean allPositioned = client.areAllPlayersPositioned();
                if (allPositioned) {
                    console.printMessage("\nAll players have chosen their positions! Starting the game...");
                    state = TuiState.RUNNING;
                    return;
                }
            } catch (UnsupportedOperationException e) {
                // Client doesn't support this operation, continue with normal flow
            } catch (Exception e) {
                console.printError("Error checking if all players are positioned: " + e.getMessage());
            }
        }
        
        try {
            Thread.sleep(2000); // Small sleep to prevent tight loop
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Called by the network client when the server signals that all players
     * have chosen their positions and the game can proceed.
     */
    public synchronized void proceedToRunningState() {
        // When connected to server, trust the server's signal that all players are positioned
        // When offline, use controller to check if game can proceed
        boolean canProceed = (socketClient != null) ? true : false; // controller.canProceedToRunningState();
        
        if (canProceed) {
            if (this.state == TuiState.WAIT_FOR_ALL_PLAYERS_POSITIONED) {
                console.printMessage("\nAll players have chosen their positions! Starting the game...");
                this.state = TuiState.RUNNING;
            } else if (this.state == TuiState.CHOOSE_START_POSITION) {
                // If we're still in position selection, transition to waiting state first
                console.printMessage("\nPosition selected. Waiting for other players...");
                this.state = TuiState.WAIT_FOR_ALL_PLAYERS_POSITIONED;
            } else {
                // Log or handle unexpected state transition attempt
                console.printError("Tried to proceed to running state from an unexpected state: " + this.state);
            }
        } else {
            console.printMessage("Waiting for more players to choose their positions...");
        }
    }

    private void handleRunningState() {
        //console.printHelp();
        TuiController tuiController = new TuiController(client, console, playerId);

        while (true) {
            String cmd = console.readCommand();
            if (!tuiController.executeCommand(cmd)) {
                state = TuiState.TERMINATED;
                return;
            }
        }
    }

    @Override
    public void displayRefresh() {
        if (activeShipBuildingHandler != null && state == TuiState.SHIP_BUILDING) {
            activeShipBuildingHandler.refreshDisplay();
        }
    }

    public void printTextMsg(String message)
    {
        console.printMessage("[Server] " + message);
    }

    public boolean notifyNewPlayerPosition(int playerId, int position) {
        updatePlayerPosition(playerId, position);
        return true;
    }

    /**
     * Called by PositionResponseMessage to handle position selection response
     */
    public void triggerPositionSelectionRefresh() {
        if (state == TuiState.CHOOSE_START_POSITION) {
            // If we're still in position selection state, the selection failed
            if (socketClient != null) {
                try {
                    socketClient.requestAvailablePositionsAsync();
                } catch (IOException e) {
                    console.printError("Error refreshing available positions: " + e.getMessage());
                }
            }
        }

        // Check if we can now proceed to the next state
        // When connected to server, this method is typically called after successful position selection
        // When offline, use controller logic to check
        boolean canProceedToNext = (socketClient != null) ?
            true : // Server will handle state transitions
            false; // (controller.canProceedToRunningState() && state == TuiState.CHOOSE_START_POSITION);

        if (canProceedToNext && state == TuiState.CHOOSE_START_POSITION) {
            console.printMessage("Position selected. Waiting for other players...");
            state = TuiState.WAIT_FOR_ALL_PLAYERS_POSITIONED;
        }
    }

    /**
     * Called by network messages to update available positions display
     */
    public void showAvailablePositions(List<Integer> positions) {
        this.availablePositions = positions;
        if (positions.isEmpty()) {
            console.printMessage("No positions available currently. Please wait...");
        } else {
            // If we're currently in position selection, show live updates
            if (state == TuiState.CHOOSE_START_POSITION) {
                console.printMessage("⟳ Available positions updated: " + positions);
            } else {
                console.printMessage("Available positions: " + positions);
            }
        }
    }
    
    /**
     * Called by PositionUpdateMessage to update a player's position
     */
    public void updatePlayerPosition(int playerId, int position) {
        console.printMessage("Player ID " + playerId + " has selected position " + position);
        
        // Position updates and available positions will be automatically updated
        // via server broadcasts, so no need to manually request them
    }
    
    /**
     * Display a message in the console
     */
    public void showMessage(String message) {
        console.printMessage(message);
    }

    @Override
    public void componentPlaced(Component component, Position position) {
        if (currentPlayer != null && currentPlayer.getShip() != null) {
            currentPlayer.getShip().placeComponent(component, position);
            console.printMessage("Component " + component.getClass().getSimpleName() + 
                               " placed at (" + position.getX() + "," + position.getY() + ")");
        }
    }

    @Override
    public void componentRemoved(Position position) {
        if (currentPlayer != null && currentPlayer.getShip() != null) {
            Component removed = currentPlayer.getShip().getComponentAt(position);
            if (removed != null) {
                currentPlayer.getShip().removeComponent(removed);
                console.printMessage("Component removed from (" + position.getX() + "," + position.getY() + ")");
            }
        }
    }

    @Override
    public void componentRotated(Position position, RotationType rotationType) {
        console.printMessage("Component at (" + position.getX() + "," + position.getY() + 
                           ") rotated " + rotationType);
    }

    @Override
    public void shipValidated(boolean isValid, String message) {
        if (isValid) {
            console.printMessage("✓ " + message);
            if (state == TuiState.SHIP_BUILDING) {
                //state = TuiState.NEXT_STATE; // to be defined
            }
        } else {
            console.printError("✗ " + message);
        }
    }

}
