package it.polimi.ingsw.view.tui.components;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import it.polimi.ingsw.Player;
import it.polimi.ingsw.Position;
import it.polimi.ingsw.Components.Component;
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
                case CHOOSE_START_POSITION -> handleChooseStartPositionState();
                case WAIT_FOR_ALL_PLAYERS_POSITIONED -> handleWaitForAllPlayersPositionedState();
                case ADVENTURE_PHASE      -> handleAdventurePhaseState();
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
                client = RmiClient.connectTo(serverAddress, this);
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
            console.printError("Errore nella creazione del giocatore: " + e.getCause());
            state = TuiState.ASK_USERNAME;
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

        state = TuiState.WAIT_FOR_CALLBACK_PLAYER;

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

/*     public void handlePlayerReady() {
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
    } */

    private void handleShipBuildingState() {
        if (this.currentPlayer == null) {
            console.printError("Cannot start ship building: current player not set.");
            state = TuiState.ASK_USERNAME;
            return;
        }
        activeShipBuildingHandler = new ShipBuildingHandler(console, client, this.currentPlayer);
        activeShipBuildingHandler.manageShipBuilding();
        activeShipBuildingHandler = null;

        console.printMessage("Ship building phase completed.");
        
        // If we're still in SHIP_BUILDING state (e.g., due to DEBUGSKIP or quit),
        // transition to the next appropriate state
        if (state == TuiState.SHIP_BUILDING) {
            console.printMessage("Transitioning to position assignment...");
            // Don't assign position locally - let the server handle it based on completion order
            console.printMessage("Waiting for position assignment based on completion order...");
            state = TuiState.CHOOSE_START_POSITION;
        }
        
        // Note: For normal validation, state transition to CHOOSE_START_POSITION 
        // happens in shipValidated() callback when the server confirms the ship is valid
    }

    private void handleChooseStartPositionState() {
        console.printMessage("\n=== Starting Position Assignment ===");
        console.printMessage("Position assignment is based on ship building completion order:");
        console.printMessage("- First player to finish gets position 1");
        console.printMessage("- Second player to finish gets position 2");
        console.printMessage("- And so on...");
        console.printMessage("Your position will be assigned automatically by the server...");
        
        // For debug/offline mode where there's no server, simulate position assignment
        if (client == null || connectionType.equals("debug")) {
            console.printMessage("✓ [DEBUG] You have been assigned starting position: 1");
            console.printMessage("All players positioned. Proceeding to game...");
            state = TuiState.RUNNING;
        } else {
            // In online mode, wait for server to assign position
            console.printMessage("Waiting for server to assign your position...");
            state = TuiState.WAIT_FOR_ALL_PLAYERS_POSITIONED;
        }
    }

    private void handleWaitForAllPlayersPositionedState() {
        console.printMessage("\n=== WAITING FOR ALL PLAYERS ===");
        console.printMessage("Your ship has been validated and you have been assigned a position.");
        console.printMessage("Waiting for other players to complete ship building...");
        console.printMessage("Adventure cards will be picked once all players are ready.");
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
                        }
                        console.printMessage("Once all players are positioned, the adventure card phase will begin.");
                        break;
                    case "help":
                        console.printMessage("Available commands:");
                        console.printMessage("  status - Show current game status");
                        console.printMessage("  skip   - Skip waiting (debug only)");
                        console.printMessage("  quit   - Exit the game");
                        break;
                    case "skip":
                        console.printMessage("[DEBUG] Skipping position assignment wait. Proceeding to adventure phase...");
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

    private void handleAdventurePhaseState() {
        console.printMessage("\n=== ADVENTURE CARD PHASE ===");
        console.printMessage("Players pick adventure cards in position order (1st, 2nd, 3rd, 4th)");
        console.printMessage("Wait for your turn or type 'status' to check current state");
        
        while (state == TuiState.ADVENTURE_PHASE) {
            String cmd = console.pollCommand(1000); // Poll with timeout
            
            if (cmd != null) {
                cmd = cmd.trim().toLowerCase();
                switch (cmd) {
                    case "pick":
                        // Try to pick an adventure card
                        try {
                            client.pickAdventureCard();
                            console.printMessage("Adventure card pick request sent. Waiting for response...");
                        } catch (Exception e) {
                            console.printError("Error picking adventure card: " + e.getMessage());
                        }
                        break;
                    case "status":
                        if (currentPlayer != null) {
                            console.printMessage("Player: " + currentPlayer.getNickName() + " (ID: " + currentPlayer.getId() + ")");
                            console.printMessage("Game state: Adventure Phase - picking cards in position order");
                        }
                        break;
                    case "help":
                        console.printMessage("Available commands:");
                        console.printMessage("  pick   - Pick an adventure card (only when it's your turn)");
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

    private void handleRunningState() {
        console.printMessage("\n=== GAME STARTED ===");
        console.printMessage("The game is now running!");
        console.printMessage("Type 'help' for available commands, 'quit' to exit");
        
        while (state == TuiState.RUNNING) {
            String cmd = console.readCommand();
            if (cmd == null) continue;
            
            cmd = cmd.trim().toLowerCase();
            switch (cmd) {
                case "help":
                    console.printMessage("Available commands:");
                    console.printMessage("  status - Show game status");
                    console.printMessage("  quit   - Exit the game");
                    break;
                case "status":
                    if (currentPlayer != null) {
                        console.printMessage("Player: " + currentPlayer.getNickName() + " (ID: " + currentPlayer.getId() + ")");
                        console.printMessage("Game state: Running");
                    }
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

    public boolean notifyNewPlayerPosition(int playerId, int position) {
        //TODO implement actual method
        return true;
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
    @Override
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

    @Override
    public void componentPlaced(Component component, Position position) {
        synchronized (displayMutex) {
            if (currentPlayer != null && currentPlayer.getShip() != null) {
                boolean placed = currentPlayer.getShip().placeComponent(component, position);
                // Note: Success message is handled by PlaceComponentResponse
                // Only update the ship model here, display refresh will be called separately
                if (!placed) {
                    console.printError("✗ Failed to place component " + component.getClass().getSimpleName() + 
                                     " at (" + position.getX() + "," + position.getY() + ") locally");
                }
            } else {
                console.printError("✗ Cannot place component: player or ship not available");
            }
            
            // Show menu again after component placement if we're in ship building mode
            if (state == TuiState.SHIP_BUILDING && activeShipBuildingHandler != null) {
                activeShipBuildingHandler.refreshMenuDisplay();
            }
        }
    }

    @Override
    public void componentRemoved(Position position) {
        synchronized (displayMutex) {
            if (currentPlayer != null && currentPlayer.getShip() != null) {
                Component removed = currentPlayer.getShip().getComponentAt(position);
                if (removed != null) {
                    currentPlayer.getShip().removeComponent(removed);
                    console.printMessage("Component removed from (" + position.getX() + "," + position.getY() + ")");
                }
            }
            
            // Show menu again after component removal if we're in ship building mode
            if (state == TuiState.SHIP_BUILDING && activeShipBuildingHandler != null) {
                activeShipBuildingHandler.refreshMenuDisplay();
            }
        }
    }

    @Override
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
                if (state == TuiState.SHIP_BUILDING) {
                    console.printMessage("Ship building complete. Position assigned. Waiting for all players to finish...");
                    state = TuiState.WAIT_FOR_ALL_PLAYERS_POSITIONED; // Wait for all players
                } else if (state == TuiState.WAIT_FOR_ALL_PLAYERS_POSITIONED) {
                    console.printMessage("Position assigned. Waiting for all players to finish...");
                    // Stay in the same state - wait for adventure phase start message
                }
            } else {
                console.printError("✗ " + message);
                // If validation failed, go back to ship building
                if (state == TuiState.WAIT_FOR_ALL_PLAYERS_POSITIONED) {
                    console.printMessage("Returning to ship building...");
                    state = TuiState.SHIP_BUILDING;
                }
            }
        }
    }

    @Override
    public void playerCreated(int playerId, String username) {
        this.currentPlayer = new Player(playerId, username, true);
        
        // Set the local player in the client so it can track the player ID for requests
        try {
            client.setLocalPlayer(currentPlayer);
        } catch (Exception e) {
            console.printError("Failed to set local player in client: " + e.getMessage());
        }
        
        //console.printMessage("Player created successfully: " + username + " (ID: " + playerId + ")");
        lobbyHandler = new LobbyHandler(console, client, playerId, currentPlayer.getNickName());
        playerCreationFuture.complete(currentPlayer);
    }

    @Override
    public void usernameRejected(String errorMessage) {
        console.printError(errorMessage);
        state = TuiState.ASK_USERNAME;
    }

    @Override
    public void matchCreated(UUID matchId, String matchName, String playerName, int playerId) {
        lobbyHandler.matchCreatedCallback(matchName, matchId, true, playerName, playerId);
        this.state = TuiState.WAIT_IN_MATCH;
    }

    @Override
    public void updateAvailableMatches(List<String> matches) {
        lobbyHandler.setAvailableMatches(matches);
    }

    @Override
    public void playerJoinedMatch(UUID matchId, String playerName) {
        lobbyHandler.playerJoinedMatchCallback(matchId, playerName);

        if (playerName.equals(currentPlayer.getNickName()) && state == TuiState.LOBBY_MENU) {
            state = TuiState.WAIT_IN_MATCH;
        }
    }

    @Override
    public void playerLeftMatch(UUID matchId, int playerId, String playerName) {
        lobbyHandler.playerLeftMatchCallback(matchId, playerId, playerName);
    }

    @Override
    public void updateHost(UUID matchId, int playerId, String playerName) {
        lobbyHandler.updateHostCallback(matchId, playerId, playerName);
    }

    @Override
    public void matchStarted(UUID matchId, String username) {
        lobbyHandler.matchStartedCallback(matchId, username);   
        this.state = TuiState.LOADING;

    }

    @Override
    public void playersInMatch(UUID matchId, List<String> playerNames, int hostId) {
        lobbyHandler.playersInMatchCallback(matchId, playerNames, hostId);
    }


    @Override
    public void loadingStatus(UUID matchId, int playersReady, int totalPlayers) {
        lobbyHandler.loadingStatusCallback(matchId, playersReady, totalPlayers);
        this.state = TuiState.WAITING_FOR_PLAYERS;
    }

    @Override
    public void playerReady(UUID matchId, int readyPlayers, int totalPlayers) {
        lobbyHandler.playerReadyCallback(matchId, readyPlayers, totalPlayers);
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
            
            this.state = TuiState.SHIP_BUILDING;
        }
    }


    @Override
    public void componentPicked(int playerId, Component component, boolean success, String message) {
        synchronized (displayMutex) {
            // Check if this response is for the current player
            if (currentPlayer != null && currentPlayer.getId() == playerId) {
                if (success && component != null) {
                    console.printMessage("✓ Component picked: " + component.getClass().getSimpleName());
                    // Pass the component to the ship building handler if it's active
                    if (activeShipBuildingHandler != null) {
                        activeShipBuildingHandler.setCurrentComponent(component);
                        // Show menu again after component pick if we're in ship building mode
                        if (state == TuiState.SHIP_BUILDING) {
                            activeShipBuildingHandler.refreshMenuDisplay();
                        }
                    }
                } else {
                    console.printError("✗ Failed to pick component: " + message);
                    // Still show menu after failed component pick
                    if (state == TuiState.SHIP_BUILDING && activeShipBuildingHandler != null) {
                        activeShipBuildingHandler.refreshMenuDisplay();
                    }
                }
            }
            // If it's not for our player, ignore the response
        }
    }

    @Override
    public void adventureCardPicked(int playerId, it.polimi.ingsw.AdventureCards.AdventureCard adventureCard, boolean success, String message, int nextPlayerTurn) {
        synchronized (displayMutex) {
            console.printMessage("[DEBUG] Adventure card response received: playerId=" + playerId + ", success=" + success + ", message=" + message + ", adventureCard=" + (adventureCard != null ? adventureCard.getClass().getSimpleName() : "null"));
            
            if (success && adventureCard != null) {
                // Find player name for display
                String playerName = (currentPlayer != null && currentPlayer.getId() == playerId) ? 
                    "You" : "Player " + playerId;
                
                console.printMessage("✓ " + playerName + " picked adventure card: " + adventureCard.getName());
                
                // Show detailed card information if it's the current player's card
                if (currentPlayer != null && currentPlayer.getId() == playerId) {
                    displayAdventureCard(adventureCard);
                    adventureCard.effect();
                } else {
                    // For other players, just show basic info
                    console.printMessage("Description: " + adventureCard.getDescription());
                }
                
                // Check if there's a next player or if phase is complete
                if (nextPlayerTurn == -1) {
                    console.printMessage("All players have picked their adventure cards. Proceeding to main game...");
                    state = TuiState.RUNNING;
                } else {
                    if (currentPlayer != null && currentPlayer.getId() == nextPlayerTurn) {
                        console.printMessage("It's your turn to pick an adventure card! Type 'pick' to draw a card.");
                    } else {
                        console.printMessage("Waiting for Player " + nextPlayerTurn + " to pick their adventure card...");
                    }
                }
            } else {
                console.printError("✗ Failed to pick adventure card: " + message);
            }
        }
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
    public void adventurePhaseStarted(java.util.UUID matchId, java.util.Map<Integer, Integer> playerPositions, int firstPlayerToAct) {
        synchronized (displayMutex) {
            console.printMessage("\n=== ALL PLAYERS POSITIONED ===");
            console.printMessage("Final positions:");
            
            // Display all player positions
            for (java.util.Map.Entry<Integer, Integer> entry : playerPositions.entrySet()) {
                int playerId = entry.getKey();
                int position = entry.getValue();
                String playerName = (currentPlayer != null && currentPlayer.getId() == playerId) ? 
                    "You" : "Player " + playerId;
                console.printMessage("  " + playerName + " is in position " + position);
            }
            
            console.printMessage("\nStarting adventure card phase...");
            
            // Check if current player is first to act
            if (currentPlayer != null && currentPlayer.getId() == firstPlayerToAct) {
                console.printMessage("You are in position 1! It's your turn to pick the first adventure card. Type 'pick' to draw a card.");
            } else {
                console.printMessage("Player " + firstPlayerToAct + " (position 1) will pick the first adventure card. Please wait for your turn.");
            }
            
            // Transition to adventure phase
            state = TuiState.ADVENTURE_PHASE;
        }
    }


}
