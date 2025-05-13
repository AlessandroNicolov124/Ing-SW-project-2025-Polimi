package it.polimi.ingsw.view.tui.components;

import it.polimi.ingsw.GameController;
import it.polimi.ingsw.Player;
import it.polimi.ingsw.network.client.GameClient;
import it.polimi.ingsw.network.commands.PlayerDTO;
import it.polimi.ingsw.network.commands.PlayerReadyCommand;
import it.polimi.ingsw.network.rmi.RmiClient;
import it.polimi.ingsw.network.socket.client.SocketClient;


public class GalaxyTruckerTUI implements Runnable {

    private final ConsoleManager console;
    private GameController controller;
    private TuiState state;

    private String username;
    private String connectionType;
    private String serverAddress;
    private int serverPort;
    private int playerId;

    private GameClient client;
    private RmiClient rmiClient;
    private SocketClient socketClient;
    private PlayerDTO plDTO;
    private Player currentPlayer;
    private ShipBuildingHandler activeShipBuildingHandler;

    public GalaxyTruckerTUI()
    {
        this.rmiClient = null;
        this.socketClient = null;
        this.controller = new GameController();
        this.console = new ConsoleManager();
        this.state = TuiState.INTRO;
        this.plDTO = null;
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
                plDTO = new PlayerDTO(username);

                playerId = socketClient.addPlayer(username);
                this.currentPlayer = new Player(playerId, username, false);
                socketClient.setLocalPlayer(this.currentPlayer);

            } else if ("rmi".equalsIgnoreCase(connectionType)) {
                rmiClient = RmiClient.connectTo(serverAddress);
                if (rmiClient == null)
                    throw new IllegalStateException("Failed to connect to RMI server");
                client = rmiClient;
                playerId = rmiClient.addPlayer(username);
                this.currentPlayer = new Player(playerId, username, false);

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
                if (this.currentPlayer != null) {
                    controller.setPlayerReady(this.currentPlayer);
                }

                PlayerReadyCommand plRdCmd = new PlayerReadyCommand(this.plDTO != null ? this.plDTO : new PlayerDTO(username));

                if (socketClient != null) {
                    socketClient.socketSetPlayerReady(plRdCmd);
                } else if (rmiClient != null) {
                }
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
        activeShipBuildingHandler = new ShipBuildingHandler(console, socketClient, this.currentPlayer);
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

        boolean positionChosen = false;
        while (!positionChosen) {
            java.util.List<Integer> availablePositions = controller.getAvailableStartingPositions();

            if (availablePositions.isEmpty()) {
                console.printError("No starting positions available. This shouldn't happen in a standard game. Waiting...");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    console.printError("Interrupted while waiting.");
                    state = TuiState.TERMINATED;
                    return;
                }
                continue;
            }

            console.printMessage("Available starting positions: " + availablePositions.toString());
            String input = console.askQuestion("Enter your desired starting position (1-4): ");

            try {
                int chosenPosition = Integer.parseInt(input);
                if (controller.setPlayerInitialStartingPosition(this.currentPlayer, chosenPosition)) {
                    positionChosen = true;
                    console.printMessage("Position " + chosenPosition + " selected. Waiting for other players...");
                    state = TuiState.WAIT_FOR_ALL_PLAYERS_POSITIONED;
                } else {
                    console.printError("Failed to set position. It might be taken or invalid. Please try again.");
                }
            } catch (NumberFormatException e) {
                console.printError("Invalid input. Please enter a number.");
            }
        }
    }

    private void handleWaitForAllPlayersPositionedState() {
        // This state just waits. The console.printMessage was moved to the end of handleChooseStartPositionState.
        // The actual transition to RUNNING will be triggered by an external call to proceedToRunningState().
        // To prevent busy-looping and consuming CPU, we can add a small sleep if no other mechanism
        // is pausing the TUI thread. However, typically, the readCommand() in other states blocks.
        // If the mainLoop continuously spins here, a Thread.sleep might be needed if this state can be active for a while.
        // For now, we assume the loop will check state, print nothing if it's this state, and proceed.
        // If there's a desire for a continuously updating message or a spinning cursor, that'd be more complex.
    }

    /**
     * Called by the network client when the server signals that all players
     * have chosen their positions and the game can proceed.
     */
    public synchronized void proceedToRunningState() {
        if (this.state == TuiState.WAIT_FOR_ALL_PLAYERS_POSITIONED) {
            console.printMessage("\nAll players have chosen their positions! Starting the game...");
            this.state = TuiState.RUNNING;
        } else {
            // Log or handle unexpected state transition attempt
            console.printError("Tried to proceed to running state from an unexpected state: " + this.state);
        }
    }

    private void handleRunningState() {
        console.printHelp();
        TuiController tuiController = new TuiController(client, console, playerId);

        while (true) {
            String cmd = console.readCommand();
            if (!tuiController.executeCommand(cmd)) {
                state = TuiState.TERMINATED;
                return;
            }
        }
    }

    public void triggerShipDisplayRefresh() {
        if (activeShipBuildingHandler != null && state == TuiState.SHIP_BUILDING) {
            activeShipBuildingHandler.refreshDisplay();
        }
    }
}
