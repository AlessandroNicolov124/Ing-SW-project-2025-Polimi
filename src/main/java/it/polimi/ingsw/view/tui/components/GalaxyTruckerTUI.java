package it.polimi.ingsw.view.tui.components;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import it.polimi.ingsw.Player;
import it.polimi.ingsw.Position;
import it.polimi.ingsw.ShipBuilder;
import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.TokenType;
import it.polimi.ingsw.network.Difficulty;
import it.polimi.ingsw.network.client.GeneralClientIf;
import it.polimi.ingsw.network.client.listeners.GameListener;
import it.polimi.ingsw.network.rmi.RmiClient;
import it.polimi.ingsw.network.socket.client.SocketClient;
import it.polimi.ingsw.network.messages.RotationType;



public class GalaxyTruckerTUI implements Runnable, GameListener {

    private final ConsoleManager console;
    private TuiState state;
    private final Object displayMutex = new Object(); // Add synchronization for display operations

    private String username;
    private String connectionType;
    private String serverAddress;
    private int serverPort;

    private GeneralClientIf client;
    private SocketClient socketClient;
    private Player currentPlayer;
    private ShipBuildingHandler activeShipBuildingHandler;
    private LobbyHandler lobbyHandler;
    private List<Integer> availablePositions;
    private boolean localReady = false;

    private CompletableFuture<Player> playerCreationFuture;
    private Map<Integer, Integer> currentPlayerPositions = new HashMap<>();
    private final int PENALTY_TIMEOUT = 10000;

    private PreparationFlightHandler preparationFlightHandler;
    private UUID currentMatchId;


    public GalaxyTruckerTUI()
    {
        this.socketClient = null;
        this.client = null;
        this.console = new ConsoleManager();
        this.state = TuiState.INTRO;
        this.currentPlayer = null;
        this.activeShipBuildingHandler = null;
        this.lobbyHandler = null;
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
                case WAIT_FOR_CALLBACK_PLAYER    -> handleWaitForPlayerCreationState();
                case ASK_NET              -> handleConnectionTypeState();
                case ASK_ADDRESS          -> handleAddressState();
                case CONNECTING           -> handleConnectingState();
                case LOBBY_MENU           -> handleLobbyMenuState();
                case WAIT_IN_MATCH        -> handleWaitInMatchState();
                case WAITING_FOR_PLAYERS  -> handleWaitForAllPlayers();
                case LOADING              -> handleLoadingMatchState();
/*                 case WAIT_PLAYER_READY    -> handlePlayerReady(); */
                case SHIP_BUILDING        -> handleShipBuildingState();
                case PREPARATION_FLIGHT -> handlePreparationFlightState();
                case WAIT_FOR_ALL_PLAYERS_POSITIONED -> handleWaitForAllPlayersPositionedState();
                case ADVENTURE_PHASE      -> handleAdventurePhaseState();
                case RUNNING              -> handleRunningState();
                case GAME_ENDED           -> handleGameEndedState();
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
        if (username.isEmpty()) {
            console.printError("Username cannot be empty!");
            return;
        }
        state = TuiState.ASK_NET;
    }

    private void handleConnectionTypeState() {
        connectionType = console.askConnectionType();
        state = TuiState.ASK_ADDRESS;
    }

    private void handleAddressState() {
        serverAddress = console.askServerAddress();
        serverPort = console.askServerPort(connectionType);
        state = TuiState.CONNECTING;
    }

    private void handleConnectingState() {
        try {
            console.printConnectingMessage(connectionType, serverAddress, serverPort);
            playerCreationFuture = new CompletableFuture<>();

            if ("socket".equalsIgnoreCase(connectionType)) {
                socketClient = new SocketClient(serverAddress, serverPort, this);
                client = socketClient;
                new Thread(socketClient).start();

                client.createPlayer(username);

            } else if ("rmi".equalsIgnoreCase(connectionType)) {
                client = RmiClient.connectTo(serverAddress, serverPort, this);
                if (client == null) {
                    throw new IllegalStateException("Failed to connect to RMI server.");
                }
                
                client.createPlayer(username);

            } else {
                throw new IllegalArgumentException("Invalid connection type: " + connectionType);
            }

            
            state = TuiState.WAIT_FOR_CALLBACK_PLAYER;

        } catch (Exception e) {
            console.printError("Connection failed: " + e.getMessage());
            state = TuiState.ASK_NET;
        }
    }

    private void handleWaitForPlayerCreationState() {
        if (playerCreationFuture == null) {
            console.printError("Internal error: player creation future is null");
            state = TuiState.ASK_USERNAME;
            return;
        }
        
        try {
            Player p = playerCreationFuture.get(15, TimeUnit.SECONDS);

            console.printMessage("Connected as " + username + "!");
            this.currentPlayer = p;
            state = TuiState.LOBBY_MENU;

        } catch (TimeoutException e) {
            console.printError("Timeout in attesa della creazione del giocatore. Riprova.");
            state = TuiState.ASK_USERNAME;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            console.printError("Attesa interrotta.");
            state = TuiState.ASK_USERNAME;

        } catch (ExecutionException e) {
            console.printError("Errore nella creazione del giocatore: " + e.getCause().getMessage());
            state = TuiState.ASK_USERNAME;
        } finally {
            playerCreationFuture = null;
        }
    }

