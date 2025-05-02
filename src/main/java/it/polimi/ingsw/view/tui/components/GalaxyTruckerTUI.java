package it.polimi.ingsw.view.tui.components;

import it.polimi.ingsw.network.rmi.RmiClient;
import it.polimi.ingsw.network.socket.client.SocketClient;


public class GalaxyTruckerTUI implements Runnable {

    private final ConsoleManager console = new ConsoleManager();
    private TuiState state = TuiState.INTRO;

    private String username;
    private String connectionType;
    private String serverAddress;
    private int    serverPort;

    private Object client;

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
                RmiClient rmi = RmiClient.connectTo(serverAddress);
                if (rmi == null)
                    throw new IllegalStateException("Failed to connect to RMI server");
            
                client = rmi;                       
            
            } else {                                
                SocketClient socket = new SocketClient(serverAddress, serverPort);
                client = socket;
                new Thread(socket).start();         
            }

            console.printMessage("Connected as " + username + "!");
            state = TuiState.RUNNING;

        } catch (Exception e) {
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
