package it.polimi.ingsw;

import it.polimi.ingsw.Components.*;

import java.io.Serializable;
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

    public void addComponentForPlayer(Player player, Component component, Position pos) {
        if (isPlayerThere(player)) {
            if(!player.placeComponent(component, pos)){
                System.out.println("Error placing component");
            }
        }
    }

    public void removeComponentForPlayer(Player player, Component component) {
        if (isPlayerThere(player)) {
            player.removeComponent(component);
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

    public void playTurn() {
        gameModel.playTurn();
    }

    public boolean validateShipForPlayer(Player player) {
        return player.validateShip();
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
