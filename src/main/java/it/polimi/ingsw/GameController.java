package it.polimi.ingsw;

import it.polimi.ingsw.Components.Component;
import java.util.*;

public class GameController {
    private GameModel gameModel;
    private GameView gameView;

    public GameController() {
        gameModel = new GameModel();
    }

    private boolean isPlayerThere(Player player) {
        return gameModel.getPlayers().contains(player);
    }

    private void startGame(){

        gameModel.setNewGameState(GameModel.eGameState.GAME_BUILD_SHIP);

        //Update view...
    }
    //TODO Consider instatiating a new player and returning it..
    public void addPlayer(Player player){
        gameModel.getPlayers().add(player);
    }

    public void removePlayer(Player player){}

    public boolean moveToShipValidation(){
        List<Player> players = gameModel.getPlayers();

        for (Player player : players) {
            if(!player.getShip().isAssembled()){
                return false;
            }
        }

        gameModel.setNewGameState(GameModel.eGameState.GAME_VALIDATE_SHIP);
        return true;
    }

    public void addComponentForPlayer(Player player, Component component, Position pos) {
        if (isPlayerThere(player)) {
            player.getShip().placeComponent(component, pos);
        }
    }

    public void removeComponentForPlayer(Player player, Component component) {

        if (isPlayerThere(player)) {
            player.getShip().removeComponent(component);
        }
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

    public boolean validateShipForPlayer(Player player) {
        return player.getShip().validateShip();
    }

    //public void

    public void updateView(){}



}
