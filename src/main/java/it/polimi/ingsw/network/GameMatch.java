package it.polimi.ingsw.network;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import it.polimi.ingsw.GameController;
import it.polimi.ingsw.Player;

public class GameMatch {

    private static final int MAX_PLAYERS = 4;

    private UUID matchId;
    private String matchName;
    private GameController controller;
    private List<Player> players;
    private MatchStates matchState;
    private Player hostPlayer;
    private Map<Integer, Boolean> readyPlayersMap;
    
    // Position tracking for ship building completion order
    private final Map<Integer, Integer> playerPositions = new ConcurrentHashMap<>();
    private final AtomicInteger nextPosition = new AtomicInteger(1);

    private Map<Integer, String> playerCabinColors = new HashMap<>();
    private Set<String> availableColors = new HashSet<>(Arrays.asList("BLUE", "GREEN", "YELLOW", "RED"));
    private Map<Integer, Boolean> playersValidated = new HashMap<>();
    private Map<Integer, Boolean> playersReadyForFlight = new HashMap<>();

    private List<Integer> shipValidationOrder = new ArrayList<>();
    private Map<Integer, Long> validationTimestamps = new HashMap<>();

    // Planet selection state management
    private boolean planetSelectionActive = false;
    private it.polimi.ingsw.AdventureCards.Planets activePlanetsCard = null;
    private List<Integer> planetSelectionOrder = new ArrayList<>();
    private int currentPlanetSelectionIndex = 0;
    private Map<Integer, Integer> planetSelections = new HashMap<>(); // playerId -> planetIndex (-1 = skip)

    private Difficulty difficulty;

    public GameMatch(UUID matchId, String matchName, int hostId, String hostName, Difficulty difficulty) {
        this.matchId = matchId;
        this.matchName = matchName;
        this.controller = new GameController();
        this.players = new ArrayList<>();
        this.matchState = MatchStates.WAITING_FOR_PLAYERS;
        this.difficulty = difficulty;

        Player host = new Player(hostId, hostName, true);
        this.players.add(host);
        this.hostPlayer = host;
        this.controller.addPlayer(host);
    }

    // Getter
    public UUID getMatchId() {
        return this.matchId;
    }

    public String getMatchName() {
        return this.matchName;
    }

    public GameController getController() {
        return this.controller;
    }

    public List<Player> getPlayers() {
        return new ArrayList<>(players);
    }

    public int getNumberOfPlayers() {
        return this.players.size();
    }

    public MatchStates getMatchState() {
        return this.matchState;
    }

    public synchronized Player getHostPlayer() {
        return this.hostPlayer;
    }

    public Player getPlayerById(int playerId) {
        return players.stream()
                .filter(player -> player.getId() == playerId)
                .findFirst()
                .orElse(null);
    }

