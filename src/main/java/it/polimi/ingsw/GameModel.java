package it.polimi.ingsw;

import it.polimi.ingsw.AdventureCards.*;
import it.polimi.ingsw.Components.*;

import java.io.Serializable;
import java.util.*;

public class GameModel {
    private FlightBoard flightBoard;
    private int turnNumber;
    private Player currentPlayer;
    private List<Player> players;
    private List<AdventureCard> deck;
    private HashMap<Player, Integer> credits;
    private Queue<Component> componentDeck;
    private final Random random;

    public void addPlayer(Player player) {
        players.add(player);
        flightBoard.addPlayer(player);
    }

    public GameModel() {
        players = new ArrayList<>();
        deck = new ArrayList<>();
        credits = new HashMap<>();
        flightBoard = new FlightBoard();
        this.turnNumber = 1;
        random = new Random();
        this.initializeComponentDeck();
    }

    // Metodo per pescare una carta dall'avventura
    public void pickAdventureCardForPlayer(Player player) {
        if (!deck.isEmpty()) {
            player.pickCard(deck.remove(0));
            //return deck.remove(0); // Rimuove e restituisce la prima carta
            //TODO implement different card picking
        }
        else
            player.pickCard(null);
        //return null; // Nessuna carta disponibile
    }

    private void loadAdventureCardTurn1(){
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

    public Component pickOneComponentCardFromDeck(Player p)
    {
        Component c = componentDeck.poll();
        return c;
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

    /**
     * Sets the initial starting position for a given player.
     *
     * @param player The player choosing the position.
     * @param position The chosen starting position (1-4).
     * @return true if the position was set successfully, false otherwise.
     */
    public boolean setPlayerInitialStartingPosition(Player player, int position) {
        if (players.contains(player)) {
            return flightBoard.setInitialPosition(player, position);
        }
        System.err.println("Player not found in game model when trying to set initial position.");
        return false;
    }

    /**
     * Gets the list of available starting positions from the flight board.
     *
     * @return A list of integers representing available starting positions (1-4).
     */
    public List<Integer> getAvailableStartingPositions() {
        return flightBoard.getAvailableStartingPositions();
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


        //TODO This thing is ambiguous ... try to generalize and perhaps avoid magic numbers
        if (turnNumber == 1) {
            loadAdventureCardTurn1();
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

    public int getTurnNumber() {
        return turnNumber;
    }

    public void updatePosition(Player player, int i) {
        flightBoard.updatePosition(player, i);
    }

    private void initializeComponentDeck() {
        // Questa è una semplice implementazione. Potrebbe essere necessario bilanciarla
        // in base alle regole del gioco e alla rarità dei componenti.
        List<Component> allComponents = new ArrayList<>();

        // Aggiungi un mix di componenti (potrebbe essere configurato in base al livello di difficoltà)
        // TODO add defines or const values???
        for (int i = 0; i < 4; i++) allComponents.add(createCabin(false));
        for (int i = 0; i < 8; i++) allComponents.add(createSingleEngine());
        for (int i = 0; i < 4; i++) allComponents.add(createDoubleEngine());
        for (int i = 0; i < 8; i++) allComponents.add(createSingleCannon());
        for (int i = 0; i < 4; i++) allComponents.add(createDoubleCannon());
        for (int i = 0; i < 6; i++) allComponents.add(createNormalDoubleCargoHolds());
        for (int i = 0; i < 6; i++) allComponents.add(createNormalTripleCargoHolds());
        for (int i = 0; i < 6; i++) allComponents.add(createSpecialDoubleCargoHolds());
        for (int i = 0; i < 6; i++) allComponents.add(createSpecialTripleCargoHolds());
        for (int i = 0; i < 3; i++) allComponents.add(createBatteryCompartment());
        for (int i = 0; i < 3; i++) allComponents.add(createShieldGenerator());
        for (int i = 0; i < 3; i++) allComponents.add(createBrownLifeSupportSystem());
        for (int i = 0; i < 3; i++) allComponents.add(createPurpleLifeSupportSystem());
        for (int i = 0; i < 5; i++) allComponents.add(createStructuralModule());

        // Mescola la lista e converti in coda
        Collections.shuffle(allComponents);
        componentDeck = new LinkedList<>(allComponents);
    }
    private Cabin createCabin(boolean isStarting) {
        Cabin cabin = new Cabin(isStarting);
        if(!isStarting){
            // Inizializza i connettori in modo casuale o predefinito
            initializeRandomConnectors(cabin);
        }
        return cabin;
    }

    private SingleEngine createSingleEngine() {
        SingleEngine engine = new SingleEngine();
        initializeRandomConnectors(engine);
        while(!engine.validateEngineDirection(engine.getComponentDirection())){
            engine.deleteConnection();
            initializeRandomConnectors(engine);
        }
        return engine;
    }

    private DoubleEngine createDoubleEngine() {
        DoubleEngine engine = new DoubleEngine();
        initializeRandomConnectors(engine);
        while(!engine.validateEngineDirection(engine.getComponentDirection())){
            engine.deleteConnection();
            initializeRandomConnectors(engine);
        }
        return engine;
    }

    private SingleCannon createSingleCannon() {
        //il cannone punta con il costruttore sempre verso nord
        SingleCannon cannon = new SingleCannon();
        initializeRandomConnectors(cannon);
        while(!cannon.validateFiringDirection(cannon.getComponentDirection())){
            cannon.deleteConnection();
            initializeRandomConnectors(cannon);
        }
        return cannon;
    }

    private DoubleCannon createDoubleCannon() {
//        Direction direction = getRandomDirection();
//        DoubleCannon cannon = new DoubleCannon(direction);
//        initializeRandomConnectors(cannon);
//        return cannon;
        //il cannone punta con il costruttore sempre verso nord
        DoubleCannon cannon = new DoubleCannon();
        initializeRandomConnectors(cannon);
        while(!cannon.validateFiringDirection(cannon.getComponentDirection())){
            cannon.deleteConnection();
            initializeRandomConnectors(cannon);
        }
        return cannon;
    }

    private CargoHolds createNormalDoubleCargoHolds() {
        CargoHolds cargoHold = new NormalCargoHolds(CargoHoldsSize.DOUBLE);
        initializeRandomConnectors(cargoHold);
        return cargoHold;
    }
    private CargoHolds createNormalTripleCargoHolds() {
        CargoHolds cargoHold = new NormalCargoHolds(CargoHoldsSize.TRIPLE);
        initializeRandomConnectors(cargoHold);
        return cargoHold;
    }

    private CargoHolds createSpecialDoubleCargoHolds() {
        CargoHolds cargoHold = new SpecialCargoHolds(CargoHoldsSize.DOUBLE);
        initializeRandomConnectors(cargoHold);
        return cargoHold;
    }
    private CargoHolds createSpecialTripleCargoHolds() {
        CargoHolds cargoHold = new SpecialCargoHolds(CargoHoldsSize.TRIPLE);
        initializeRandomConnectors(cargoHold);
        return cargoHold;
    }

    private BatteryCompartment createBatteryCompartment() {
        BatteryCompartment battery = new BatteryCompartment();
        initializeRandomConnectors(battery);
        return battery;
    }

    private ShieldGenerator createShieldGenerator() {
        ShieldGenerator shield = new ShieldGenerator();
        initializeRandomConnectors(shield);
        return shield;
    }

    private LifeSupportSystem createPurpleLifeSupportSystem() {
        LifeSupportSystem lifeSupportSystem = new LifeSupportSystem(LifeSupportSystemType.PURPLE);
        initializeRandomConnectors(lifeSupportSystem);
        return lifeSupportSystem;
    }
    private LifeSupportSystem createBrownLifeSupportSystem() {
        LifeSupportSystem lifeSupportSystem = new LifeSupportSystem(LifeSupportSystemType.BROWN);
        initializeRandomConnectors(lifeSupportSystem);
        return lifeSupportSystem;
    }

    private StructuralModule createStructuralModule() {
        StructuralModule structuralModule = new StructuralModule();
        initializeRandomConnectors(structuralModule);
        return structuralModule;
    }

    private Direction getRandomDirection() {
        Direction[] directions = Direction.values();
        return directions[random.nextInt(directions.length)];
    }

    private void initializeRandomConnectors(Component component) {
        // Inizializza i connettori in modo casuale
        // Questa è una versione semplificata, potresti voler implementare
        // regole più complesse per la generazione dei connettori
        ConnectorType[] possibleConnectors = {
                new SingleConnectorType(),
                new DoubleConnectorType(),
                new UniversalConnectorType(),
                new NullConnectorType()
        };

        ConnectorType north = possibleConnectors[random.nextInt(possibleConnectors.length)];
        ConnectorType east = possibleConnectors[random.nextInt(possibleConnectors.length)];
        ConnectorType south = possibleConnectors[random.nextInt(possibleConnectors.length)];
        ConnectorType west = possibleConnectors[random.nextInt(possibleConnectors.length)];

        component.initializeConnectors(north, east, south, west);
    }

}

