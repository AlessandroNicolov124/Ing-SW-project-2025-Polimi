package it.polimi.ingsw;

import it.polimi.ingsw.Components.*;

import java.util.List;

public class GameController {
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
        gameModel.getPlayers().add(player);
    }

    public void setCredits(Player player, int credits){
        gameModel.setCredits(player, credits);
    }
    public void setCurrentPlayer(Player player){
        gameModel.setCurrentPlayer(player);
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

    public void setPlayerReady(Player player) {player.setReady(true);
    }

    public void playTurn() {
        gameModel.playTurn();
    }

    public boolean validateShipForPlayer(Player player) {
        return player.getShip().validateShip();
    }

    //public void

    public void updateView(){}


    // Metodi di supporto



    void printBoardStatus() {
        List<Player> players = gameModel.getFlightBoard().getPlayersInFlightOrder();
        for (Player player : players) {
            int position = gameModel.getFlightBoard().getPosition(player);
            System.out.println(player.getNickName() + " è alla posizione " + position);
        }
    }
}