    public Map<Integer, Boolean> getReadyPlayersMap() {
        return readyPlayersMap;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public synchronized void recordPlayerValidation(int playerId) {
        if (!playersValidated.containsKey(playerId) || !playersValidated.get(playerId)) {
            playersValidated.put(playerId, true);
            validationTimestamps.put(playerId, System.currentTimeMillis());
            
            if (!shipValidationOrder.contains(playerId)) {
                shipValidationOrder.add(playerId);
            }
            
            System.out.println("[GameMatch " + matchId + "] Player " + playerId + 
                " validated ship. Validation order position: " + shipValidationOrder.indexOf(playerId));
        }
    }

    public List<Integer> getShipValidationOrder() {
        return new ArrayList<>(shipValidationOrder);
    }

    public List<Integer> getCurrentFlightOrder() {    
        List<Player> flightOrderedPlayers = controller.getPlayersInFlightOrder();
        List<Integer> playerIds = new ArrayList<>();
        
        for (Player player : flightOrderedPlayers) {
            playerIds.add(player.getId());
        }
        
        return playerIds;
    }

    public int getPlayerFlightPosition(int playerId) {    
        Player player = getPlayerById(playerId);
        if (player == null) {
            return -1;
        }
        
        try {
            return controller.getPlayerFlightPosition(player);
        } catch (IllegalArgumentException e) {
            return -1;
        }
    }

    public Player getLeadingPlayer() {      
        return controller.getLeadinPlayer();
    }

    public List<Player> getPlayersInFlightOrder() {        
        return controller.getPlayersInFlightOrder();
    }

    public Integer getFirstValidatedPlayer() {
        return shipValidationOrder.isEmpty() ? null : shipValidationOrder.get(0);
    }

    public Map<Integer, Long> getValidationTimestamps() {
        return new HashMap<>(validationTimestamps);
    }

    // General match state checks
    public boolean isFull() {
        return this.players.size() >= MAX_PLAYERS;
    }

    public synchronized boolean isEmpty() {
        return this.players.isEmpty();
    }

    public boolean canStartMatch() {
        return this.players.size() >= 2 && matchState == MatchStates.WAITING_FOR_PLAYERS;
    }

    public boolean isPlayerInMatch(int playerId) {
        return this.players.contains(getPlayerById(playerId));
    }

    public boolean isPlayerInMatchById(int playerId) {
        return this.players.stream().anyMatch(player -> player.getId() == playerId);
    }

    public boolean isPlayerHostById(int playerId) {
        return this.hostPlayer != null && this.hostPlayer.getId() == playerId;
    }

    public void setPlayerCabinColor(int playerId, String color) {
        String previousColor = playerCabinColors.get(playerId);
        if (previousColor != null) {
            availableColors.add(previousColor);
        }
        
        playerCabinColors.put(playerId, color);
        availableColors.remove(color);
    }

    public String getPlayerCabinColor(int playerId) {
        return playerCabinColors.get(playerId);
    }
    
    public boolean isColorTaken(String color) {
        return !availableColors.contains(color);
    }

    public boolean allPlayersValidated() {
        return playersValidated.size() == players.size() && 
            playersValidated.values().stream().allMatch(Boolean::booleanValue);
    }

    public void setPlayerReadyForFlight(int playerId, boolean ready) {
        playersReadyForFlight.put(playerId, ready);
    }

    public boolean allPlayersReadyForFlight() {
        if (playersReadyForFlight.isEmpty()) {
            return false;
        }
        
        for (Player player : players) {
            if (player.isActive()) {
                Boolean isReady = playersReadyForFlight.get(player.getId());
                if (isReady == null || !isReady) {
                    return false;
                }
            }
        }
        return true;
    }

    public synchronized void setMatchState(MatchStates state) {
        MatchStates oldState = this.matchState;
        this.matchState = state;
        System.out.println("[GameMatch " + matchId + "] State changed from " + oldState + " to " + state);
    }

    public Map<Integer, Boolean> getPlayersValidated() {
        return playersValidated;
    }

    // Handling players in the match
    public synchronized void addPlayer(int playerId, String playerName) {
        if (this.players.size() < MAX_PLAYERS && matchState == MatchStates.WAITING_FOR_PLAYERS) {
            if (isPlayerInMatchById(playerId)) {
                System.out.println("[GameMatch " + matchId + "] Player " + playerName + " already in match " + matchName);
                return;
            }
            if (isFull() || matchState != MatchStates.WAITING_FOR_PLAYERS) {
                System.out.println("[GameMatch " + matchId + "] Cannot add player " + playerName + ": Match is full or already started");
                return;
            }
            Player player = new Player(playerId, playerName, true);
            this.players.add(player);
            this.controller.addPlayer(player);

            if (hostPlayer == null) {
                hostPlayer = player;
                System.out.println("[GameMatch " + matchId + "] Player " + playerName + " became host (no previous host)");
            } 

            System.out.println("[GameMatch " + matchId + "] Player " + playerName + " added to match " + matchName);
        } else {
            System.out.println("[GameMatch " + matchId + "] Cannot add player " + playerName + ": Match is full or already started");
        }
    }

    public synchronized boolean removePlayer(int playerId) {
        Player player = getPlayerById(playerId);
        if (player == null) {
            System.out.println("[GameMatch " + matchId + "] Player ID "
                + playerId + " not found in match " + matchName);
            return false;
        }

        String color = playerCabinColors.remove(playerId);
        if (color != null) {
            availableColors.add(color);
            System.out.println("[GameMatch " + matchId + "] Released cabin color " + color + " for player " + player.getNickName());
        }

        playersValidated.remove(playerId);
        playersReadyForFlight.remove(playerId);
        if (readyPlayersMap != null) {
            readyPlayersMap.remove(playerId);
        }

        boolean wasHost = isPlayerHostById(playerId);
        boolean removed = this.players.remove(player);

        if (!removed) {
            System.out.println("[GameMatch " + matchId + "] Player " + player.getNickName() + " could not be removed from match " + matchName);
            return false;
        }

        System.out.println("[GameMatch " + matchId + "] Player " + player.getNickName() + " removed from match " + matchName);

        if (players.isEmpty()) {
            this.hostPlayer = null;
            System.out.println("[GameMatch " + matchId + "] Match is now empty, no host assigned");
        } else if (wasHost) {
            this.hostPlayer = players.get(0);
            System.out.println("[GameMatch " + matchId + "] New host assigned: " + hostPlayer.getNickName() + " (ID: " + hostPlayer.getId() + ")");
        }

        return removed;
    }

    // Control match state
    public synchronized void startMatch() {
        if (matchState == MatchStates.WAITING_FOR_PLAYERS && players.size() >= 2) {
            matchState = MatchStates.LOADING;
            
            if (controller != null) {
                controller.startGame();
            }
            
            System.out.println("[GameMatch " + matchId + "] Match " + matchName + " started with " + players.size() + " players. Loading game...");
        } else {
            System.out.println("[GameMatch " + matchId + "] Cannot start match: insufficient players or wrong state");
        }
    }

    public synchronized void loadingAckResponse() {
        if (matchState == MatchStates.LOADING) {
            readyPlayersMap = new java.util.HashMap<>();
            for (Player player : players) {
                readyPlayersMap.put(player.getId(), false);
            }
            System.out.println("[GameMatch " + matchId + "] Game loaded. Waiting for players to finish loading...");
        }
    }

    public synchronized boolean setPlayerReady(int playerId) {
        if (readyPlayersMap == null || !readyPlayersMap.containsKey(playerId)) {
            System.out.println("[GameMatch " + matchId + "] Player ID " + playerId + " is not in the match or match is not loading.");
            return false;
        }

        // if the player is already ready, ignore the request otherwise he would be able to spam (interesting behavior for trolling)
        if (Boolean.TRUE.equals(readyPlayersMap.get(playerId))) {
            System.out.println("[GameMatch " + matchId + "] Player ID " + playerId + " was already ready. Ignoring duplicate.");
            return false;
        }

        readyPlayersMap.put(playerId, true);
        System.out.println("[GameMatch " + matchId + "] Player ID " + playerId + " is ready.");

        if (readyPlayersMap.values().stream().allMatch(Boolean::booleanValue)) {
            matchState = MatchStates.IN_PROGRESS;
            System.out.println("[GameMatch " + matchId + "] All players are ready. " + "Match is now in progress.");
        } else {
            System.out.println("[GameMatch " + matchId + "] Waiting for all players to be ready...");
        }
        return true;
    }


    public void endMatch() {
        this.matchState = MatchStates.FINISHED;
        System.out.println("[GameMatch " + matchId + "] Match " + matchName + " has ended.");
        // TODO: handle end of match logic
    }

    /**
     * Assigns a position to a player based on ship building completion order.
     * The first player to complete gets completion order 1 and flight position 6,
     * second gets completion order 2 and flight position 3, etc.
     * 
     * @param playerId The ID of the player who completed ship building
     * @return The assigned completion order (1-4), or -1 if player not found or already assigned
     */
    public synchronized int assignPlayerPosition(int playerId) {
        // Check if player is in the match
        if (!isPlayerInMatchById(playerId)) {
            System.out.println("[GameMatch " + matchId + "] Cannot assign position: Player ID " + playerId + " not in match");
            return -1;
        }
        
        // Check if player already has a position assigned
        if (playerPositions.containsKey(playerId)) {
            int existingPosition = playerPositions.get(playerId);
            System.out.println("[GameMatch " + matchId + "] Player ID " + playerId + " already has completion order " + existingPosition);
            return existingPosition;
        }
        
        // Assign the next available completion order
        int assignedCompletionOrder = nextPosition.getAndIncrement();
        if (assignedCompletionOrder > MAX_PLAYERS) {
            System.out.println("[GameMatch " + matchId + "] No more positions available for player ID " + playerId);
            return -1;
        }
        
        // Store the completion order in playerPositions (for backward compatibility)
        playerPositions.put(playerId, assignedCompletionOrder);
        
        // Set the actual flight board position based on completion order
        Player player = getPlayerById(playerId);
        if (player != null && controller != null) {
            boolean success = controller.setPlayerInitialStartingPosition(player, assignedCompletionOrder);
            if (!success) {
                System.out.println("[GameMatch " + matchId + "] Failed to set flight board position for player " + player.getNickName());
                // Remove from completion order tracking if flight board position failed
                playerPositions.remove(playerId);
                nextPosition.decrementAndGet(); // Rollback the increment
                return -1;
            }
        }
        
        String playerName = player != null ? player.getNickName() : "Unknown";
        System.out.println("[GameMatch " + matchId + "] Player " + playerName + " (ID: " + playerId + 
                          ") assigned completion order " + assignedCompletionOrder + " and flight board position");
        return assignedCompletionOrder;
    }
    
    /**
     * Gets the assigned position for a player
     * 
     * @param playerId The player ID
     * @return The assigned position, or -1 if not assigned
     */
    public int getPlayerPosition(int playerId) {
        return playerPositions.getOrDefault(playerId, -1);
    }
    
    /**
     * Checks if all players have been assigned positions
     * 
     * @return true if all players have positions, false otherwise
     */
    public boolean allPlayersPositioned() {
        return playerPositions.size() == players.size();
    }
    
    /**
     * Gets the current position assignments
     * 
     * @return Map of player ID to assigned position
     */
    public Map<Integer, Integer> getPlayerPositions() {
        return new ConcurrentHashMap<>(playerPositions);
    }

    /**
     * Gets the next player's turn for adventure card picking based on position order.
     * Returns -1 if all players have had their turn.
     * 
     * @param currentPlayerId The ID of the player who just picked a card
     * @return The next player's ID, or -1 if phase is complete
     */
    public synchronized int getNextPlayerTurnForAdventure(int currentPlayerId) {
        // Get current player's position
        int currentPosition = playerPositions.getOrDefault(currentPlayerId, -1);
        if (currentPosition == -1) {
            return -1; // Player not found
        }
        
        // Find the next position in order (1, 2, 3, 4)
        int nextPosition = currentPosition + 1;
        
        // Find the player with the next position
        for (Map.Entry<Integer, Integer> entry : playerPositions.entrySet()) {
            if (entry.getValue() == nextPosition) {
                return entry.getKey(); // Return the player ID with next position
            }
        }
        
        // If no next position found, adventure phase is complete
        return -1;
    }

    /**
     * Gets the player ID of the player who should pick adventure cards (leading player).
     * This method now checks the actual flight board positions after adventure card effects.
     * 
     * @return The player ID of the adventure card picker (leading player), or -1 if not found
     */
    public synchronized int getFirstPlayerToAct() {
        if (controller != null) {
            // Get the adventure card picker from the flight board after effects
            Player cardPicker = controller.getAdventureCardPicker();
            if (cardPicker != null) {
                return cardPicker.getId();
            }
        }
        
        // Fallback to initial positions if controller not available
        // Find player with best completion order (lowest number = best initial position)
        int bestCompletionOrder = Integer.MAX_VALUE;
        int bestPlayerId = -1;
        
        for (Map.Entry<Integer, Integer> entry : playerPositions.entrySet()) {
            int completionOrder = entry.getValue();
            if (completionOrder < bestCompletionOrder) {
                bestCompletionOrder = completionOrder;
                bestPlayerId = entry.getKey();
            }
        }
        
        return bestPlayerId;
    }

    /**
     * Start planet selection phase for a Planets card
     * @param planetsCard The Planets card that was drawn
     * @return true if planet selection was started successfully
     */
    public synchronized boolean startPlanetSelection(it.polimi.ingsw.AdventureCards.Planets planetsCard) {
        if (planetSelectionActive) {
            System.out.println("[GameMatch " + matchId + "] Planet selection already active");
            return false;
        }
        
        // Get players in flight order (leader first)
        List<Player> playersInOrder = getPlayersInFlightOrder();
        if (playersInOrder.isEmpty()) {
            System.out.println("[GameMatch " + matchId + "] No players available for planet selection");
            return false;
        }
        
        // Initialize planet selection state
        planetSelectionActive = true;
        activePlanetsCard = planetsCard;
        planetSelectionOrder.clear();
        currentPlanetSelectionIndex = 0;
        planetSelections.clear();
        
        // Build selection order (leader first, then others in flight order)
        for (Player player : playersInOrder) {
            planetSelectionOrder.add(player.getId());
        }
        
        System.out.println("[GameMatch " + matchId + "] Started planet selection with " + 
                          planetSelectionOrder.size() + " players");
        return true;
    }
    
    /**
     * Get the current player who should make a planet selection
     * @return Player ID of current selector, or -1 if no selection active
     */
    public synchronized int getCurrentPlanetSelector() {
        if (!planetSelectionActive || currentPlanetSelectionIndex >= planetSelectionOrder.size()) {
            return -1;
        }
        return planetSelectionOrder.get(currentPlanetSelectionIndex);
    }
    
    /**
     * Record a planet selection and advance to next player
     * @param playerId The player making the selection
     * @param planetIndex The selected planet index (-1 = skip)
     * @return true if selection was recorded successfully
     */
    public synchronized boolean recordPlanetSelection(int playerId, int planetIndex) {
        if (!planetSelectionActive) {
            System.out.println("[GameMatch " + matchId + "] No planet selection active");
            return false;
        }
        
        int expectedPlayerId = getCurrentPlanetSelector();
        if (playerId != expectedPlayerId) {
            System.out.println("[GameMatch " + matchId + "] Player " + playerId + 
                              " cannot select planet, expected player " + expectedPlayerId);
            return false;
        }
        
        // Record the selection
        planetSelections.put(playerId, planetIndex);
        
        // If a planet was selected (not skipped), mark it as taken in the Planets card
        if (planetIndex >= 0 && activePlanetsCard != null) {
            boolean success = activePlanetsCard.handlePlayerSelection(getPlayerById(playerId), planetIndex);
            if (!success) {
                System.out.println("[GameMatch " + matchId + "] Failed to record planet selection for player " + playerId);
                return false;
            }
        } else if (planetIndex == -1 && activePlanetsCard != null) {
            // Player chose to skip
            activePlanetsCard.handlePlayerSelection(getPlayerById(playerId), -1);
        }
        
        // Move to next player
        currentPlanetSelectionIndex++;
        
        System.out.println("[GameMatch " + matchId + "] Player " + playerId + 
                          " selected planet " + planetIndex + " (index " + currentPlanetSelectionIndex + 
                          "/" + planetSelectionOrder.size() + ")");
        return true;
    }
    
    /**
     * Check if planet selection phase is complete
     * @return true if all players have made their selections
     */
    public synchronized boolean isPlanetSelectionComplete() {
        return planetSelectionActive && currentPlanetSelectionIndex >= planetSelectionOrder.size();
    }
    
    /**
     * Get the next player in planet selection order
     * @return Player ID of next selector, or -1 if no more players
     */
    public synchronized int getNextPlanetSelector() {
        if (!planetSelectionActive || currentPlanetSelectionIndex >= planetSelectionOrder.size()) {
            return -1;
        }
        return planetSelectionOrder.get(currentPlanetSelectionIndex);
    }
    
    /**
     * End planet selection phase and apply results
     * @return true if planet selection was ended successfully
     */
    public synchronized boolean endPlanetSelection() {
        if (!planetSelectionActive) {
            return false;
        }
        
        planetSelectionActive = false;
        
        // Apply planet selection results through the Planets card
        if (activePlanetsCard != null && controller != null) {
            // The Planets card will handle the cargo loading and flight day costs
            // based on the selections that were recorded
            System.out.println("[GameMatch " + matchId + "] Applying planet selection results");
            
            // Complete the planet selection process
            activePlanetsCard.completePlanetSelection(controller.getGameModel());
        }
        
        activePlanetsCard = null;
        planetSelectionOrder.clear();
        currentPlanetSelectionIndex = 0;
        planetSelections.clear();
        
        System.out.println("[GameMatch " + matchId + "] Planet selection phase ended");
        return true;
    }
    
    /**
     * Get available planets for selection
     * @return List of available planets, or empty list if no selection active
     */
    public synchronized List<it.polimi.ingsw.AdventureCards.Planets.Planet> getAvailablePlanets() {
        if (!planetSelectionActive || activePlanetsCard == null) {
            return new ArrayList<>();
        }
        return activePlanetsCard.getAvailablePlanets();
    }
    
    /**
     * Get the flight days cost for the current planet selection
     * @return Flight days cost, or 0 if no selection active
     */
    public synchronized int getPlanetSelectionFlightDaysCost() {
        if (!planetSelectionActive || activePlanetsCard == null) {
            return 0;
        }
        return activePlanetsCard.getFlightDaysCost();
    }
    
    /**
     * Check if planet selection is currently active
     * @return true if planet selection is in progress
     */
    public synchronized boolean isPlanetSelectionActive() {
        return planetSelectionActive;
    }

    // TODO: implement something to retrieve match statistics or results 

}
