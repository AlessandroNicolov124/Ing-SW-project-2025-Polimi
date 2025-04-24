package it.polimi.ingsw;

import it.polimi.ingsw.AdventureCards.*;

import java.util.*;

import static it.polimi.ingsw.GameModel.eGameState.eGAME_MAX_ENUM;

public class GameModel {
    private FlightBoard flightBoard;
    private eGameState gameState;
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
        gameState = eGameState.GAME_IDLE;

        loadAdventureCard();
    }

    public void setNewGameState(eGameState gameState) {
        if (this.gameState != gameState) {
            this.gameState = gameState;
        }
    }

    public eGameState getGameState() {
        return gameState;
    }

    // Metodo per pescare una carta dall'avventura
    public void pickAdventureCardForPlayer(Player player) {
        if (!deck.isEmpty()) {
            player.pickCard(deck.remove(0));
            //return deck.remove(0); // Rimuove e restituisce la prima carta
            //TODO implent different card picking
        }
        else
            player.pickCard(null);
        //return null; // Nessuna carta disponibile
    }

    public int calculatesPoints(){
        return 0;
    }

    private void loadAdventureCard(){
        deck.add(new Planets());
        //deck.add(new AbandonedStation());
        //deck.add(new AbandonedShip());
        //deck.add(new Smugglers());
        deck.add(new OpenSpace());
        deck.add(new MeteorSwarm());
        deck.add(new Stardust());
        //deck.add(new BigMeteorSwarm());
        deck.add(new Slavers());
        deck.add(new Pirates());
        deck.add(new Epidemic());
        deck.add(new Sabotage());
        //deck.add(new CombatZone());

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

    public FlightBoard getFlightBoard(){
        return this.flightBoard;
    }

    public boolean isGameStartable(){
        for (Player p : this.players){
            if (!p.getReady()){
                return false;
            }
        }
        return true;
    }

    public enum eGameState {
        GAME_IDLE,
        GAME_BUILD_SHIP,
        GAME_VALIDATE_SHIP,
        GAME_PLAYING,
        GAME_FINISHED,
        eGAME_MAX_ENUM
    }

}

