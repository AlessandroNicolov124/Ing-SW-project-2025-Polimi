package it.polimi.ingsw.view.tui.components;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import it.polimi.ingsw.Player;
import it.polimi.ingsw.Position;
import it.polimi.ingsw.Components.Component;
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
        
        // If we're still in SHIP_BUILDING state after handler completes (e.g., due to DEBUGSKIP),
        // provide a fallback transition to waiting state for debugging
        if (state == TuiState.SHIP_BUILDING) {
            console.printMessage("[DEBUG] Ship building ended without validation. Transitioning to waiting state...");
            console.printMessage("[DEBUG] Note: Position assignment may happen when validation response arrives.");
            state = TuiState.WAIT_FOR_ALL_PLAYERS_POSITIONED;
        }
        
        // Normal state transitions are handled by the server's position assignment system
        // via shipValidated() and positionAssigned() callbacks
    }

//     private void handleChooseStartPositionState() {
//         if (this.currentPlayer == null) {
//             console.printError("Cannot choose starting position: current player not set.");
//             state = TuiState.ASK_USERNAME;
//             return;
//         }

//         console.printMessage("\n=== Choose Your Starting Position ===");

//         try {
//             // Request available positions from server 
//             if (socketClient != null) {
//                 socketClient.requestAvailablePositions();
//                 console.printMessage("Requesting available positions from server...");
//             } else {
//                 // Fall back to local positions if no socket client
//                 //availablePositions = client.getAvailableStartingPositions();
//                 showAvailablePositions(availablePositions);
//             }
//         } catch (IOException e) {
//             console.printError("Error requesting available positions: " + e.getMessage());
//             state = TuiState.TERMINATED;
//             return;
//         }

//         boolean positionChosen = false;
//         while (!positionChosen) {
//             if (availablePositions == null || availablePositions.isEmpty()) {
//                 console.printMessage("Waiting for server to provide available positions...");
//                 try {
//                     Thread.sleep(1000);
//                 } catch (InterruptedException e) {
//                     Thread.currentThread().interrupt();
//                 }
//                 continue;
//             }

//             String input = console.askQuestion("Enter your desired starting position " + availablePositions + ": ");

//             try {
//                 int chosenPosition = Integer.parseInt(input);
                
//                 // Validate position selection - use local data when connected to server, controller when offline
// //                boolean isValid = (socketClient != null) ?
// //                    availablePositions.contains(chosenPosition) :
//                     //client.isPositionSelectionValid(chosenPosition);
                    
//                 if (!true) {
//                     console.printError("Position " + chosenPosition + " is not available. Please choose from: " + availablePositions);
//                     continue;
//                 }

