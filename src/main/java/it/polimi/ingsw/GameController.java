package it.polimi.ingsw;

import it.polimi.ingsw.AdventureCards.AdventureCard;
import it.polimi.ingsw.Components.*;

import java.util.List;
import java.util.Map;

public class  GameController {
    private GameModel gameModel;
    private GameView gameView;

    public GameController() {
        gameModel = new GameModel();
    }

    private boolean isPlayerThere(Player player) {
        return gameModel.getPlayers().contains(player);
    }

    public void startGame(){
        gameModel.startGame();
        System.out.println("[GameController] Game started with " + gameModel.getPlayers().size() + " players");
        //gameView.startGame();
        //Set private method??
    }

    public void startGame(List<Player> players) {
        for (Player player : players) {
            if (!isPlayerThere(player)) {
                gameModel.addPlayer(player);
            }
        }
        gameModel.startGame();
    }

    //TODO Consider instatiating a new player and returning it..
    public void addPlayer(Player player){
        gameModel.addPlayer(player);
        System.out.println("[GameController] Player " + player.getNickName() + " (ID: " + player.getId() + ") added to game");
    }

    public void setCredits(Player player, int credits){
        gameModel.setCredits(player, credits);
    }
    public void setCurrentPlayer(Player player){
        gameModel.setCurrentPlayer(player);
    }

    public List<Player> getPlayers(){
        return gameModel.getPlayers();
    }

    public Player getPlayerByNickName (String nickName){
        for (Player p : gameModel.getPlayers()){
            if (p.getNickName().equals(nickName)){
                return p;
            }
        }
        return null;
    }

    public Player getPlayerById(int id) {
        for (Player p : gameModel.getPlayers()) {
            if (p.getId() == id) {
                return p;
            }
        }
        return null;
    }

    public Component getCompFromDeck(Player p){
        return gameModel.pickOneComponentCardFromDeck(p);
    }

    public boolean addComponentForPlayer(Player player, Component component, Position pos) {
        if (isPlayerThere(player)) {
            return player.placeComponent(component, pos);
        }
        return false; // Player not found or other issue
    }

    public boolean removeComponentAt(Player player, Position position) {
        if (isPlayerThere(player)) {
            return player.removeComponentAt(position);
        }
        return false;
    }

    // public boolean removeComponentForPlayer(Player player, Component component) {

    //     if (isPlayerThere(player)) {
    //         return player.removeComponent(component);
    //     }
    //     return false;
    // }

    public void pickCardForPlayer(Player player) {
        if (isPlayerThere(player)) {
           gameModel.pickAdventureCardForPlayer(player);
        }
    }

    /**
     * Applies the effect of an adventure card to the game state.
     * Uses polymorphism to call the specific card's effect implementation.
     * 
     * @param adventureCard The adventure card whose effect should be applied
     */
    public void applyAdventureCardEffect(AdventureCard adventureCard) {
        if (adventureCard == null) {
            return;
        }

        System.out.println("[GameController] Applying effect for adventure card: " + adventureCard.getClass().getSimpleName());
        
        // Use polymorphism - each card implements its own effect method
        adventureCard.effect(gameModel);
    }

    /**
     * Checks if there are more adventure cards to pick
     * @return true if there are cards remaining, false if deck is empty
     */
    public boolean hasMoreAdventureCards() {
        return !gameModel.getAdventureDeck().isEmpty();
    }

    /**
     * Checks if the flight/game is complete
     * @return true if flight is complete (deck empty or only one active player), false otherwise
     */
    public boolean isFlightComplete() {
        return gameModel.isFlightComplete();
    }

    public boolean startMatchIfPossible()
    {
        boolean eval = gameModel.isGameStartable();
        if (eval) {
            startGame();
            // Set the proper attributes to start the game buid ships etc..
        }
        return eval;
    }

    public void setPlayerReady(Player player) {
        player.setReady(true);
    }

    public List<Player> getFlightOrder() {
        return gameModel.getFlightBoard().getPlayersInFlightOrder();
    }

    public int getPlayerFlightPosition(Player player) {
        if (isPlayerThere(player)) {
            return gameModel.getFlightBoard().getPosition(player);
        }
        return -1;
    }

    public Player getLeadinPlayer() {
        return gameModel.getFlightBoard().getLeadingPlayer();
    }

