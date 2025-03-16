package it.polimi.ingsw;

import java.util.HashMap;
import java.util.List;

public class GameModel {
    private int turnNumber;
    private String state;
    private Player currentPlayer;
    private List<Player> Players;
    private List<AdventureCard> deck;
    private HashMap<Player, Integer> credits;

    public GameModel() {

    }

    public void startGame(){

    }

    public void endGame(){

    }

    public void playTurn(){

    }

    public int calculatesPoints(){
        return 0;
    }

    public AdventureCard loadAdventureCard(){
        return null;
    }

    public void updateState(String newState){
        state = newState;
    }

    public Player getCurrentPlayer(){
        return currentPlayer;
    }

    public void setCredits(Player p, int amount){
        credits.put(p, amount);
    }

}
