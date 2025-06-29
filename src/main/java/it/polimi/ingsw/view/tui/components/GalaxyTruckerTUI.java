package it.polimi.ingsw.view.tui.components;

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
import it.polimi.ingsw.AdventureCards.AdventureCard;
import it.polimi.ingsw.AdventureCards.Planets;


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
    private AdventurePhaseHandler activeAdventurePhaseHandler;
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

    private void handleShipBuildingState() {
        if (this.currentPlayer == null) {
            console.printError("Cannot start ship building: current player not set.");
            state = TuiState.ASK_USERNAME;
            return;
        }
        // Create a new ship building handler (either first time or after validation failure)
        activeShipBuildingHandler = new ShipBuildingHandler(console, client, this.currentPlayer, this);
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
        
        if (true) {
            if (this.state == TuiState.WAIT_FOR_ALL_PLAYERS_POSITIONED) {
                console.printMessage("\nAll players have been positioned! Starting the game...");
                this.state = TuiState.RUNNING;
            } else {
                // Log or handle unexpected state transition attempt
                console.printError("Tried to proceed to running state from an unexpected state: " + this.state);
            }
        }
    }

    private void handleAdventurePhaseState() {
        if (activeAdventurePhaseHandler == null) {
            console.printError("Adventure phase handler not initialized!");
            state = TuiState.TERMINATED;
            return;
        }
        
        activeAdventurePhaseHandler.handleAdventurePhase();
        
        // Check if the handler was deactivated due to quit command
        if (!activeAdventurePhaseHandler.isActive()) {
            state = TuiState.TERMINATED;
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

    /**
     * Check if TUI is currently in adventure phase state
     * Used by AdventurePhaseHandler to know when to exit
     */
    public boolean isInAdventurePhaseState() {
        return state == TuiState.ADVENTURE_PHASE;
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
                console.printMessage("✓ Ship building complete! Requesting position assignment...");
                // Immediately transition away from ship building to stop the ship building handler
                if (state == TuiState.SHIP_BUILDING) {
                    state = TuiState.WAIT_FOR_ALL_PLAYERS_POSITIONED;
                    console.printMessage("Once all players complete validation, the adventure phase will begin.");
                }
                // Position assignment will be handled by positionAssigned() callback
            } else {
                console.printError("✗ " + message);
                console.printMessage("Returning to ship building - please fix the issues and try again...");
                // Always return to ship building state on validation failure
                state = TuiState.SHIP_BUILDING;
                // Clear the active handler so it gets recreated in the main loop
                activeShipBuildingHandler = null;
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
            
            this.state = TuiState.SHIP_BUILDING;
        }
    }

    @Override
    public void shipBuildingStartFailed(String errorMessage) {

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
    public void adventureCardPicked(int playerId, AdventureCard adventureCard, boolean success, String message, int nextPlayerTurn, Map<Integer, Integer> updatedPositions) {
        synchronized (displayMutex) {
            if (activeAdventurePhaseHandler != null) {
                activeAdventurePhaseHandler.handleAdventureCardPicked(playerId, adventureCard, success, message, nextPlayerTurn, updatedPositions);
            } else {
                console.printError("Adventure phase handler not available to process card pick response");
            }
        }
    }

    @Override
    public void adventurePhaseComplete(UUID matchId, boolean gameComplete, String completionReason) {
        synchronized (displayMutex) {
            if (activeAdventurePhaseHandler != null) {
                activeAdventurePhaseHandler.handlePhaseComplete(gameComplete, completionReason);
                activeAdventurePhaseHandler = null; // Clear the handler
            }
            
            if (gameComplete) {
                state = TuiState.GAME_ENDED;
            } else {
                state = TuiState.RUNNING;
            }
        }
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
            // Initialize adventure phase handler
            activeAdventurePhaseHandler = new AdventurePhaseHandler(console, client, currentPlayer, this);
            activeAdventurePhaseHandler.initializePhase(playerPositions, firstPlayerToAct);
            
            // Transition to adventure phase
            state = TuiState.ADVENTURE_PHASE;
        }
    }

    @Override
    public void planetChoiceRequested(UUID matchId, List<Planets.Planet> availablePlanets, int flightDaysCost, int currentPlayerId, String currentPlayerName) {
        synchronized (displayMutex) {
            if (activeAdventurePhaseHandler != null) {
                activeAdventurePhaseHandler.handlePlanetChoiceRequested(matchId, availablePlanets, flightDaysCost, currentPlayerId, currentPlayerName);
            } else {
                console.printError("Adventure phase handler not available to process planet choice request");
            }
        }
    }

    @Override
    public void planetSelected(int playerId, String playerName, int planetIndex, String planetName, boolean success, String message) {
        synchronized (displayMutex) {
            if (activeAdventurePhaseHandler != null) {
                activeAdventurePhaseHandler.handlePlanetSelected(playerId, playerName, planetIndex, planetName, success, message);
            } else {
                // Handle planet selection directly if no adventure phase handler
                if (success) {
                    if (planetIndex >= 0) {
                        console.printMessage("✓ " + playerName + " selected planet: " + planetName);
                    } else {
                        console.printMessage("✓ " + playerName + " chose not to land on any planet");
                    }
                } else {
                    console.printError("✗ Planet selection failed for " + playerName + ": " + message);
                }
            }
        }
    }

}