    public List<Player> getPlayersInFlightOrder() {
        return gameModel.getFlightBoard().getPlayersInFlightOrder();
    }

    /**
     * Sets the initial starting position for the given player.
     *
     * @param player The player choosing the position.
     * @param position The chosen starting position (1-4).
     * @return true if the position was successfully set, false otherwise.
     */
    public boolean setPlayerInitialStartingPosition(Player player, int position) {
        if (isPlayerThere(player)) {
            boolean result = gameModel.setPlayerInitialStartingPosition(player, position);
            if (result) {
                System.out.println("[GameController] Player " + player.getNickName() + " (ID: " + player.getId() + ") set to position " + position);
            } else {
                System.out.println("[GameController] Failed to set position " + position + " for player " + player.getNickName());
            }
            return result;
        }
        System.out.println("[GameController] Player not found when trying to set position");
        return false; // Player not found
    }

    /**
     * Gets the list of available starting positions from the game model.
     *
     * @return A list of integers representing available starting positions.
     */
    public List<Integer> getAvailableStartingPositions() {
        List<Integer> positions = gameModel.getAvailableStartingPositions();
        System.out.println("[GameController] Available positions: " + positions);
        return positions;
    }

    /**
     * Gets the current position of a player on the flight board.
     *
     * @param player The player to check.
     * @return The current position of the player, or -1 if player is not found.
     */
    public int getPlayerPosition(Player player) {
        if (isPlayerThere(player)) {
            return gameModel.getFlightBoard().getPosition(player);
        }
        return -1; // Player not found
    }

    /**
     * Gets a map of all players and their positions on the flight board.
     *
     * @return A map with players as keys and their positions as values.
     */
    public Map<Player, Integer> getAllPlayerPositions() {
        return gameModel.getFlightBoard().getAllPositions();
    }

    /**
     * Updates a player's position based on their player ID.
     * This method handles the controller logic for position updates from network messages.
     *
     * @param playerId The ID of the player whose position was updated
     * @param position The new position of the player
     * @return true if the position was successfully updated, false otherwise
     */
    public boolean updatePlayerPosition(int playerId, int position) {
        Player player = getPlayerById(playerId);
        if (player != null) {
            boolean result = gameModel.setPlayerInitialStartingPosition(player, position);
            if (result) {
                System.out.println("[GameController] Player " + player.getNickName() + " (ID: " + playerId + ") position updated to " + position);
            } else {
                System.out.println("[GameController] Failed to update position " + position + " for player " + player.getNickName());
            }
            return result;
        }
        System.out.println("[GameController] Player with ID " + playerId + " not found when trying to update position");
        return false;
    }

    /**
     * Checks if all players have been positioned and the game can proceed to the running state.
     * This method contains the controller logic for determining game state transitions.
     *
     * @return true if all players are positioned and game can proceed, false otherwise
     */
    public boolean canProceedToRunningState() {
        List<Integer> availablePositions = getAvailableStartingPositions();
        boolean allPlayersPositioned = availablePositions.isEmpty() || 
                                     (availablePositions.size() == 4 - gameModel.getPlayers().size());
        
        if (allPlayersPositioned) {
            System.out.println("[GameController] All players have been positioned. Game can proceed to running state.");
        } else {
            System.out.println("[GameController] Still waiting for players to choose positions. Available: " + availablePositions);
        }
        
        return allPlayersPositioned;
    }

    /**
     * Validates that a position selection is valid for the current game state.
     *
     * @param position The position to validate
     * @return true if the position is available and valid, false otherwise
     */
    public boolean isPositionSelectionValid(int position) {
        List<Integer> availablePositions = getAvailableStartingPositions();
        boolean isValid = availablePositions.contains(position);
        
        if (!isValid) {
            System.out.println("[GameController] Position " + position + " is not available. Available positions: " + availablePositions);
        }
        
        return isValid;
    }

    /**
     * Handles the logic for when a player attempts to select a position.
     * This combines validation and position setting into a single controller method.
     *
     * @param player The player selecting the position
     * @param position The desired position
     * @return true if the position was successfully selected, false otherwise
     */
    public boolean handlePlayerPositionSelection(Player player, int position) {
        if (!isPositionSelectionValid(position)) {
            return false;
        }
        
        return setPlayerInitialStartingPosition(player, position);
    }