    private void handleLobbyMenuState() {
        try {
            if (lobbyHandler == null) {
                console.printError("Lobby handler not initialized!");
                state = TuiState.TERMINATED;
                return;
            }

        while (state == TuiState.LOBBY_MENU) {
            lobbyHandler.tick();
            String cmd = console.pollCommand(200);
            if (cmd != null && !lobbyHandler.executeCommand(cmd)) {
                state = TuiState.TERMINATED;
                return;
            }
        }

        } catch (Exception e) {
            console.printError("Error in lobby menu: " + e.getMessage());
        }
    }

    private void handleWaitInMatchState() {
        if (lobbyHandler == null || !lobbyHandler.isInMatch()) {
            state = TuiState.LOBBY_MENU;
            return;
        }

        while (state == TuiState.WAIT_IN_MATCH) {
            lobbyHandler.tick();
            String cmd = console.pollCommand(200);
            if (cmd != null && !lobbyHandler.executeCommand(cmd)) {
                state = TuiState.TERMINATED;
                return;
            }
            if (!lobbyHandler.isInMatch()) {
                state = TuiState.LOBBY_MENU;
            }
        }
    }

    private void handleLoadingMatchState() {
        try {
            client.loadingAck(lobbyHandler.getCurrentMatchId());
        } catch (Exception e) {
            e.printStackTrace();
        }
        console.printMessage("Match started by: " + username + "! Waiting for other players to start!");

        state = TuiState.WAITING_FOR_PLAYERS;

    }

