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
        //gameModel.startGame();
        //gameView.startGame();
        //Set private method??
    }
    //TODO Consider intatiating a new player and returning it..
    public void addPlayer(Player player){
        gameModel.getPlayers().add(player);
    }

    public void setCredits(Player player, int credits){
        gameModel.setCredits(player, credits);
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


    // Metodi di supporto

    public void testGameRound() {
        System.out.println("=== TEST SIMULAZIONE ROUND DI GIOCO ===");

        // Crea alcuni giocatori con navi
        Player player1 = createTestPlayer(1, "Giocatore1", createTestShip());
        Player player2 = createTestPlayer(2, "Giocatore3", createTestShip());
        Player player3 = createTestPlayer(3, "Giocatore3", createTestShip());

        // Aggiungi i giocatori alla flight board
        addPlayer(player1);
        addPlayer(player2);
        addPlayer(player3);

        // Imposta i crediti iniziali
        setCredits(player1, 10);
        setCredits(player2, 10);
        setCredits(player3, 10);

        System.out.println("Stato iniziale del tabellone:");
        printBoardStatus();

        // Imposta il turno di gioco
        gameModel.setTurnNumber(1);

        // Inizia il gioco
        System.out.println("Inizializzazione del gioco...");
        gameModel.startGame();

        // Stampa informazioni sul mazzo di carte
        System.out.println("Carte avventura caricate: " + gameModel.getAdventureDeck().size());

        // Simula l'esecuzione di alcuni voli
        System.out.print("\nSimulazione di alcuni voli:");
        for (int i = 0; i < 3; i++) {
            System.out.println("\nVOLO : " + i);
            for (Player player : gameModel.getPlayers()) {
                System.out.println("\nTurno di " + player.getNickName());
                gameModel.setCurrentPlayer(player);

                // Simula una carta pescata
                pickCardForPlayer(player);

                // Simula l'esecuzione del turno
                System.out.println("Potenza motore: " + player.getShip().calculateEnginePower());
                gameModel.playTurn();
            }
        }

    }

    /**
     * Crea un giocatore di test
     */
    static Player createTestPlayer(int id, String name, ShipBuilder ship) {
        return new Player(id, name, true, ship);
    }

    /**
     * Crea una nave di test con la potenza del motore specificata
     */
    ShipBuilder createTestShip() {
        ShipBuilder ship = new ShipBuilder(10, 10);

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
    void printBoardStatus() {
        List<Player> players = gameModel.getFlightBoard().getPlayersInFlightOrder();
        for (Player player : players) {
            int position = gameModel.getFlightBoard().getPosition(player);
            System.out.println(player.getNickName() + " è alla posizione " + position);
        }
    }
}
