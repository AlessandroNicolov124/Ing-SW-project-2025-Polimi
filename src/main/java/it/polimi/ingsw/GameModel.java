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

    public void setCurrentPlayer(Player p){
        this.currentPlayer = p;
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

    public void setTurnNumber(int turnNumber) {
        this.turnNumber = turnNumber;
    }

    public void incrementTurnNumber() {
        turnNumber++;
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

    public void startGame() {
        // Aggiungi tutti i giocatori al FlightBoard
        for (Player player : players) {
            flightBoard.addPlayer(player);
        }

        // Carica le carte avventura
        if (turnNumber == 1) {
            loadAdventureCard();
            //navi piccole e mazzo avventura composto da 8 carte di livello I
//            "Open Space" (Spazio aperto)
//            "Planets" (Pianeti)
//            "Abandoned Station" (Stazione abbandonata)
//            "Abandoned Ship" (Nave abbandonata)
//            "Meteoric Swarm" (Sciame di meteoriti)
//            "Small Combat Zone" (Piccola zona di combattimento)
//            "Smugglers" (Contrabbandieri)
//            "Space Pirates" (Pirati spaziali)
        } else if (turnNumber == 2) {
            // Mazzo con 12 carte di livello I e II
            //Include carte aggiuntive come:
            //"Stardust" (Polvere stellare)
            //"Epidemic" (Epidemia)
            //"Sabotage" (Sabotaggio)
            //"Medium Combat Zone" (Media zona di combattimento)
            //"Meteoric Shower" (Pioggia di meteoriti)
            //"Slave Traders" (Mercanti di schiavi)
            //"Cosmic Dust" (Polvere cosmica)
        } else if (turnNumber == 3) {
            //Mazzo con carte di livello I, II e III
            //Include le carte più difficili come:
            //"Large Combat Zone" (Grande zona di combattimento)
            //"Heavy Meteoric Shower" (Intensa pioggia di meteoriti)
            //"Meteoric Storm" (Tempesta di meteoriti)
            //"Saboteurs" (Sabotatori)
            //"Combat Zone with Enemies" (Zona di combattimento con nemici)
            //"Pirates with Heavy Artillery" (Pirati con artiglieria pesante)
            //"Diplomatic Mission" (Missione diplomatica)
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
        System.out.println("Nuova posizine " + currentPlayer.getNickName() + ": " + position);
    }

    //il gioco è completato quando o è rimasto un solo giocatore oppure tutte le carte avventura sono state risolte
    public boolean isFlightComplete() {
        if(players.stream().filter(p -> p.isActive()).count()==1 || getAdventureDeck().isEmpty()){
            return true;
        }
        return false;

    }


    public void endGame(){

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