    private void handleWaitForAllPlayers() {
        if (!localReady) {
            console.printMessage("Press enter when you are ready");
        }

        while (state == TuiState.WAITING_FOR_PLAYERS) {
            String input = console.pollCommand(200);

            if (input != null && !localReady) {
                try {
                    client.playerReady(lobbyHandler.getCurrentMatchId(), lobbyHandler.getPlayerId());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                console.printMessage("You are marked as ready. Waiting for game to start...");
                localReady = true;
            }
        }
        localReady = false;
     }

    private void handleShipBuildingState() {
        if (this.currentPlayer == null) {
            console.printError("Cannot start ship building: current player not set.");
            state = TuiState.ASK_USERNAME;
            return;
        }
        
        // Create handler only once
        if (activeShipBuildingHandler == null) {
            activeShipBuildingHandler = new ShipBuildingHandler(console, client, this.currentPlayer, this);
        }
        
        // Run ship building synchronously - it will handle user input and server callbacks
        activeShipBuildingHandler.manageShipBuilding();
        
        // Clean up when done
        activeShipBuildingHandler = null;
        console.printMessage("Ship building phase completed.");
    }

    private void handleWaitForAllPlayersPositionedState() {
        console.printMessage("\n=== WAITING FOR ALL PLAYERS ===");
        console.printMessage("Your ship has been validated and you have been assigned a position!");
        console.printMessage("Flight board positions assigned based on completion order:");
        console.printMessage("  • 1st to finish → Flight Position 6 (BEST POSITION)");
        console.printMessage("  • 2nd to finish → Flight Position 3");
        console.printMessage("  • 3rd to finish → Flight Position 1");
        console.printMessage("  • 4th to finish → Flight Position 0 (worst position)");
        console.printMessage("");
        console.printMessage("IMPORTANT: Player in the BEST position (highest number) picks adventure cards!");
        console.printMessage("Waiting for other players to complete ship building...");
        console.printMessage("Adventure phase will start once all players are positioned.");
        console.printMessage("Type 'status' to check current state or 'help' for available commands.");
        
        while (state == TuiState.WAIT_FOR_ALL_PLAYERS_POSITIONED) {
            String input = console.pollCommand(2000); // 2 second timeout
            
            if (input != null) {
                input = input.trim().toLowerCase();
                switch (input) {
                    case "status":
                        console.printMessage("Current state: Waiting for all players to complete ship building");
                        if (currentPlayer != null) {
                            console.printMessage("Player: " + currentPlayer.getNickName() + " (ID: " + currentPlayer.getId() + ")");
                            console.printMessage("You have completed ship building and been assigned a position!");
                        }
                        console.printMessage("Waiting for other players to finish building and get positioned...");
                        console.printMessage("Once all players are positioned, the adventure card phase will begin.");
                        break;
                    case "help":
                        console.printMessage("Available commands:");
                        console.printMessage("  status    - Show current game status");
                        console.printMessage("  debugskip - [DEBUG] Skip waiting and proceed to adventure phase");
                        console.printMessage("  quit      - Exit the game");
                        break;
                    case "debugskip":
                        console.printMessage("[DEBUG] Skipping position assignment wait. Proceeding to adventure phase...");
                        console.printMessage("[DEBUG] Note: This may cause issues if not all players are actually positioned.");
                        state = TuiState.ADVENTURE_PHASE;
                        break;
                    case "quit":
                        console.printMessage("Exiting game...");
                        state = TuiState.TERMINATED;
                        return;
                    default:
                        console.printMessage("Unknown command: " + input + ". Type 'help' for available commands.");
                        break;
                }
            }
        }
        
        // Add a fallback timeout to prevent infinite waiting
        // In a real implementation, this would be removed and proper server communication would handle state transitions
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
                console.printMessage("\nAll players have been positioned! Starting the game...");
                this.state = TuiState.RUNNING;
            } else {
                // Log or handle unexpected state transition attempt
                console.printError("Tried to proceed to running state from an unexpected state: " + this.state);
            }
        } else {
            console.printMessage("Waiting for more players to be positioned...");
        }
    }

    private void handleAdventurePhaseState() {
        console.printMessage("\n=== ADVENTURE CARD PHASE ===");
        console.printMessage("Only the LEADING player (highest position number) can pick adventure cards for everyone");
        console.printMessage("Wait for the card picker to pick or type 'status' to check current state");
        
        while (state == TuiState.ADVENTURE_PHASE) {
            String cmd = console.pollCommand(1000); // Poll with timeout
            
            if (cmd != null) {
                cmd = cmd.trim().toLowerCase();
                switch (cmd) {
                    case "pick":
                        // Try to pick an adventure card (only leader can)
                        try {
                            client.pickAdventureCard(currentPlayer.getId());
                            console.printMessage("Adventure card pick request sent. Waiting for response...");
                        } catch (Exception e) {
                            console.printError("Error picking adventure card: " + e.getMessage());
                        }
                        break;
                    case "status":
                        if (currentPlayer != null) {
                            console.printMessage("Player: " + currentPlayer.getNickName() + " (ID: " + currentPlayer.getId() + ")");
                            console.printMessage("Game state: Adventure Phase - only leader can pick cards");
                        }
                        break;
                    case "help":
                        console.printMessage("Available commands:");
                        console.printMessage("  pick   - Pick an adventure card (only if you are the leader)");
                        console.printMessage("  status - Show current game status");
                        console.printMessage("  quit   - Exit the game");
                        break;
                    case "quit":
                        console.printMessage("Exiting game...");
                        state = TuiState.TERMINATED;
                        return;
                    default:
                        console.printMessage("Unknown command: " + cmd + ". Type 'help' for available commands.");
                        break;
                }
            }
        }
    }

    private void handlePreparationFlightState() {
        if (this.currentPlayer == null) {
            console.printError("Cannot start preparation phase: current player not set.");
            state = TuiState.ASK_USERNAME;
            return;
        }
        
        preparationFlightHandler = new PreparationFlightHandler(client, console, currentPlayer);
        
        preparationFlightHandler.managePreparationFlight();
        
        console.printMessage("Preparation phase completed!");
        state = TuiState.ADVENTURE_PHASE;
    }


    private void handleRunningState() {
        while (true) {
            String cmd = console.readCommand();
            if (!lobbyHandler.executeCommand(cmd)) {
                state = TuiState.TERMINATED;
                return;
            }
        }
    }

    private void handleGameEndedState() {
        console.printMessage("\n GAME COMPLETE! ");
        console.printMessage("The adventure card phase has ended.");
        console.printMessage("");
        console.printMessage("Final game statistics would be shown here in a complete implementation.");
        console.printMessage("");
        console.printMessage("Commands:");
        console.printMessage("  status  - Show final game state");
        console.printMessage("  quit    - Exit the game");
        
        while (state == TuiState.GAME_ENDED) {
            String cmd = console.pollCommand(1000);
            
            if (cmd != null) {
                cmd = cmd.trim().toLowerCase();
                switch (cmd) {
                    case "status":
                        if (currentPlayer != null) {
                            console.printMessage("Player: " + currentPlayer.getNickName() + " (ID: " + currentPlayer.getId() + ")");
                            console.printMessage("Game state: Complete - adventure cards exhausted or only one player remaining");
                        }
                        break;
                    case "quit":
                        console.printMessage("Thanks for playing Galaxy Trucker!");
                        state = TuiState.TERMINATED;
                        return;
                    case "help":
                        console.printMessage("Available commands:");
                        console.printMessage("  status - Show current game status");
                        console.printMessage("  quit   - Exit the game");
                        break;
                    default:
                        console.printMessage("Unknown command: " + cmd + ". Type 'help' for available commands.");
                        break;
                }
            }
        }
    }

    @Override
    public void displayRefresh() {
        synchronized (displayMutex) {
            if (activeShipBuildingHandler != null && state == TuiState.SHIP_BUILDING) {
                activeShipBuildingHandler.refreshDisplay();
            }
        }
    }

    public void printTextMsg(String message)
    {
        //TODO implement actual method
    }

    @Override
    public void matchCreatedFailed(String errorMessage) {

    }

    public boolean notifyNewPlayerPosition(int playerId, int position) {
        //TODO implement actual method
        return true;
    }

    @Override
    public void notifyNewPlayerPositionFailed(String errorMessage) {

    }

    @Override
    public void positionAssigned(int playerId, String playerName, int assignedPosition, boolean success, String message) {
        synchronized (displayMutex) {
            if (success) {
                console.printMessage(message);
                
                // If this is our player, update our state
                if (currentPlayer != null && currentPlayer.getId() == playerId) {
                    console.printMessage("You have been assigned position " + assignedPosition + "!");
                    // Transition to waiting for all players positioned state
                    if (state == TuiState.SHIP_BUILDING) {
                        state = TuiState.WAIT_FOR_ALL_PLAYERS_POSITIONED;
                    }
                } else {
                    console.printMessage(playerName + " was assigned position " + assignedPosition);
                }
            } else {
                console.printError("Position assignment failed: " + message);
            }
        }
    }


    /**
     * Called by PositionResponseMessage to handle position selection response
     */
    // public void triggerPositionSelectionRefresh() {
    //     if (state == TuiState.CHOOSE_START_POSITION) {
    //         // If we're still in position selection state, the selection failed
    //         if (socketClient != null) {
    //             try {
    //                 socketClient.requestAvailablePositions();
    //             } catch (IOException e) {
    //                 console.printError("Error refreshing available positions: " + e.getMessage());
    //             }
    //         }
    //     }

        // Check if we can now proceed to the next state
        // When connected to server, this method is typically called after successful position selection
        // When offline, use controller logic to check
        //boolean canProceedToNext = (socketClient != null) ?
            //true : // Server will handle state transitions
            //(controller.canProceedToRunningState() && state == TuiState.CHOOSE_START_POSITION);