    /**
     * Ensures a player with the given ID exists in the local controller.
     * This is useful for client-side controllers that need to sync with server data.
     *
     * @param playerId The ID of the player to ensure exists
     * @param nickname The nickname of the player (used if player needs to be created)
     * @return The player object (existing or newly created)
     */
    public Player ensurePlayerExists(int playerId, String nickname) {
        Player existingPlayer = getPlayerById(playerId);
        if (existingPlayer == null) {
            // Create the player if it doesn't exist
            Player newPlayer = new Player(playerId, nickname, false);
            addPlayer(newPlayer);
            System.out.println("[GameController] Added player " + nickname + " (ID: " + playerId + ") to local controller");
            return newPlayer;
        }
        return existingPlayer;
    }

    public void playTurn() {
        gameModel.playTurn();
    }

    public boolean validateShipForPlayer(Player player) {
        if (isPlayerThere(player)) {
            String validationErrors = player.getShip().validateShip();
            return validationErrors == null;
        }
        return false;
    }

    public String getShipValidationErrors(Player player) {
        if (isPlayerThere(player)) {
            return player.getShip().validateShip();
        }
        return "Player not found";
    }

    public void rotateComponentClockwiseForPlayer(Player player, Component componentToRotate) {
        if (isPlayerThere(player)) {
            ShipBuilder ship = player.getShip();
            if (ship != null && componentToRotate != null) {
                ship.rotateComponentOfShipClockwise(componentToRotate);
                // Consider calling updateView() or returning a status if needed
            }
        }
    }

    public Component rotateComponentClockwise(Player player, Component componentToRotate) {
        if (isPlayerThere(player)) {
            ShipBuilder ship = player.getShip();
            if (ship != null && componentToRotate != null) {
                componentToRotate = ship.rotateComponentClockwise(componentToRotate);
                // Consider calling updateView() or returning a status if needed
            }
        }
        return componentToRotate;
    }

    public void rotateComponentCounterClockwiseForPlayer(Player player, Component componentToRotate) {
        if (isPlayerThere(player)) {
            ShipBuilder ship = player.getShip();
            if (ship != null && componentToRotate != null) {
                ship.rotateComponentOfShipCounterClockwise(componentToRotate);
                // Consider calling updateView() or returning a status if needed
            }
        }
    }

    public Component rotateComponentCounterClockwise(Player player, Component componentToRotate) {
        if (isPlayerThere(player)) {
            ShipBuilder ship = player.getShip();
            if (ship != null && componentToRotate != null) {
                componentToRotate = ship.rotateComponentCounterClockwise(componentToRotate);
                // Consider calling updateView() or returning a status if needed
            }
        }
        return componentToRotate;
    }

    public Component rotateComponentOpposite(Player player, Component componentToRotate) {
        if (isPlayerThere(player)) {
            ShipBuilder ship = player.getShip();
            if (ship != null && componentToRotate != null) {
                componentToRotate = ship.rotateComponentOpposite(componentToRotate);
                // Consider calling updateView() or returning a status if needed
            }
        }
        return componentToRotate;
    }

    public void rotateComponentOppositeForPlayer(Player player, Component componentToRotate) {
        if (isPlayerThere(player)) {
            ShipBuilder ship = player.getShip();
            if (ship != null && componentToRotate != null) {
                ship.rotateComponentOfShipOpposite(componentToRotate);
                // Consider calling updateView() or returning a status if needed
            }
        }
    }

    public void updateView(){}


    // Metodi di supporto



    void printBoardStatus() {
        List<Player> players = gameModel.getFlightBoard().getPlayersInFlightOrder();
        for (Player player : players) {
            int position = gameModel.getFlightBoard().getPosition(player);
            System.out.println(player.getNickName() + " è alla posizione " + position);
        }
    }

    /**
     * Stampa lo stato delle navicelle
     */
    void printShipStatus() {
        List<Player> players = gameModel.getFlightBoard().getPlayersInFlightOrder();
        for (Player p : players) {
            System.out.println("this is the " + p.getNickName() + " ship");
            Map<Component, Position> components = p.getShip().getComponents();
            for (Component component : components.keySet()) {
                System.out.println(components.get(component).toString() + " : " + component.getClass().getSimpleName());
            }
        }

    }


}
