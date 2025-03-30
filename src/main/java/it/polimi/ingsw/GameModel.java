package it.polimi.ingsw;

import it.polimi.ingsw.AdventureCards.*;

import java.util.*;

public class GameModel {
    private FlightBoard flightBoard;
    private int turnNumber;
    private String state;
    private Player currentPlayer;
    private List<Player> players;
    private List<AdventureCard> deck;
    private HashMap<Player, Integer> credits;

    public GameModel() {
        players = new ArrayList<>();
        deck = new ArrayList<>();
        credits = new HashMap<>();
        flightBoard = new FlightBoard();
    }

    // Metodo per pescare una carta dall'avventura
    public AdventureCard pickAdventureCard() {
        if (!deck.isEmpty()) {
            return deck.remove(0); // Rimuove e restituisce la prima carta
        }
        return null; // Nessuna carta disponibile
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

    public void loadAdventureCard(){
        deck.add(new Planets());
        deck.add(new AbandonedStation());
        deck.add(new AbandonedShip());
        deck.add(new Smugglers());
        deck.add(new OpenSpace());
        deck.add(new MeteorSwarm());
        deck.add(new Stardust());
        deck.add(new BigMeteorSwarm());
        deck.add(new Slavers());
        deck.add(new Pirates());
        deck.add(new Epidemic());
        deck.add(new Sabotage());
        deck.add(new CombactZone());

        System.out.println("Tutte le carte avventura sono state caricate.");

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

    public int getCredits(Player p){
        return credits.get(p);
    }

    public List<AdventureCard> getAdventureDeck(){
        return this.deck;
    }

    public List<Player> getPlayers(){
        return this.players;
    }

}