//        if (canProceedToNext && state == TuiState.CHOOSE_START_POSITION) {
//            console.printMessage("Position selected. Waiting for other players...");
//            state = TuiState.WAIT_FOR_ALL_PLAYERS_POSITIONED;

//    }

    /**
     * Called by network messages to update available positions display
     */

    public void showAvailablePositions(List<Integer> positions) {
        this.availablePositions = positions;
        if (positions == null || positions.isEmpty()) {
            console.printMessage("No positions available currently.");
        } else {
            console.printMessage("Available positions: " + positions);
        }
    }
    
    /**
     * Called by PositionUpdateMessage to update a player's position
     */
//     public void updatePlayerPosition(int playerId, int position) {
//         console.printMessage("Player ID " + playerId + " has selected position " + position);
        
//         // When connected to server, check if we're the last player to select a position
//         if (socketClient != null) {
//             try {
//                 socketClient.requestAvailablePositions();
//             } catch (IOException e) {
//                 console.printError("Error checking remaining positions: " + e.getMessage());
//             }
//         } else {
//             // Offline mode - use controller to update position
//             //boolean success = controller.updatePlayerPosition(playerId, position);
            
// //            if (!success) {
// //                console.printError("Failed to update position for player ID " + playerId);
//             //}
//         }
//     }
    
    /**
     * Display a message in the console
     */
    public void showMessage(String message) {
        console.printMessage(message);
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * Check if TUI is currently in ship building state
     * Used by ShipBuildingHandler to know when to exit
     */
    public boolean isInShipBuildingState() {
        return state == TuiState.SHIP_BUILDING;
    }

    @Override
    public void componentPlaced(Component component, Position position) {
        synchronized (displayMutex) {
            if (currentPlayer != null && currentPlayer.getShip() != null) {
                boolean placed = currentPlayer.getShip().placeComponent(component, position);
                if (placed) {
                    console.printMessage("✓ Component " + component.getClass().getSimpleName() + 
                                    " placed at (" + position.getX() + "," + position.getY() + ")");
                } else {
                    console.printError("✗ Failed to place component locally");
                }
            }
            
            // Refresh display and clear current component
            if (state == TuiState.SHIP_BUILDING && activeShipBuildingHandler != null) {
                activeShipBuildingHandler.componentPlacedSuccessfully();
                activeShipBuildingHandler.refreshDisplay();
            }
        }
    }

    @Override
    public void componentPlacedFailed(String errorMessage) {

    }

    @Override
    public void componentRemoved(Position position) {
        synchronized (displayMutex) {
            if (currentPlayer != null && currentPlayer.getShip() != null) {
                Component removed = currentPlayer.getShip().getComponentAt(position);
                if (removed != null) {
                    currentPlayer.getShip().removeComponent(removed);
                    console.printMessage("✓ Component removed from (" + position.getX() + "," + position.getY() + ")");
                }
            }
            
            if (state == TuiState.SHIP_BUILDING && activeShipBuildingHandler != null) {
                activeShipBuildingHandler.refreshDisplay();
            }
        }
    }

    @Override
    public void componentRemovedFailed(String errorMessage) {

    }

    @Override
    public void componentRotated(int playerId, Position position, RotationType rotationType, boolean isValid) {
        if (!isValid || currentPlayer == null || currentPlayer.getId() != playerId) 
            return;

        ShipBuilder ship = currentPlayer.getShip();
        Component c = ship.getComponentAt(position);
        if (c != null) {
            switch (rotationType) {
                case CLOCKWISE -> ship.rotateComponentOfShipClockwise(c);
                case ANTI_CLOCKWISE -> ship.rotateComponentOfShipCounterClockwise(c);
                case OPPOSITE -> ship.rotateComponentOpposite(c);
            }
            console.printMessage("✓ Component at (" + position.getX() + "," + position.getY() + ") rotated " + rotationType);
            
            if (state == TuiState.SHIP_BUILDING && activeShipBuildingHandler != null) {
                activeShipBuildingHandler.refreshDisplay();
            }
        }
    }


    @Override
    public void componentRotatedFailed(String errorMessage) {

    }

    public void componentRotated(Position position, RotationType rotationType) {
        synchronized (displayMutex) {
            console.printMessage("Component at (" + position.getX() + "," + position.getY() + 
                               ") rotated " + rotationType);
        }
    }

    @Override
    public void shipValidated(boolean isValid, String message) {
        synchronized (displayMutex) {
            if (isValid) {
                console.printMessage("✓ " + message);
                console.printMessage("✓ Ship validation successful! Moving to flight preparation...");
                // Transition to flight preparation instead of waiting for positions
                if (state == TuiState.SHIP_BUILDING) {
                    state = TuiState.PREPARATION_FLIGHT;
                }
            } else {
                console.printError("✗ Ship validation failed: " + message);
                console.printMessage("Please fix the issues and try validating again...");
                // Stay in ship building state and refresh the handler
                if (activeShipBuildingHandler != null) {
                    activeShipBuildingHandler.refreshMenuDisplay();
                }
            }
        }
    }

    @Override
    public void componentPicked(int playerId, Component component, boolean success, String message) {
        synchronized (displayMutex) {
            if (currentPlayer != null && currentPlayer.getId() == playerId) {
                if (success && component != null) {
                    console.printMessage("✓ Component picked: " + component.getClass().getSimpleName());
                    if (activeShipBuildingHandler != null) {
                        activeShipBuildingHandler.setCurrentComponent(component);
                        activeShipBuildingHandler.displayCurrentComponent();
                        activeShipBuildingHandler.refreshMenuDisplay();
                    }
                } else {
                    console.printError("✗ " + message);
                    if (activeShipBuildingHandler != null) {
                        activeShipBuildingHandler.refreshMenuDisplay();
                    }
                }
            }
        }
    }

    @Override
    public void componentPickFailed(String errorMessage) {

    }

    @Override
    public void shipValidationFailed(String errorMessage) {

    }

    @Override
    public void playerCreated(int playerId, String username) {
        this.currentPlayer = new Player(playerId, username, true);

        //console.printMessage("Player created successfully: " + username + " (ID: " + playerId + ")");
        lobbyHandler = new LobbyHandler(console, client, playerId, currentPlayer.getNickName());
        playerCreationFuture.complete(currentPlayer);
    }

    @Override
    public void usernameRejected(String errorMessage) {
        console.printError(errorMessage);

        if (playerCreationFuture != null && !playerCreationFuture.isDone()) {
            playerCreationFuture.completeExceptionally(new RuntimeException("Username rejected: " + errorMessage));
        }

    }

    public void matchCreated(UUID matchId, String matchName, String playerName, int playerId, Difficulty difficulty) {
        lobbyHandler.matchCreatedCallback(matchName, matchId, true, playerName, playerId);
        this.state = TuiState.WAIT_IN_MATCH;
    }
    
    public void updateAvailableMatches(List<String> matches) {
        lobbyHandler.setAvailableMatches(matches);
    }

    @Override
    public void updateAvailableMatchesFailed(String errorMessage) {

    }

    @Override
    public void playerJoinedMatch(UUID matchId, String playerName) {
        lobbyHandler.playerJoinedMatchCallback(matchId, playerName);

        if (playerName.equals(currentPlayer.getNickName()) && state == TuiState.LOBBY_MENU) {
            state = TuiState.WAIT_IN_MATCH;
        }
    }

    @Override
    public void playerJoinedMatchFailed(String errorMessage) {

    }

    @Override
    public void playerLeftMatch(UUID matchId, int playerId, String playerName) {
        lobbyHandler.playerLeftMatchCallback(matchId, playerId, playerName);
    }

    @Override
    public void playerLeftMatchFailed(String errorMessage) {

    }

    @Override
    public void updateHost(UUID matchId, int playerId, String playerName) {
        lobbyHandler.updateHostCallback(matchId, playerId, playerName);
    }

    @Override
    public void updateHostFailed(String errorMessage) {

    }

    @Override
    public void matchStarted(UUID matchId, String username) {
        lobbyHandler.matchStartedCallback(matchId, username);   
        this.state = TuiState.LOADING;

    }

    @Override
    public void matchStartFailed(String errorMessage) {

    }

    @Override
    public void loadingAckResponseFailed(String errorMessage) {

    }

    @Override
    public void playersInMatch(UUID matchId, List<String> playerNames, int hostId) {
        lobbyHandler.playersInMatchCallback(matchId, playerNames, hostId);
    }

    @Override
    public void playersInMatchFailed(String errorMessage) {

    }


    @Override
    public void loadingStatus(UUID matchId, int playersReady, int totalPlayers) {
        lobbyHandler.loadingStatusCallback(matchId, playersReady, totalPlayers);
        this.state = TuiState.WAITING_FOR_PLAYERS;
    }

    @Override
    public void loadingStatusFailed(String errorMessage) {

    }

    @Override
    public void playerReady(UUID matchId, int readyPlayers, int totalPlayers) {
        lobbyHandler.playerReadyCallback(matchId, readyPlayers, totalPlayers);
    }

    @Override
    public void playerReadyFailed(String errorMessage) {

    }

    @Override
    public void shipBuildingStart(UUID matchId, int readyPlayers) {
        synchronized (displayMutex) {
            lobbyHandler.shipBuildingStartCallback(matchId, readyPlayers);
            
            // Add a small delay to allow any pending console output to complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            Timer timer = new Timer(); // Create a new timer

            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    //System.out.println("Timer expired! Notification sent.");
                    if (TuiState.SHIP_BUILDING.equals(state)) {
                        console.printMessage("Time expired, you will get a penalty");
                        try {
                            client.sendTimeExpiredNotification(username);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }

                }
            };

            timer.schedule(task, PENALTY_TIMEOUT);

            console.printMessage("You are going to have 1:30 minute time at your disposal");

            this.state = TuiState.SHIP_BUILDING;
        }
    }

    @Override
    public void shipBuildingStartFailed(String errorMessage) {

    }



