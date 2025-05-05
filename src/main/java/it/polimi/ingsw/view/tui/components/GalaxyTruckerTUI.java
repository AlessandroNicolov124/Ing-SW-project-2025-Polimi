package it.polimi.ingsw.view.tui.components;

import it.polimi.ingsw.GameController;
import it.polimi.ingsw.Player;
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

    private Object client;
    private RmiClient rmiClient;
    private SocketClient socketClient;
    private PlayerDTO plDTO;

    public GalaxyTruckerTUI()
    {
        this.rmiClient = null;
        this.socketClient = null;
        this.controller = new GameController();
        this.console = new ConsoleManager();
        this.state = TuiState.INTRO;
        this.plDTO = null;
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
                case INTRO        -> handleIntroState();
                case ASK_USERNAME -> handleUsernameState();
                case ASK_NET      -> handleConnectionTypeState();
                case ASK_ADDRESS  -> handleAddressState();
                case CONNECTING   -> handleConnectingState();
                case WAIT_PLAYER_READY -> handlePlayerReady();
                case RUNNING      -> handleRunningState();
                case TERMINATED   -> { return; }
            }
        }
    }

    private void handleIntroState() {
        console.printBanner();
        state = TuiState.ASK_USERNAME;
    }

    private void handleUsernameState() {
        username = console.askUsername();
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

            if ("rmi".equals(connectionType)) {
                rmiClient = RmiClient.connectTo(serverAddress);
                if (rmiClient == null)
                    throw new IllegalStateException("Failed to connect to RMI server");

                client = rmiClient;

            } else {
                socketClient = new SocketClient(serverAddress, serverPort);
                client = socketClient;
                new Thread(socketClient).start();
            }


            if (rmiClient == null) {
                //In case of socket connection
                this.plDTO = new PlayerDTO(username);
                //controller.addPlayer(pl);
                socketClient.socketAddPlayer(this.plDTO);
                //socketClient.sendObject(controller);
            }

            console.printMessage("Connected as " + username + "!");
            state = TuiState.WAIT_PLAYER_READY;

        } catch (Exception e) {
            console.printError("Connection failed: " + e.getMessage());
            state = TuiState.ASK_NET;
        }
    }

    public void handlePlayerReady() {
        try {
            console.AskAreYouReadyQuestion();
            PlayerReadyCommand plRdCmd = new PlayerReadyCommand(this.plDTO);

            if (rmiClient == null) {
                socketClient.socketSetPlayerReady(plRdCmd);
            }
            state = TuiState.RUNNING;
        }
        catch (Exception e) {
            console.printError("Connection failed: " + e.getMessage());
            state = TuiState.ASK_NET;
        }
    }

    private void handleRunningState() {
        console.printHelp();
        CommandExecutor executor = new CommandExecutor(client, console);

        while (true) {
            String cmd = console.readCommand();
            if (!executor.executeCommand(cmd)) {
                state = TuiState.TERMINATED;
                return;
            }
        }
    }
}
