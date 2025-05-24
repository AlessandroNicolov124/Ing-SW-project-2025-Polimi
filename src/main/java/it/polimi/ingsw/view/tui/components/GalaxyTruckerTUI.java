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
                socketClient.requestAvailablePositions();
                console.printMessage("Requesting available positions from server...");
            } else {
                // Fall back to local positions if no socket client
                //availablePositions = client.getAvailableStartingPositions();
                showAvailablePositions(availablePositions);
            }
        } catch (IOException e) {
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
                
                // Validate position selection - use local data when connected to server, controller when offline
//                boolean isValid = (socketClient != null) ?
//                    availablePositions.contains(chosenPosition) :
                    //client.isPositionSelectionValid(chosenPosition);
                    
                if (!true) {
                    console.printError("Position " + chosenPosition + " is not available. Please choose from: " + availablePositions);
                    continue;
                }

                try {
                    if (socketClient != null) {
                        socketClient.selectPosition(chosenPosition);
                        console.printMessage("Position request sent to server. Waiting for confirmation...");
                        // The response will be handled by notifyPositionResponse
                        positionChosen = true;
                        // Don't transition state yet - wait for server confirmation
                    } else {
                        //TODO implemet corretly using client
                        // Fall back to local controller if no socket client
                        /*if (controller.handlePlayerPositionSelection(this.currentPlayer, chosenPosition)) {
                            positionChosen = true;
                            console.printMessage("Position " + chosenPosition + " selected. Waiting for other players...");
                            state = TuiState.WAIT_FOR_ALL_PLAYERS_POSITIONED;
                        } else {
                            console.printError("Failed to set position. It might be taken or invalid. Please try again.");
                        }*/
                    }
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
        //boolean canProceed = (socketClient != null) ? true : controller.canProceedToRunningState();
        
        if (true) {
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
        //TODO implement actual method
    }

    public boolean notifyNewPlayerPosition(int playerId, int position) {
        //TODO implement actual method
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
                    socketClient.requestAvailablePositions();
                } catch (IOException e) {
                    console.printError("Error refreshing available positions: " + e.getMessage());
                }
            }
        }

        // Check if we can now proceed to the next state
        // When connected to server, this method is typically called after successful position selection
        // When offline, use controller logic to check
        //boolean canProceedToNext = (socketClient != null) ?
            //true : // Server will handle state transitions
            //(controller.canProceedToRunningState() && state == TuiState.CHOOSE_START_POSITION);


//        if (canProceedToNext && state == TuiState.CHOOSE_START_POSITION) {
//            console.printMessage("Position selected. Waiting for other players...");
//            state = TuiState.WAIT_FOR_ALL_PLAYERS_POSITIONED;

    }

    /**
     * Called by network messages to update available positions display
     */
    public void showAvailablePositions(List<Integer> positions) {
        this.availablePositions = positions;
        if (positions.isEmpty()) {
            console.printMessage("No positions available currently. Please wait...");
        } else {
            console.printMessage("Available positions: " + positions);
        }
    }
    
    /**
     * Called by PositionUpdateMessage to update a player's position
     */
    public void updatePlayerPosition(int playerId, int position) {
        console.printMessage("Player ID " + playerId + " has selected position " + position);
        
        // When connected to server, check if we're the last player to select a position
        if (socketClient != null) {
            try {
                socketClient.requestAvailablePositions();
            } catch (IOException e) {
                console.printError("Error checking remaining positions: " + e.getMessage());
            }
        } else {
            // Offline mode - use controller to update position
            //boolean success = controller.updatePlayerPosition(playerId, position);
            
//            if (!success) {
//                console.printError("Failed to update position for player ID " + playerId);
            //}
        }
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
