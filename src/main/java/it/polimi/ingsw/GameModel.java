package it.polimi.ingsw;

import it.polimi.ingsw.AdventureCards.*;
import it.polimi.ingsw.Components.*;

import java.io.Serializable;
import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.util.stream.Collectors;

public class GameModel {
    private FlightBoard flightBoard;
    private int turnNumber;
    private Player currentPlayer;
    private List<Player> players;
    private List<AdventureCard> deck;
    private HashMap<Player, Integer> credits;
    private Queue<Component> componentDeck;
    private Queue<Component> startingCabinDeck;
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
        Component c = null;
        if(p.getShip().getComponents().isEmpty()){
            //deve essere pescata una starting cabin
            c = startingCabinDeck.poll();
        }
        if(c==null){
            c = componentDeck.poll();
        }
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

    /**
     * Aggiorna la posizione di un giocatore sul tabellone di volo
     * considerando le posizioni già occupate
     *
     * @param player Il giocatore la cui posizione deve essere aggiornata
     * @param spaces Il numero di spazi da muovere (può essere positivo o negativo)
     */
    public void updatePosition(Player player, int spaces) {
        flightBoard.updatePosition(player, spaces);
    }

    private void initializeComponentDeckDefault() {
        // Questa è una semplice implementazione. Potrebbe essere necessario bilanciarla
        // in base alle regole del gioco e alla rarità dei componenti.
        List<Component> allComponents = new ArrayList<>();

        for (int i = 0; i < 4; i++) allComponents.add(createCabin(true));
        Collections.shuffle(allComponents);
        startingCabinDeck = new LinkedList<>(allComponents);

        allComponents = new ArrayList<>();
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
        for (int i = 0; i < 6; i++) allComponents.add(createSpecialSingleCargoHolds());
        for (int i = 0; i < 3; i++) allComponents.add(createBatteryCompartment());
        for (int i = 0; i < 3; i++) allComponents.add(createShieldGenerator());
        for (int i = 0; i < 3; i++) allComponents.add(createBrownLifeSupportSystem());
        for (int i = 0; i < 3; i++) allComponents.add(createPurpleLifeSupportSystem());
        for (int i = 0; i < 5; i++) allComponents.add(createStructuralModule());

        // Mescola la lista e converti in coda
        Collections.shuffle(allComponents);
        componentDeck = new LinkedList<>(allComponents);
    }

    private void initializeComponentDeck() {
        initializeComponentDeckFromFile("component_deck.txt");
    }
    private void initializeComponentDeckFromFile(String filename) {
        List<Component> allComponents = new ArrayList<>();
        List<Component> startingCabins = new ArrayList<>();

        System.out.println("Inizializzazione component deck da file: " + filename);
        try {
            // Debug: mostra dove stiamo cercando
            String currentDir = System.getProperty("user.dir");
            // System.out.println("Directory corrente: " + currentDir);
            Path filePath = Paths.get(currentDir + "/src/main/resources/" + filename);

            if (Files.exists(filePath)) {
                    System.out.println("✓ File trovato: " + filePath.toAbsolutePath());
            }else{
                throw new IOException("File non trovato in nessun percorso");
            }

            // Leggi il file
            List<String> lines = Files.readAllLines(filePath);

            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue; // Salta righe vuote e commenti
                }

                // Parse della riga: "componentType_characteristic1_characteristic2_NESO"
                String[] parts = line.split("_");
                if (parts.length < 1) {
                    System.err.println("Formato riga non valido (manca _): " + line);
                    continue;
                }

                String componentType = parts[0].toLowerCase();
                // Gestione speciale per starting cabins
                if (componentType.equals("cabin") && (parts.length == 2 || parts.length == 3) && parts[1].equals("starting")) {
                    Component component = createComponentWithCharacteristics(componentType, new String[]{"starting"}, "UUUU");
                    if (component != null) {
                        //System.out.println("Componente inizializzato da file: " + filename);
                        startingCabins.add(component);
                    }
                    continue;
                }
                // Controlla se l'ultimo elemento sono i connettori (4 caratteri con solo N, E, S, O, D, U)
                String lastPart = parts[parts.length - 1].toUpperCase();
                String connectors;
                String[] characteristics;

                if (lastPart.length() == 4 && lastPart.matches("[NDUS]+")) {
                    // L'ultimo elemento sono i connettori
                    connectors = lastPart;
                    characteristics = new String[parts.length - 2];
                    for (int i = 1; i < parts.length - 1; i++) {
                        characteristics[i - 1] = parts[i].toLowerCase();
                    }
                } else {
                    // Non ci sono connettori espliciti, usa default
                    connectors = "UUUU";
                    characteristics = new String[parts.length - 1];
                    for (int i = 1; i < parts.length; i++) {
                        characteristics[i - 1] = parts[i].toLowerCase();
                    }
                }

                Component component = createComponentWithCharacteristics(componentType, characteristics, connectors);
                if (component != null) {
                    allComponents.add(component);
                }
            }

