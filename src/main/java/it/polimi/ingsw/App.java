package it.polimi.ingsw;

import it.polimi.ingsw.AdventureCards.*;
import it.polimi.ingsw.Components.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) {

        testGameRound();
    }

    /**
     * Simula un intero round con più giocatori
     */
    private static void testGameRound() {
        System.out.println("=== TEST SIMULAZIONE ROUND DI GIOCO ===");

        GameModel gameModel = new GameModel();

        // Crea alcuni giocatori con navi
        Player player1 = createTestPlayer(1, "Giocatore1", createTestShip());
        Player player2 = createTestPlayer(2, "Giocatore2", createTestShip());
        Player player3 = createTestPlayer(3, "Giocatore3", createTestShip());

        // Aggiungi i giocatori alla flight board
        gameModel.addPlayer(player1);
        gameModel.addPlayer(player2);
        gameModel.addPlayer(player3);

        // Imposta i crediti iniziali
        gameModel.setCredits(player1, 10);
        gameModel.setCredits(player2, 10);
        gameModel.setCredits(player3, 10);

        System.out.println("Stato iniziale del tabellone:");
        printBoardStatus(gameModel.getFlightBoard());

        // Imposta il turno di gioco
        gameModel.setTurnNumber(1);

        // Inizia il gioco
        System.out.println("Inizializzazione del gioco...");
        gameModel.startGame();

        // Stampa informazioni sul mazzo di carte
        System.out.println("Carte avventura caricate: " + gameModel.getAdventureDeck().size());

        // Simula l'esecuzione di alcuni turni
        System.out.print("\nSimulazione di alcuni turni di gioco:");
        for(int i=0; i<3; i++){
            System.out.println("\nTURNO : " + i);
            for(Player player : gameModel.getPlayers()) {
                System.out.println("\nTurno di " + player.getNickName());
                gameModel.setCurrentPlayer(player);

                // Simula una carta pescata
                player.pickCard(gameModel);

                // Simula l'esecuzione del turno
                System.out.println("Potenza motore: " + player.getShip().calculateEnginePower());
                gameModel.playTurn();
            }
        }


        // Simula il completamento del volo
        //il volo finisce quando rimane un solo giocatore, oppure sono state risolte tutte le carte avventura

        // Verifica se il volo è completato
        boolean isComplete = gameModel.isFlightComplete();
        System.out.println("Volo completato: " + isComplete);

        // Se il volo è completato, calcola i bonus
        if (isComplete) {
            Map<Player, Integer> bonuses = gameModel.getFlightBoard().calculateFlightBonuses(gameModel.getTurnNumber());

            System.out.println("\nBonus di posizione assegnati:");
            for (Map.Entry<Player, Integer> entry : bonuses.entrySet()) {
                Player player = entry.getKey();
                int bonus = entry.getValue();

                int oldCredits = gameModel.getCredits(player);
                gameModel.setCredits(player, oldCredits + bonus);

                System.out.println(player.getNickName() + ": +" + bonus + " crediti (Totale: " + gameModel.getCredits(player) + ")");
            }
        }
    }

    /**
     * Testa lo scenario in cui le posizioni sono occupate
     */
    private static void testPositionOccupied() {
        System.out.println("\n=== TEST POSIZIONI OCCUPATE ===");

        FlightBoard flightBoard = new FlightBoard();
        flightBoard.setMaxDistance(8);

        // Crea alcuni giocatori
        Player player1 = createTestPlayer(1, "Giocatore1", createTestShip());
        Player player2 = createTestPlayer(2, "Giocatore2", createTestShip());

        // Aggiungi i giocatori al FlightBoard
        flightBoard.addPlayer(player1);
        flightBoard.addPlayer(player2);

        // Imposta manualmente le posizioni
        flightBoard.updatePosition(player1, 3);

        System.out.println("Posizione di " + player1.getNickName() + ": " + flightBoard.getPosition(player1));

        // Prova a spostare il secondo giocatore nella stessa posizione
        System.out.println("Spostamento di " + player2.getNickName() + " di 3 spazi...");
        flightBoard.updatePosition(player2, 3);

        System.out.println("Posizione di " + player2.getNickName() + ": " + flightBoard.getPosition(player2));
        System.out.println("(Dovrebbe essere 2 perché la posizione 3 è occupata)");
    }

    /**
     * Testa l'aggiornamento delle posizioni in ordine
     */
    private static void testUpdatePositions() {
        System.out.println("\n=== TEST AGGIORNAMENTO POSIZIONI IN ORDINE ===");

        FlightBoard flightBoard = new FlightBoard();
        flightBoard.setMaxDistance(8);

        // Crea alcuni giocatori
        Player player1 = createTestPlayer(1, "Giocatore1", createTestShip());
        Player player2 = createTestPlayer(2, "Giocatore2", createTestShip());
        Player player3 = createTestPlayer(3, "Giocatore3", createTestShip());

        // Aggiungi i giocatori al FlightBoard
        flightBoard.addPlayer(player1);
        flightBoard.addPlayer(player2);
        flightBoard.addPlayer(player3);

        // Imposta manualmente le posizioni iniziali
        flightBoard.updatePosition(player1, 3); // Giocatore1 a posizione 3
        flightBoard.updatePosition(player2, 5); // Giocatore2 a posizione 5
        flightBoard.updatePosition(player3, 2); // Giocatore3 a posizione 2

        System.out.println("Posizioni iniziali:");
        printBoardStatus(flightBoard);

        // Crea una mappa di movimenti
        Map<Player, Integer> playerMoves = new HashMap<>();
        playerMoves.put(player1, 2); // Giocatore1 si muove di 2
        playerMoves.put(player2, 1); // Giocatore2 si muove di 1
        playerMoves.put(player3, 4); // Giocatore3 si muove di 4

        System.out.println("\nMovimenti: " + player1.getNickName() + ": +2, " +
                player2.getNickName() + ": +1, " + player3.getNickName() + ": +4");

        // Aggiorna le posizioni in ordine
        System.out.println("\nAggiornamento posizioni in ordine (dal più avanti al più indietro):");
        flightBoard.updatePositionsInOrder(playerMoves);

        System.out.println("\nPosizioni aggiornate:");
        printBoardStatus(flightBoard);
    }

    /**
     * Testa il calcolo dei bonus di posizione
     */
    private static void testFlightBonuses() {
        System.out.println("\n=== TEST BONUS DI POSIZIONE ===");

        FlightBoard flightBoard = new FlightBoard();
        flightBoard.setMaxDistance(8);

        // Crea alcuni giocatori
        Player player1 = createTestPlayer(1, "Giocatore1", createTestShip());
        Player player2 = createTestPlayer(2, "Giocatore2", createTestShip());
        Player player3 = createTestPlayer(3, "Giocatore3", createTestShip());
        Player player4 = createTestPlayer(4, "Giocatore4", createTestShip());

        // Aggiungi i giocatori al FlightBoard
        flightBoard.addPlayer(player1);
        flightBoard.addPlayer(player2);
        flightBoard.addPlayer(player3);
        flightBoard.addPlayer(player4);

        // Fai arrivare tutti i giocatori alla fine (ma in ordini diversi)
        flightBoard.updatePosition(player1, 8); // Primo arrivato
        flightBoard.updatePosition(player2, 8); // Secondo arrivato
        flightBoard.updatePosition(player3, 8); // Terzo arrivato
        flightBoard.updatePosition(player4, 7); // Non arrivato

        System.out.println("Stato del tabellone alla fine del volo:");
        printBoardStatus(flightBoard);

        // Calcola i bonus per il round 1
        Map<Player, Integer> bonusesRound1 = flightBoard.calculateFlightBonuses(1);
        System.out.println("\nBonus per il Round 1:");
        for (Map.Entry<Player, Integer> entry : bonusesRound1.entrySet()) {
            System.out.println(entry.getKey().getNickName() + ": " + entry.getValue() + " crediti");
        }

        // Calcola i bonus per il round 2
        Map<Player, Integer> bonusesRound2 = flightBoard.calculateFlightBonuses(2);
        System.out.println("\nBonus per il Round 2:");
        for (Map.Entry<Player, Integer> entry : bonusesRound2.entrySet()) {
            System.out.println(entry.getKey().getNickName() + ": " + entry.getValue() + " crediti");
        }

        // Calcola i bonus per il round 3
        Map<Player, Integer> bonusesRound3 = flightBoard.calculateFlightBonuses(3);
        System.out.println("\nBonus per il Round 3:");
        for (Map.Entry<Player, Integer> entry : bonusesRound3.entrySet()) {
            System.out.println(entry.getKey().getNickName() + ": " + entry.getValue() + " crediti");
        }
    }

    // Metodi di supporto

    /**
     * Crea un giocatore di test
     */
    private static Player createTestPlayer(int id, String name, ShipBuilder ship) {
        return new Player(id, name, true, ship);
    }

    /**
     * Crea una nave di test con la potenza del motore specificata
     */
    private static ShipBuilder createTestShip() {
        ShipBuilder ship = new ShipBuilder(5, 5);

        // Aggiungi una cabina alla posizione iniziale
        Position startPos = ship.getStartingPosition();
        Cabin cabin = new Cabin(true);
        if(!ship.placeComponent(cabin, startPos)){
            System.out.println("ERROR placing component cabin");
        }

        // Aggiungi un motore
        Engine engine = new SingleEngine();
        engine.initializeConnectors(new DoubleConnectorType(), new NullConnectorType(),new NullConnectorType(),new NullConnectorType());
        Position enginePos = new Position(startPos.getX(), startPos.getAdjacentPosition(Direction.SOUTH).getY());
        if(!ship.placeComponent(engine, enginePos)){
            System.out.println("ERROR placing component engine");
        }

        return ship;
    }

    /**
     * Stampa lo stato attuale della plancia di volo
     */
    private static void printBoardStatus(FlightBoard board) {
        List<Player> players = board.getPlayersInFlightOrder();
        for (Player player : players) {
            int position = board.getPosition(player);
            System.out.println(player.getNickName() + " è alla posizione " + position);
        }
    }
}