//                 try {
//                     if (socketClient != null) {
//                         socketClient.selectPosition(chosenPosition);
//                         console.printMessage("Position request sent to server. Waiting for confirmation...");
//                         // The response will be handled by notifyPositionResponse
//                         positionChosen = true;
//                         // Don't transition state yet - wait for server confirmation
//                     } else {
//                         //TODO implemet corretly using client
//                         // Fall back to local controller if no socket client
//                         /*if (controller.handlePlayerPositionSelection(this.currentPlayer, chosenPosition)) {
//                             positionChosen = true;
//                             console.printMessage("Position " + chosenPosition + " selected. Waiting for other players...");
//                             state = TuiState.WAIT_FOR_ALL_PLAYERS_POSITIONED;
//                         } else {
//                             console.printError("Failed to set position. It might be taken or invalid. Please try again.");
//                         }*/
//                     }
//                 } catch (Exception e) {
//                     console.printError("Error selecting position: " + e.getMessage());
//                 }
//             } catch (NumberFormatException e) {
//                 console.printError("Invalid input. Please enter a number.");
//             }
//         }
//     }

    private void handleWaitForAllPlayersPositionedState() {
        console.printMessage("\n=== WAITING FOR ALL PLAYERS ===");
        console.printMessage("Your ship has been validated and you have been assigned a position!");
        console.printMessage("Positions are assigned based on completion order:");
        console.printMessage("  • 1st to finish → Position 1 (LEADING PLAYER - picks adventure card for everyone)");
        console.printMessage("  • 2nd to finish → Position 2");
        console.printMessage("  • 3rd to finish → Position 3");
        console.printMessage("  • 4th to finish → Position 4");
        console.printMessage("");
        console.printMessage("IMPORTANT: Only the leading player (position 1) will pick adventure cards!");
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
        console.printMessage("Only the leading player (position 1) can pick adventure cards for everyone");
        console.printMessage("Wait for the leader to pick or type 'status' to check current state");
        
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
    public void matchCreated(UUID matchId, String matchName, String playerName, int playerId, Difficulty difficulty) {

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
    public void componentPlacedFailed(String errorMessage) {

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
    public void componentRemovedFailed(String errorMessage) {

    }

    @Override
    public void componentRotated(int playerId, Position position, RotationType rotationType, boolean isValid) {

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
                console.printMessage("Ship building complete! Waiting for position assignment...");
                // Position assignment will be handled by positionAssigned() callback
                // State transition will happen there
            } else {
                console.printError("✗ " + message);
                // If validation failed, stay in ship building mode
                if (state == TuiState.WAIT_FOR_ALL_PLAYERS_POSITIONED) {
                    console.printMessage("Returning to ship building...");
                    state = TuiState.SHIP_BUILDING;
                }
            }
        }
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


    public void matchCreated(UUID matchId, String matchName, String playerName, int playerId) {
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
    public void componentPicked(int playerId, Component component, boolean success, String message) {
        synchronized (displayMutex) {
            // Check if this response is for the current player
            if (currentPlayer != null && currentPlayer.getId() == playerId) {
                if (success && component != null) {
                    console.printMessage("✓ Component picked: " + component.getClass().getSimpleName());
                    // Pass the component to the ship building handler if it's active
                    if (activeShipBuildingHandler != null) {
                        if (activeShipBuildingHandler.getCurrentComponent() == null) {
                            activeShipBuildingHandler.setCurrentComponent(component);
                            activeShipBuildingHandler.refreshMenuDisplay();
                        }
                    }
                } else {
                    console.printError("✗ " + message);
                    if (activeShipBuildingHandler != null) {
                        activeShipBuildingHandler.refreshMenuDisplay();
                    }
                }
            }
            // Ignore responses for other players
        }
    }

    @Override
    public void componentPickFailed(String errorMessage) {

    }

    @Override
    public void adventureCardPicked(int playerId, it.polimi.ingsw.AdventureCards.AdventureCard adventureCard, boolean success, String message, int nextPlayerTurn) {
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
    public void cabinColorSelected(String color) {

    }

    @Override
    public void cabinColorSelectionFailed(String errorMsg) {

    }

    @Override
    public void flightPreparationStart(UUID matchId) {

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
            console.printMessage("\nALL PLAYERS POSITIONED! Starting Adventure Phase!");
            console.printMessage("=== FINAL POSITIONS ===");
            
            // Display final position standings
            for (int position = 1; position <= playerPositions.size(); position++) {
                for (Map.Entry<Integer, Integer> entry : playerPositions.entrySet()) {
                    if (entry.getValue() == position) {
                        int playerId = entry.getKey();
                        String playerName = (currentPlayer != null && currentPlayer.getId() == playerId) ? 
                            "You" : "Player " + playerId;
                        console.printMessage("Position " + position + ": " + playerName);
                        break;
                    }
                }
            }
            
            console.printMessage("======================\n");
            console.printMessage("ADVENTURE CARD RULE: Only the leading player (position 1) picks adventure cards for everyone!");
            
            // Check if we are the leader
            if (currentPlayer != null && currentPlayer.getId() == firstPlayerToAct) {
                console.printMessage("You are the leading player! Type 'pick' to draw an adventure card for all players.");
            } else {
                console.printMessage("Waiting for the leading player (position 1) to pick an adventure card...");
            }
            
            // Transition to adventure phase
            state = TuiState.ADVENTURE_PHASE;
        }
    }


}