            // Mescola e inizializza i deck
            Collections.shuffle(startingCabins);
            startingCabinDeck = new LinkedList<>(startingCabins);

            Collections.shuffle(allComponents);
            componentDeck = new LinkedList<>(allComponents);

            System.out.println("Component deck inizializzato da file: " + filename);
            System.out.println("Starting cabins: " + startingCabins.size());
            System.out.println("Altri componenti: " + allComponents.size());

        } catch (IOException e) {
            System.err.println("Errore nella lettura del file " + filename + ": " + e);
            System.out.println("Inizializzazione con valori di default...");
            initializeComponentDeckDefault();
        }
    }

    // Factory method per creare componenti con caratteristiche e connettori specifici
    private Component createComponentWithCharacteristics(String componentType, String[] characteristics, String connectors) {
        Component component = null;

        // Crea il componente base con le caratteristiche
        switch (componentType) {
            case "cabin":
                if(characteristics.length>0){
                    component = new Cabin(true);
                }else{
                    component = new Cabin(false);
                }
                break;
            case "singleengine":
                component = new SingleEngine();
                break;
            case "doubleengine":
                component = new DoubleEngine();
                break;
            case "singlecannon":
                component = new SingleCannon();
                break;
            case "doublecannon":
                component = new DoubleCannon();
                break;
            case "normalcargoholds":
                // Formato: normalcargoholds_double_NESO o normalcargoholds_triple_NESO
                if (characteristics.length > 0) {
                    CargoHoldsSize size = parseCargoSize(characteristics[0]);
                    component = new NormalCargoHolds(size);
                } else {
                    System.err.println("NormalCargoHolds richiede specifica della dimensione");
                    return null;
                }
                break;
            case "specialcargoholds":
                // Formato: specialcargoholds_single_NESO o specialcargoholds_double_NESO
                if (characteristics.length > 0) {
                    CargoHoldsSize size = parseCargoSize(characteristics[0]);
                    component = new SpecialCargoHolds(size);
                } else {
                    System.err.println("SpecialCargoHolds richiede specifica della dimensione");
                    return null;
                }
                break;
            case "batterycompartment":
                // Formato: batterycompartment_double_NESO o batterycompartment_triple_NESO
                if (characteristics.length >= 1) {
                    BatteryType type = parseBatteryType(characteristics[0]);
                    int maxCharge = (type == BatteryType.DOUBLE) ? 2 : 3;
                    component = new BatteryCompartment(maxCharge, type, 0);
                } else {
                    System.err.println("batterycompartment richiede specifica del tipo");
                    return null;
                }
                break;
            case "shieldgenerator":
                component = new ShieldGenerator();
                break;
            case "lifesupportsystem":
                // Formato: lifesupportsystem_brown_NESO o lifesupportsystem_purple_NESO
                if (characteristics.length > 0) {
                    LifeSupportSystemType type = parseLifeSupportType(characteristics[0]);
                    component = new LifeSupportSystem(type);
                } else {
                    System.err.println("LifeSupportSystem richiede specifica del tipo");
                    return null;
                }
                break;
            case "structuralmodule":
                component = new StructuralModule();
                break;
            default:
                System.err.println("Tipo di componente sconosciuto: " + componentType);
                return null;
        }

        if (component != null) {
            // Imposta i connettori specifici (Nord, Est, Sud, Ovest)
            ConnectorType north = parseConnectorType(connectors.charAt(0));
            ConnectorType east = parseConnectorType(connectors.charAt(1));
            ConnectorType south = parseConnectorType(connectors.charAt(2));
            ConnectorType west = parseConnectorType(connectors.charAt(3));

            component.initializeConnectors(north, east, south, west);

            // Validazione per componenti con regole speciali
            if (!validateComponentConnectors(component, componentType)) {
                System.err.println("Connettori non validi per " + componentType + ": " + connectors);
                return null;
            }
        }

        return component;
    }

    // Valida i connettori per componenti con regole speciali
    private boolean validateComponentConnectors(Component component, String componentType) {
        return switch (componentType) {
            case "singleengine", "doubleengine" -> switch (component) {
                case SingleEngine se -> se.validateEngineDirection(component.getComponentDirection());
                case DoubleEngine de -> de.validateEngineDirection(component.getComponentDirection());
                default -> throw new IllegalStateException("Unexpected value: " + component);
            };
            case "singlecannon", "doublecannon" -> switch (component) {
                case SingleCannon se -> se.validateFiringDirection(component.getComponentDirection());
                case DoubleCannon de -> de.validateFiringDirection(component.getComponentDirection());
                default -> throw new IllegalStateException("Unexpected value: " + component);
            };
            default -> true;
        };
    }

    private CargoHoldsSize parseCargoSize(String sizeStr) {
        switch (sizeStr.toLowerCase()) {
            case "single":
                return CargoHoldsSize.SINGLE;
            case "double":
                return CargoHoldsSize.DOUBLE;
            case "triple":
                return CargoHoldsSize.TRIPLE;
            default:
                System.err.println("Dimensione cargo sconosciuta: " + sizeStr + ", usando DOUBLE");
                return CargoHoldsSize.DOUBLE;
        }
    }

    // Parser per il tipo di batteria
    private BatteryType parseBatteryType(String typeStr) {
        switch (typeStr.toLowerCase()) {
            case "double":
                return BatteryType.DOUBLE;
            case "triple":
                return BatteryType.TRIPLE;
            default:
                System.err.println("Tipo batteria sconosciuto: " + typeStr + ", usando DOUBLE");
                return BatteryType.DOUBLE;
        }
    }

    // Parser per il tipo di life support system
    private LifeSupportSystemType parseLifeSupportType(String typeStr) {
        switch (typeStr.toLowerCase()) {
            case "brown":
                return LifeSupportSystemType.BROWN;
            case "purple":
                return LifeSupportSystemType.PURPLE;
            default:
                System.err.println("Tipo life support sconosciuto: " + typeStr + ", usando BROWN");
                return LifeSupportSystemType.BROWN;
        }
    }

    // Converte il carattere in ConnectorType
    private ConnectorType parseConnectorType(char connectorChar) {
        switch (connectorChar) {
            case 'S':
                return new SingleConnectorType();
            case 'D':
                return new DoubleConnectorType();
            case 'U':
                return new UniversalConnectorType();
            case 'N':
                return new NullConnectorType();
            default:
                System.err.println("Tipo connettore sconosciuto: " + connectorChar + ", usando Null");
                return new NullConnectorType();
        }
    }

    // Factory methods per creare i diversi tipi di componenti
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
        if(!engine.validateEngineDirection(engine.getComponentDirection())){
            //System.out.println("   CONNETTORI NON VALIDI!");
            engine.ensureConnector(Direction.SOUTH, new NullConnectorType());
        }
        return engine;
    }

    private DoubleEngine createDoubleEngine() {
        DoubleEngine engine = new DoubleEngine();
        initializeRandomConnectors(engine);
        if(!engine.validateEngineDirection(engine.getComponentDirection())){
            //System.out.println("   CONNETTORI NON VALIDI!");
            engine.ensureConnector(Direction.SOUTH, new NullConnectorType());
        }
        return engine;
    }

    private SingleCannon createSingleCannon() {
        //il cannone punta con il costruttore sempre verso nord
        SingleCannon cannon = new SingleCannon();
        initializeRandomConnectors(cannon);
        if(!cannon.validateFiringDirection(cannon.getComponentDirection())){
            //System.out.println("   CONNETTORI NON VALIDI!");
            cannon.ensureConnector(Direction.NORTH, new NullConnectorType());
        }
        return cannon;
    }

    private DoubleCannon createDoubleCannon() {
        //il cannone punta con il costruttore sempre verso nord
        DoubleCannon cannon = new DoubleCannon();
        initializeRandomConnectors(cannon);
        if(!cannon.validateFiringDirection(cannon.getComponentDirection())){
            //System.out.println("   CONNETTORI NON VALIDI!");
            cannon.ensureConnector(Direction.NORTH, new NullConnectorType());
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
    private CargoHolds createSpecialSingleCargoHolds() {
        CargoHolds cargoHold = new SpecialCargoHolds(CargoHoldsSize.SINGLE);
        initializeRandomConnectors(cargoHold);
        return cargoHold;
    }

    private BatteryCompartment createBatteryCompartment() {
        // Selezione casuale del tipo di batteria dall'enum
        BatteryType[] types = BatteryType.values();
        BatteryType randomType = types[(int)(Math.random() * types.length)];
        int maxC;
        if(randomType == BatteryType.DOUBLE){
            maxC = 2;
        }else{
            maxC = 3;
        }
        BatteryCompartment battery = new BatteryCompartment(maxC, randomType,0);

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

