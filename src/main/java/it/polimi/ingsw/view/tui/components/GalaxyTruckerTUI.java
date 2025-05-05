package it.polimi.ingsw.view.tui.components;

import it.polimi.ingsw.network.client.GameClient;
import it.polimi.ingsw.network.rmi.RmiClient;
import it.polimi.ingsw.network.socket.client.SocketClient;

public class GalaxyTruckerTUI implements Runnable {

    private final ConsoleManager console;
    private TuiState state;
    private String username;
    private String connectionType;
    private String serverAddress;
    private int serverPort;
    private GameClient client;
    private int playerId;


    public GalaxyTruckerTUI()
    {
        this.console = new ConsoleManager();
        this.state = TuiState.INTRO;
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
                client = RmiClient.connectTo(serverAddress);
                if (client == null)
                    throw new IllegalStateException("Failed to connect to RMI server");
                
                playerId = client.addPlayer(username);

            } else {
                SocketClient socketClient = new SocketClient(serverAddress, serverPort);
                client = socketClient;
                
                Thread clientThread = new Thread(socketClient, "socket-client");
                clientThread.setDaemon(true);
                clientThread.start();
                playerId = socketClient.addPlayer(username);
            }
            
            console.printMessage("Connected as " + username + "!");
            state = TuiState.RUNNING;

        } catch (Exception e) {
            console.printError("Connection failed: " + e.getMessage());
            if (client != null) {
                try {
                    client.close();
                } catch (Exception ignored) {}
                client = null;
            }
            state = TuiState.ASK_NET;
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
}