//    public void showAvailablePositions(List<Integer> positions) {
//        // TODO Auto-generated method stub
//        throw new UnsupportedOperationException("Unimplemented method 'showAvailablePositions'");
//    }

    @Override
    public void adventureCardPicked(int playerId, it.polimi.ingsw.AdventureCards.AdventureCard adventureCard, boolean success, String message, int nextPlayerTurn, Map<Integer, Integer> updatedPositions) {
        synchronized (displayMutex) {
            if (success && adventureCard != null) {
                // Find player name for display
                String playerName = (currentPlayer != null && currentPlayer.getId() == playerId) ? 
                    "You" : "Player " + playerId;
                
                console.printMessage("✓ The leading player " + playerName + " picked adventure card: " + adventureCard.getName());
                
                // Show detailed card information for everyone
                displayAdventureCard(adventureCard);
                // Note: The actual card effect is applied on the server side
                console.printMessage("Adventure card effects have been applied to the game state.");
                
                // Display updated positions if provided
                if (updatedPositions != null && !updatedPositions.isEmpty()) {
                    Map<Integer, Integer> previousPositions = new HashMap<>(currentPlayerPositions);
                    currentPlayerPositions = new HashMap<>(updatedPositions); // Make a defensive copy
                    displayPositionUpdates(previousPositions, currentPlayerPositions);
                }
                
                // Check if there are more adventure cards to pick
                if (nextPlayerTurn == -2) { // Special value indicating deck is empty or game ended
                    console.printMessage("Adventure card deck is empty. Waiting for game completion notification...");
                } else {
                    console.printMessage("Waiting for the new leading player to pick the next adventure card...");
                    console.printMessage("(The leader may have changed due to card effects)");
                }
            } else {
                console.printError("✗ Failed to pick adventure card: " + message);
            }
        }
    }

    /**
     * Display position changes after adventure card effects
     */
    private void displayPositionUpdates(Map<Integer, Integer> previousPositions, Map<Integer, Integer> currentPositions) {
        console.printMessage("\n=== FLIGHT BOARD POSITIONS (0-23, higher = better) ===");
        
        // Track if any position actually changed
        boolean hasChanges = false;
        List<String> changeDetails = new ArrayList<>();
        
        // Find all players and their positions
        Map<Integer, String> playerNames = new HashMap<>(); // position -> player names at that position
        for (Map.Entry<Integer, Integer> entry : currentPositions.entrySet()) {
            int playerId = entry.getKey();
            int position = entry.getValue();
            
            String playerName = (currentPlayer != null && currentPlayer.getId() == playerId) ? 
                "You" : "Player " + playerId;
            
            // Check if position changed
            Integer previousPos = previousPositions.get(playerId);
            if (previousPos != null && !previousPos.equals(position)) {
                hasChanges = true;
                if (position > previousPos) {
                    // Higher position number = better position = moved forward
                    int improvement = position - previousPos;
                    playerName += " (↑" + improvement + ")";
                    changeDetails.add(playerName.replace(" (↑" + improvement + ")", "") + " moved forward " + improvement + " positions");
                } else {
                    // Lower position number = worse position = moved backward
                    int decline = previousPos - position;
                    playerName += " (↓" + decline + ")";
                    changeDetails.add(playerName.replace(" (↓" + decline + ")", "") + " moved backward " + decline + " positions");
                }
            } else if (previousPos == null) {
                // Player wasn't tracked before - this shouldn't happen during adventure phase
                changeDetails.add(playerName + " position tracking started");
            }
            
            playerNames.put(position, playerNames.getOrDefault(position, "") + 
                (playerNames.containsKey(position) ? ", " : "") + playerName);
        }
        
        // Display positions from best to worst (highest to lowest position numbers)
        List<Integer> sortedPositions = new ArrayList<>(playerNames.keySet());
        sortedPositions.sort(Collections.reverseOrder()); // Descending order (23, 22, 21, ... 0)
        
        // Create rank based on the sorted order
        int rank = 1;
        for (int position : sortedPositions) {
            String playersAtPosition = playerNames.get(position);
            String positionLabel = getPositionLabel(rank);
            console.printMessage("Position " + position + " " + positionLabel + ": " + playersAtPosition);
            rank++;
        }
        
        // Show position changes summary
        if (hasChanges) {
            console.printMessage("\nPosition changes from this card:");
            for (String change : changeDetails) {
                console.printMessage("  • " + change);
            }
        } else {
            // Check if positions are identical
            boolean identicalMaps = previousPositions.equals(currentPositions);
            if (identicalMaps) {
                console.printMessage("(No position changes from this card)");
            } else {
                console.printMessage("(Position data updated but no player movements detected)");
            }
        }
        
        console.printMessage("================================================\n");
    }

    /**
     * Get a descriptive label for a rank
     */
    private String getPositionLabel(int rank) {
        switch (rank) {
            case 1: return "(1st place)";
            case 2: return "(2nd place)";
            case 3: return "(3rd place)";
            case 4: return "(4th place)";
            default: return "(" + rank + "th place)";
        }
    }

    @Override
    public void addTokenToComponent(String playerName, Position position, TokenType tokenType, Boolean success, String errorMessage) {
        if (preparationFlightHandler != null && playerName.equals(currentPlayer.getNickName())) {
            preparationFlightHandler.tokenPlaced(position, tokenType, success);
        }
    }

    @Override
    public void getPlayerTokenList(String playerName, Map<Position, List<TokenType>> tokens) {
        if (preparationFlightHandler != null && playerName.equals(currentPlayer.getNickName())) {
            preparationFlightHandler.updateTokens(tokens);
        }
    }

    @Override
    public void removeTokenFromComponent(String playerName, Position position, TokenType tokenType, boolean success, String errorMessage) {
        if (preparationFlightHandler != null && playerName.equals(currentPlayer.getNickName())) {
            preparationFlightHandler.tokenRemoved(position, tokenType, success);
        }
    }

    @Override
    public void getPlayerShipResponse(String playerName, Map<Position, Component> shipComponents) {
        if (preparationFlightHandler != null) {
            if (playerName.equals(currentPlayer.getNickName())) {
                preparationFlightHandler.updateShipComponents(shipComponents);
            } else {
                preparationFlightHandler.displayOtherPlayerShip(playerName, shipComponents);
            }
        }
    }

    @Override
    public void adventurePhaseComplete(UUID matchId, boolean gameComplete, String completionReason) {
        synchronized (displayMutex) {
            console.printMessage("\n=== ADVENTURE PHASE COMPLETE ===");
            console.printMessage("Reason: " + completionReason);
            
            if (gameComplete) {
                console.printMessage(" GAME OVER! ");
                console.printMessage("Final results will be calculated...");
                state = TuiState.GAME_ENDED;
            } else {
                console.printMessage("Adventure phase finished. Proceeding to main game...");
                state = TuiState.RUNNING;
            }
        }
    }

    @Override
    public void FlightOrderpositionRecieved(UUID matchId, int playerId, List<Player> PlayerIdPosMap, boolean success) {

    }

    /**
     * Displays detailed information about an adventure card
     */
    private void displayAdventureCard(it.polimi.ingsw.AdventureCards.AdventureCard adventureCard) {
        console.printMessage("\n╔══════════════════════════════════════════════════════════════════════╗");
        console.printMessage("║                        ADVENTURE CARD DRAWN                         ║");
        console.printMessage("╠══════════════════════════════════════════════════════════════════════╣");
        console.printMessage("║ Card Name: " + String.format("%-55s", adventureCard.getName()) + " ║");
        console.printMessage("║                                                                      ║");

        // Split description into multiple lines if it's too long
        String description = adventureCard.getDescription();
        String[] descriptionLines = wrapText(description, 68);
        console.printMessage("║ Description:                                                         ║");
        for (String line : descriptionLines) {
            console.printMessage("║ " + String.format("%-68s", line) + " ║");
        }

        console.printMessage("║                                                                      ║");

        // Show additional properties if available
        if (adventureCard.getDuration() > 1) {
            console.printMessage("║ Duration: " + String.format("%-59s", adventureCard.getDuration() + " rounds") + " ║");
        }
        if (adventureCard.getCost() > 0) {
            console.printMessage("║ Cost: " + String.format("%-63s", adventureCard.getCost() + " credits") + " ║");
        }

        console.printMessage("╚══════════════════════════════════════════════════════════════════════╝");
        console.printMessage("");
    }

    /**
     * Wraps text to fit within specified width
     */
    private String[] wrapText(String text, int maxWidth) {
        if (text.length() <= maxWidth) {
            return new String[]{text};
        }

        java.util.List<String> lines = new java.util.ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            if (currentLine.length() + word.length() + 1 <= maxWidth) {
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            } else {
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                } else {
                    // Word is longer than maxWidth, just add it anyway
                    lines.add(word);
                }
            }
        }

        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        return lines.toArray(new String[0]);
    }

    @Override
    public void cabinColorSelected(int playerId, String color) {

    }

    @Override
    public void cabinColorSelectionFailed(String errorMsg) {

    }

    @Override
    public void flightPreparationStart(UUID matchId) {
        synchronized (displayMutex) {
            console.printMessage("\n=== FLIGHT PREPARATION PHASE ===");
            this.currentMatchId = matchId;          // salva l’ID della partita
            this.state = TuiState.PREPARATION_FLIGHT;
        }
    } 

    @Override
    public void flightPreparationStartFailed(String errorMessage) {

    }

    @Override
    public void playerReadyForFlight(UUID matchId, int playerId, String playerName, boolean shipValid) {

    }

    @Override
    public void playerReadyForFlightFailed(String errorMessage) {

    }

    @Override
    public void adventurePhaseStarted(UUID matchId, Map<Integer, Integer> playerPositions, int firstPlayerToAct) {
        synchronized (displayMutex) {
            // Initialize position tracking
            currentPlayerPositions = new HashMap<>(playerPositions);
            
            console.printMessage("\nALL PLAYERS POSITIONED! Starting Adventure Phase!");
            console.printMessage("=== STARTING POSITIONS (0-23, lower = better for winning) ===");
            
            // Sort players by position (best to worst for flight race)
            List<Map.Entry<Integer, Integer>> sortedEntries = new ArrayList<>(playerPositions.entrySet());
            sortedEntries.sort(Map.Entry.comparingByValue()); // Sort by position value (ascending)
            
            // Display flight race standings
            int rank = 1;
            for (Map.Entry<Integer, Integer> entry : sortedEntries) {
                int playerId = entry.getKey();
                int position = entry.getValue();
                String playerName = (currentPlayer != null && currentPlayer.getId() == playerId) ? 
                    "You" : "Player " + playerId;
                
                String rankStr = getRankString(rank);
                console.printMessage(rankStr + " in flight race (pos " + position + "): " + playerName);
                rank++;
            }
            
            // Show who picks adventure cards (player with highest position number = best flight position)
            String cardPickerName = (currentPlayer != null && currentPlayer.getId() == firstPlayerToAct) ? 
                "You" : "Player " + firstPlayerToAct;
            
            // Find the position of the card picker
            int cardPickerPosition = playerPositions.getOrDefault(firstPlayerToAct, -1);
            
            console.printMessage("\n ADVENTURE CARD PICKER: " + cardPickerName + " (position " + cardPickerPosition + ")");
            
            console.printMessage("\n=============================================");
            console.printMessage("ADVENTURE CARD RULES:");
            console.printMessage("• LEADING player (highest position number) picks cards!");
            console.printMessage("• Flight day costs move you to LOWER position numbers (worse positions)");
            console.printMessage("• Multiple players losing flight days move in reverse order");
            console.printMessage("• Players cannot overlap - if a position is occupied, move to next available position");
            
            // Check if we are the card picker
            if (currentPlayer != null && currentPlayer.getId() == firstPlayerToAct) {
                console.printMessage("\n You are the adventure card picker! Type 'pick' to draw a card for all players.");
            } else {
                console.printMessage("\n Waiting for " + cardPickerName + " to pick an adventure card...");
            }
            
            // Transition to adventure phase
            state = TuiState.ADVENTURE_PHASE;
        }
    }

    /**
     * Convert rank number to ordinal string
     */
    private String getRankString(int rank) {
        switch (rank) {
            case 1: return "1st";
            case 2: return "2nd";
            case 3: return "3rd";
            case 4: return "4th";
            default: return rank + "th";
        }
    }

    @Override
    public void playerReadyForAdventurePhase(UUID matchId, int playerId, boolean success, String message) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'playerReadyForAdventurePhase'");
    }

    @Override
    public void adventurePhaseReady(UUID matchId, String message) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'adventurePhaseReady'");
    }

}
