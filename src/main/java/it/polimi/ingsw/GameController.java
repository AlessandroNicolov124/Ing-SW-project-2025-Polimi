package it.polimi.ingsw;

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
        //gameView.startGame();
        //Set private method??
    }
    //TODO Consider instatiating a new player and returning it..
    public void addPlayer(Player player){
        gameModel.addPlayer(player);
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

    public boolean addComponentForPlayer(Player player, Component component, Position pos) {
        if (isPlayerThere(player)) {
            return player.placeComponent(component, pos);
        }
        return false; // Player not found or other issue
    }

    public boolean removeComponentForPlayer(Player player, Component component) {

        if (isPlayerThere(player)) {
            return player.removeComponent(component);
        }
        return false;
    }

    public void pickCardForPlayer(Player player) {
        if (isPlayerThere(player)) {
           gameModel.pickAdventureCardForPlayer(player);
        }
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

    public void playTurn() {
        gameModel.playTurn();
    }

    public boolean validateShipForPlayer(Player player) {
        return player.validateShip();
    }

    //public void

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
