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

    public GalaxyTruckerTUI()
    {
        this.rmiClient = null;
        this.socketClient = null;
        this.controller = new GameController();
        this.console = new ConsoleManager();
        this.state = TuiState.INTRO;
        this.plDTO = null;
        this.currentPlayer = null;
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
        this.currentPlayer = new Player(0, username, false);
        controller.addPlayer(this.currentPlayer);
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

            if ("rmi".equalsIgnoreCase(connectionType)) {
                rmiClient = RmiClient.connectTo(serverAddress);
                if (rmiClient == null)
                    throw new IllegalStateException("Failed to connect to RMI server");
                client = rmiClient;

                playerId = rmiClient.addPlayer(username);

            } else if ("socket".equalsIgnoreCase(connectionType)) {
                socketClient = new SocketClient(serverAddress, serverPort);
                client = socketClient;
                new Thread(socketClient).start();
                plDTO = new PlayerDTO(username);

                playerId = socketClient.addPlayer(username);
            } else {
                throw new IllegalArgumentException("Invalid connection type: " + connectionType);
            }

            console.printMessage("Connected as " + username + "!");
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
        ShipBuildingHandler shipBuilderUI = new ShipBuildingHandler(console, socketClient, this.currentPlayer);
        shipBuilderUI.manageShipBuilding();

        console.printMessage("Ship building complete. Transitioning to next phase...");
        state = TuiState.RUNNING;
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
}
