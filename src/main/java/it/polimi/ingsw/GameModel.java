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


    public void startGame() {
        // Carica le carte avventura
        loadAdventureCard();

        // Inizializza il FlightBoard
        flightBoard = new FlightBoard();

        // Imposta la distanza massima per il round corrente
        if (turnNumber == 1) {
            flightBoard.setMaxDistance(8);
        } else if (turnNumber == 2) {
            flightBoard.setMaxDistance(12);
        } else if (turnNumber == 3) {
            flightBoard.setMaxDistance(16);
        }

        // Aggiungi tutti i giocatori al FlightBoard
        for (Player player : players) {
            flightBoard.addPlayer(player);
        }

        // Altre inizializzazioni...
    }

    public void playTurn() {
        // Logica per determinare la potenza del motore del giocatore corrente
        int enginePower = currentPlayer.getShip().calculateEnginePower();

        // Aggiorna la posizione del giocatore sulla FlightBoard
        flightBoard.updatePosition(currentPlayer, enginePower);

        // Ottieni la posizione attuale del giocatore
        int position = flightBoard.getPosition(currentPlayer);

        // Usa la posizione per determinare quale carta avventura il giocatore affronta
        if (position < deck.size()) {
            AdventureCard currentCard = deck.get(position);
            // Gestisci l'effetto della carta avventura...
        }

        // Controlla se il volo è completato
        if (flightBoard.isFlightComplete()) {
            // Calcola i bonus di posizione per il punteggio
            Map<Player, Integer> bonuses = flightBoard.calculateFlightBonuses(turnNumber);

            // Aggiungi i bonus ai crediti dei giocatori
            for (Map.Entry<Player, Integer> entry : bonuses.entrySet()) {
                Player player = entry.getKey();
                int bonus = entry.getValue();
                setCredits(player, getCredits(player) + bonus);
            }

            // ...
        }
    }

    public void endGame(){

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

