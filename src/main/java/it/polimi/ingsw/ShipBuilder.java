package it.polimi.ingsw;

import it.polimi.ingsw.Components.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ShipBuilder {

    private final Position startingPosition = new Position(3, 2);
    private int width, height; //dimensioni plancia
    private Map<Component, Position> components;
    private boolean isAssembled;
    private Map<CrewType, Integer> crewCount = new HashMap<>(); // tiene conto del numero di umani e alieni sulla navicella

    public ShipBuilder(int width, int height) {
        this.components = new HashMap<>();
        this.isAssembled = false;
        this.width = width;
        this.height = height;
    }

    public void setAssembled(boolean isAssembled) {
        this.isAssembled = isAssembled;
    }

    //posizione allinterno della plancia
    public boolean isPositionValid(Position position) {

        boolean[][] VALID_SQUARES = {
                {false, false, true,  false, true,  false, false},
                {false, true,  true,  true,  true,  true,  false},
                {true,  true,  true,  true,  true,  true,  true },
                {true,  true,  true,  true,  true,  true,  true },
                {true, true,  true,  false, true,  true,  true },
        };

        if(position.getX() >= 0 && position.getX() < width &&
                position.getY() >= 0 && position.getY() < height){
            return VALID_SQUARES[position.getY()][position.getX()];
        }else {
            return false; // fuori dai limiti della plancia
        }

    }

    public boolean isPositionOccupied(Position position) {
        return components.containsValue(position);
    }

    public boolean placeComponent(Component component, Position position) {
        // Verifica se la posizione è già occupata
        if (isPositionOccupied(position)) {
            System.out.println("  position occupied");
            return false;
        }

        // Se è il primo componente, deve essere posizionato nella posizione iniziale
        if (components.isEmpty() && !position.equals(this.startingPosition)) {
            return false;
        }
        //TODO ALE: Cancellare
        // Se non è il primo componente, deve essere collegato a un componente esistente
//        if (!components.isEmpty()) {
//
//            Position adjPos = null;
//            switch(component){
//                case Engine e:
//                    Direction engineDir = e.getComponentDirection();
//                    Direction opposite = switch(engineDir) {
//                        case NORTH -> Direction.SOUTH;
//                        case SOUTH -> Direction.NORTH;
//                        case EAST  -> Direction.WEST;
//                        case WEST  -> Direction.EAST;
//                    };
//                    Position behind = position.getAdjacentPosition(opposite);
//                    if (isPositionValid(behind) && isPositionOccupied(behind)) {
//                        System.out.println("Cannot place Engine: cell immediately behind (" + behind + ") is occupied");
//                        return false;
//                    }
//                    break;
//                case Cannon c: {
//                    Direction cannonDir = c.getComponentDirection();
//                    Position ahead = position.getAdjacentPosition(cannonDir);
//                    if (isPositionValid(ahead) && isPositionOccupied(ahead)) {
//                        System.out.println("  Cannot place Cannon: cell immediately ahead (" + ahead + ") is occupied");
//                        return false;
//                    }
//                    break;
//                }
//                default:
//                    break;
//            }
//
//            boolean isConnected = true;
//
//            for (Direction direction : Direction.values()) {
//                Position adjacentPosition = direction.getAdjacentPosition(position);
//                Component adjacentComponent = getComponentAt(adjacentPosition);
//
//                if (adjacentComponent != null && !component.canConnectTo(adjacentComponent, direction)) {
//                    isConnected = false;
//                    break;
//                }
//            }
//
//            if (!isConnected) {
//                System.out.print(" not connected, ");
//                return false;
//            }
//        }

        // Posiziona il componente
        components.put(component, position);
        return true;
    }

    private boolean wouldDisconnectShip(Component componentToRemove) {
        if (components.size() <= 1) {
            return false; // Only one component, can't disconnect
        }

        //Position positionToRemove = components.get(componentToRemove);
        Map<Component, Position> tempComponents = new HashMap<>(components);
        tempComponents.remove(componentToRemove);

        // Use BFS to check if all components are still connected
        Set<Component> visited = new HashSet<>();
        Queue<Component> queue = new LinkedList<>();

        // Start BFS from the first component
        Component firstComponent = tempComponents.keySet().iterator().next();
        queue.add(firstComponent);
        visited.add(firstComponent);

        while (!queue.isEmpty()) {
            Component current = queue.poll();
            Position currentPos = tempComponents.get(current);

            // Check all adjacent positions
            for (Direction direction : Direction.values()) {
                Position adjacentPos = direction.getAdjacentPosition(currentPos);
                Component adjacentComponent = null;

                // Find component at this position
                for (Map.Entry<Component, Position> entry : tempComponents.entrySet()) {
                    if (entry.getValue().equals(adjacentPos)) {
                        adjacentComponent = entry.getKey();
                        break;
                    }
                }

                if (adjacentComponent != null && !visited.contains(adjacentComponent)) {
                    visited.add(adjacentComponent);
                    queue.add(adjacentComponent);
                }
            }
        }

        // If visited components count equals all components, ship is still connected
        return visited.size() != tempComponents.size();
    }

    // Metodo per rimuovere un componente dalla nave
    public boolean removeComponent(Component component) {
        if (!components.containsKey(component)) {
            return false;
        }

       switch(component) {
           case Cabin cabin -> {
               if(cabin.isOccuped()){
                   removeCrewFromCabin(cabin.getCrew().getFirst(), cabin);
               }
               // TODO verificare che il giocatore abbia ancora umani altrimenti ha perso
           }
//           case LifeSupportSystem lifeSupport -> {
//               disconnectLifeSupportConnections(lifeSupport);
//           }
           default -> { /* Other components can be removed */ }
       }

        // Remove the component
        components.remove(component);
        return true;
    }

    private boolean areAllComponentsConnected() {
        if (components.isEmpty()) {
            return true;
        }

        Set<Component> visited = new HashSet<>();
        Queue<Component> queue = new LinkedList<>();

        // Start BFS from the first component
        Component firstComponent = components.keySet().iterator().next();
        queue.add(firstComponent);
        visited.add(firstComponent);

        while (!queue.isEmpty()) {
            Component current = queue.poll();
            Position currentPos = components.get(current);

            // Check all adjacent positions
            for (Direction direction : Direction.values()) {
                Position adjacentPos = direction.getAdjacentPosition(currentPos);
                Component adjacentComponent = getComponentAt(adjacentPos);

                if (adjacentComponent != null && !visited.contains(adjacentComponent)) {
                    if (current.canConnectTo(adjacentComponent, direction)) {
                        visited.add(adjacentComponent);
                        queue.add(adjacentComponent);
                    }
                }
            }
        }

        // If visited components count equals all components, all are connected
        return visited.size() == components.size();
    }

    // Convalida la nave per assicurarsi che tutti i componenti necessari siano presenti
    public boolean validateShip() {
        if (components.isEmpty()) {
            System.out.println("No components");
            return false;
        }

        // 1. Check if all components are connected
        if (!areAllComponentsConnected()) {
            System.out.println("Components not connected");
            return false;
        }

        // 2. Check for Cannon
        boolean validationCannonFire = checkCannonFire();

        // 3. Check for required components
        //boolean hasEngine = components.keySet().stream().anyMatch(c -> c instanceof Engine);
        //boolean hasCabin = components.keySet().stream().anyMatch(c -> c instanceof Cabin);
        //isAssembled = hasEngine && hasCabin && validationCannonFire; // La nave è assemblata se ha almeno un motore e una cabina

        long hasEngine = components.keySet().stream()
                .flatMap(component -> switch (component) {
                    case Engine e -> Stream.of(e);
                    default -> Stream.empty();
                })
                .count();
        long hasCabin = components.keySet().stream()
                .flatMap(component -> switch (component) {
                    case Cabin c -> Stream.of(c);
                    default -> Stream.empty();
                })
                .count();

        boolean validationEngineFire = checkEngineFire();

        // La nave è assemblata se ha almeno un motore e una cabina
        boolean isValid = hasEngine > 0 && hasCabin > 0 && validationEngineFire && validationCannonFire;

        //System.out.println("Ship Validation: " + isValid);
        return isValid;
    }

    public boolean checkCannonFire() {
        List<SingleCannon> singleCannons = getOperationalSingleCannons();
        List<DoubleCannon> doubleCannons = getOperationalDoubleCannons();
        for (SingleCannon singleCannon : singleCannons) {
            Position inFiringDirection = getPositionOf(singleCannon).getAdjacentPosition(singleCannon.getComponentDirection());
            if (getAllPositions().contains(inFiringDirection)) {
                return false;
            }
        }
        for (DoubleCannon dc : doubleCannons) {
            Position inFiringDirection = getPositionOf(dc).getAdjacentPosition(dc.getComponentDirection());
            if (getAllPositions().contains(inFiringDirection)) {
                return false;
            }
        }
        return true;
    }

    public boolean checkEngineFire() {
        List<SingleEngine> singleEngines = getOperationalSingleEngines();
        List<DoubleEngine> doubleEngines = getOperationalDoubleEngines();
        for (SingleEngine se : singleEngines) {
            Position inOppositeDirection = getPositionOf(se).getAdjacentPosition(se.getComponentDirection().getOpposite());
            if (getAllPositions().contains(inOppositeDirection)) {
                return false;
            }
        }
        for (DoubleEngine dc : doubleEngines) {
            Position inOppositeDirection = getPositionOf(dc).getAdjacentPosition(dc.getComponentDirection().getOpposite());
            if (getAllPositions().contains(inOppositeDirection)) {
                return false;
            }
        }
        return true;
    }

    // Restituisce i componenti della nave
    public Map<Component, Position> getComponents() {
        return components;
    }

    //restituisce il component nella posizone specificata
    public Component getComponentAt(Position position) {
        for (Map.Entry<Component, Position> entry : components.entrySet()) {
            if (entry.getValue().equals(position)) {
                return entry.getKey();
            }
        }
        return null;
    }

    //restituisce la posizione del component specificato
    public Position getPositionOf(Component component) {
        return components.get(component);
    }

    public List<Component> getAllComponents() {
        return new ArrayList<>(components.keySet());
    }

    public List<Position> getAllPositions() {
        return new ArrayList<>(components.values());
    }


    // Restituisce lo stato di assemblaggio della nave
    public boolean isAssembled() {
        return isAssembled;
    }

    public Position getStartingPosition() {
        return startingPosition;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    //se il component adiancente è una cabina, aggiorna il set di LifeSupportConnections
    public void updateLifeSupportConnections(LifeSupportSystem lifeSupport) {
        Position position = getPositionOf(lifeSupport);
        LifeSupportSystemType type = lifeSupport.getType();

        // Check adjacent positions for cabins
        for (Direction dir : Direction.values()) {
            Position adjacentPos = dir.getAdjacentPosition(position);
            Component adjacentComponent = getComponentAt(adjacentPos);

            switch (adjacentComponent) {
                case Cabin cabin -> {
                    if (lifeSupport.canConnectTo(adjacentComponent, dir)) {
                        cabin.connectLifeSupport(type);
                    }
                }
                case null -> {
                    // Non c'è un componente adiacente, non fare nulla
                }
                default -> {
                }
            }
        }
    }

    //se il component adiancente è una cabina, aggiorna il set di LifeSupportConnections
    public void disconnectLifeSupportConnections(LifeSupportSystem lifeSupport) {
        Position position = getPositionOf(lifeSupport);
        LifeSupportSystemType type = lifeSupport.getType();

        // Check adjacent positions for cabins
        for (Direction dir : Direction.values()) {
            Position adjacentPos = dir.getAdjacentPosition(position);
            Component adjacentComponent = getComponentAt(adjacentPos);

            switch (adjacentComponent) {
                case Cabin cabin -> {
                    if (lifeSupport.canConnectTo(adjacentComponent, dir)) {
                        cabin.disconnectLifeSupport(type);
                    }
                }
                case null -> {
                    // Non c'è un componente adiacente, non fare nulla
                }
                default -> {
                }
            }
        }
    }


    public boolean addCrewToCabin(Crew crew, Cabin cabin) {
        // Verifica limiti a livello di nave per gli alieni
        if (crew.getType() != CrewType.HUMAN) {
            if (crewCount.getOrDefault(crew.getType(), 0) >= 1) {
                return false;
            }
        }

        // Prova ad aggiungere l'equipaggio
        if (cabin.addCrew(crew)) {
            //if (crew.getType() != CrewType.HUMAN) {
                crewCount.put(crew.getType(), crewCount.getOrDefault(crew.getType(), 0) + 1);
            //}
            return true;
        }

        return false;
    }

    public int getHumanCount() {
        return crewCount.getOrDefault(CrewType.HUMAN, 0);
    }

    public int getAlienCount() {
        return getTotalCrewCount() - getHumanCount();
    }

    public int getCrewCount(CrewType type) {
        return crewCount.getOrDefault(type, 0);
    }

    public int getTotalCrewCount() {
        return crewCount.values().stream().mapToInt(Integer::intValue).sum();
    }

    public boolean removeCrewFromCabin(Crew crew, Cabin cabin) {
        if (cabin.removeCrew(crew)) {
            int currentCount = crewCount.getOrDefault(crew.getType(), 0);
            if (currentCount > 0) {
                crewCount.put(crew.getType(), currentCount - 1);
            }
            return true;
        }
        return false;
    }

    /**
     * Rimuove un numero specificato di membri dell'equipaggio dalla nave.
     * Rimuove equipaggio dalle cabine in modo sequenziale fino a raggiungere il numero richiesto.
     *
     * @param count Il numero di membri dell'equipaggio da rimuovere
     * @return Il numero effettivo di membri dell'equipaggio rimossi
     */
    public int removeCrewMembers(int count) {
//        if (count <= 0) {
//            return 0;  // Nessun membro da rimuovere
//        }
//
//        int removedCount = 0;
//        List<Cabin> cabinsWithCrew = getCabinsWithCrew();  // Ottieni tutte le cabine occupate
//
//        for (Cabin cabin : cabinsWithCrew) {
//            if (removedCount >= count) {
//                break;  // Abbiamo rimosso il numero richiesto di membri dell'equipaggio
//            }
//
//            List<Crew> crewToRemove = cabin.getCrew();  // Ottieni il membro dell'equipaggio nella cabina
//            if (crewToRemove != null) {
//                for (Crew crew : crewToRemove) {
//                    if (removeCrewFromCabin(crew, cabin)) {
//                        removedCount++;
//                    }
//                }
//            }
//        }
//
//        return removedCount;

        int remainingToRemove = count;

        // First try to remove aliens
        int aliensToRemove = Math.min(getAlienCount(), remainingToRemove);
        if (aliensToRemove > 0) {
            remainingToRemove -= removeAliens(aliensToRemove);
        }

        // Then remove humans if needed
        if (remainingToRemove > 0) {
            remainingToRemove -= removeHumans(Math.min(getHumanCount(), remainingToRemove));
        }

        return count - remainingToRemove; // Ritorna il numero di membri rimossi
    }

    /**
     * Metodo di supporto che restituisce una lista di tutte le cabine che contengono equipaggio,
     * prima quelle che contengono alieni poi umani
     *
     * @return Lista di cabine occupate da membri dell'equipaggio
     */
    private List<Cabin> getCabinsWithCrew() {
        List<Cabin> cabinsWithCrew = new ArrayList<>();

        // Assumo che tu abbia una collezione di cabine da qualche parte nella classe
        // Sostituisci 'allCabins' con la tua variabile effettiva
        List<Cabin> allCabins = getAllCabins();
        for (Cabin cabin : allCabins) {
            if (!cabin.getCrew().isEmpty()) {
                cabinsWithCrew.add(cabin);
            }
        }

        // Ordina la lista in base alla priorità di rimozione
        // Ad esempio, per rimuovere prima gli alieni:
        cabinsWithCrew.sort((cabin1, cabin2) -> {
            CrewType type1 = cabin1.getCrew().getFirst().getType();
            CrewType type2 = cabin2.getCrew().getFirst().getType();

            if (type1 != CrewType.HUMAN && type2 == CrewType.HUMAN) {
                return -1;  // Alieni prima
            } else if (type1 == CrewType.HUMAN && type2 != CrewType.HUMAN) {
                return 1;   // Umani dopo
            }
            return 0;       // Stesso tipo, ordine invariato
        });

        return cabinsWithCrew;
    }

    /**
     * Rimuove il numero specificato di membri dell'equipaggio di un certo tipo
     *
     * @param crewType Il tipo di equipaggio da rimuovere
     * @param count    Il numero di membri da rimuovere
     * @return Il numero effettivo di membri rimossi
     */
    public int removeCrewOfType(CrewType crewType, int count) {
        if (count <= 0) {
            return 0;
        }

        int removedCount = 0;
        List<Cabin> allCabins = getAllCabins();

        for (Cabin cabin : allCabins) {
            if (removedCount >= count) break;
            // Crea una copia della lista per evitare ConcurrentModificationException
            List<Crew> crewList = new ArrayList<>(cabin.getCrew());
            for (Crew crew : crewList) {
                if (removedCount >= count) break;
                if (crew.getType() == crewType) {
                    if (removeCrewFromCabin(crew, cabin)) {
                        removedCount++;
                    }
                }
            }
        }

        return removedCount;
    }


    /**
     * Aggiunge membri dell'equipaggio del tipo specificato
     * Usato principalmente per ripristinare l'equipaggio rimosso erroneamente
     * @param crewType Il tipo di equipaggio da aggiungere
     * @param count Il numero di membri da aggiungere
     */
    public void addCrewOfType(CrewType crewType, int count) {
        if (count <= 0) return;

        List<Cabin> allCabins = getAllCabins();
        int added = 0;

        if (crewType != CrewType.HUMAN) {
            // Solo un alieno per nave
            if (crewCount.getOrDefault(crewType, 0) >= 1) return;
            for (Cabin cabin : allCabins) {
                Crew alien = switch (crewType) {
                    case BROWN_ALIEN -> new BrownAlienCrew();
                    case PURPLE_ALIEN -> new PurpleAlienCrew();
                    default -> null;
                };
                if (alien != null && addCrewToCabin(alien, cabin)) {
                    added++;
                    //crewCount.put(crewType, crewCount.getOrDefault(crewType, 0) + 1);
                    break; // Solo uno per nave
                }

            }
            if (added < count) {
                System.out.println("Attenzione: non è stato possibile aggiungere tutti gli alieni richiesti (" + count + "). Aggiunti: " + added);
            }
            return;
        }

        // Per gli umani, aggiungi fino al numero richiesto
        while (added < count && !allCabins.isEmpty()) {
            // Prova ad aggiungere umani in ogni cabina
            for (Cabin cabin : allCabins) {
                if(!(cabin.getCrew().size() >= 2)) {// Se la cabina ha già 2 umani la salto
                    if (addCrewToCabin(new HumanCrew(), cabin)) {
                        added++;
                        //crewCount.put(crewType, crewCount.getOrDefault(crewType, 0) + 1);
                    }
                }
                if (added >= count) break;
            }
        }
        if (added < count) {
            System.out.println("Attenzione: non è stato possibile aggiungere tutti gli umani richiesti (" + count + "). Aggiunti: " + added);
        }
    }


//    Metti 2 umani nella tua cabina di partenza.
//            • Metti in ogni altra cabina 2 umani o 1 alieno. Puoi scegliere
//    un alieno solo se la cabina è interconnessa ad un modulo
//    di supporto vitale del corrispondente colore.
//            • La tua nave non può ospitare più di 1 alieno per ciascun
//    colore.
    public void distributeCrewAutomatically() {
        for (Component component : components.keySet()) {
            switch (component) {
                case Cabin cabin -> {
                    if (!cabin.initializeCrewStartingCabin()) {
                        // Logica per distribuire l'equipaggio...
                        boolean crewAdded = false;

                        // Prova con alieno viola
                        if (cabin.hasConnectedLifeSupport(LifeSupportSystemType.PURPLE) &&
                                crewCount.getOrDefault(CrewType.PURPLE_ALIEN, 0) == 0) {
                            crewAdded = addCrewToCabin(new PurpleAlienCrew(), cabin);
                        }

                        // Prova con alieno marrone
                        if (!crewAdded && cabin.hasConnectedLifeSupport(LifeSupportSystemType.BROWN) &&
                                crewCount.getOrDefault(CrewType.BROWN_ALIEN, 0) == 0) {
                            crewAdded = addCrewToCabin(new BrownAlienCrew(), cabin);
                        }

                        // Se non è stato aggiunto un alieno, aggiungi umani
                        if (!crewAdded) {
                            addCrewToCabin(new HumanCrew(), cabin);
                            addCrewToCabin(new HumanCrew(), cabin);
                        }
                    }
                }
                default -> {                }
            }
        }
    }

    //Carica i cannoni doppi della nave usando le batterie disponibili.
    public int chargeWeapons() {
        int chargedCannons = 0;

        List<BatteryCompartment> availableBatteries = getChargedBatteries();

        if (availableBatteries.isEmpty()) {
            return 0;
        }

        List<DoubleCannon> doubleCannons = getOperationalDoubleCannons();
        for (DoubleCannon cannon : doubleCannons) {
            // Trovo una batteria con carica sufficiente
            for (BatteryCompartment battery : availableBatteries) {
                if (cannon.charge(battery)) {
                    chargedCannons++;
                    break;
                }
            }
        }

//        List<DoubleCannon> doubleCannons = getOperationalDoubleCannons();
//        for (DoubleCannon cannon : doubleCannons) {
//            if(chargeWeapon(cannon)){
//                chargedCannons++;
//            }
//        }

        return chargedCannons;
    }

    public boolean chargeWeapon(DoubleCannon cannon) {
        List<BatteryCompartment> availableBatteries = getChargedBatteries();

        if (availableBatteries.isEmpty()) {
            return false;
        }

        for (BatteryCompartment battery : availableBatteries) {
            if (cannon.charge(battery)) {
                return true;
            }
        }

        return true;
    }

    public int calculateTotalBatteryCharge(){
        return getChargedBatteries().stream().mapToInt(BatteryCompartment::getChargeLevel).sum();
    }

    public double calculateFirePower() {
        double totalFirePower = components.keySet().stream()
                .mapToDouble(component -> switch(component){
                    case Cannon c -> c.getEffectivePower();
                    default -> 0.0;
                })
                .sum();

        // Bonus dell'alieno viola
        if (totalFirePower > 0 && crewCount.getOrDefault(CrewType.PURPLE_ALIEN, 0) > 0) {
            totalFirePower += 2.0;
        }

        return totalFirePower;
    }

    public double calculateFirePowerWithBatteries(boolean usingBattery) {
        double totalFirePower = components.keySet().stream()
                .mapToDouble(component -> switch(component){
                    case SingleCannon e -> e.getEffectivePower();
                    case DoubleCannon e -> e.calculatePowerUsingBattery(usingBattery);
                    default -> 0;
                })
                .sum();

        // Bonus dell'alieno viola
        if (totalFirePower > 0 && crewCount.getOrDefault(CrewType.PURPLE_ALIEN, 0) > 0) {
            totalFirePower += 2.0;
        }

        return totalFirePower;
    }

    public List<Cabin> getAllCabins() {
        return components.keySet().stream()
                .flatMap(component -> switch (component) {
                    case Cabin bc -> Stream.of(bc);
                    default -> Stream.empty();
                })
                .filter(bc -> !bc.isDamaged())
                .collect(Collectors.toList());
    }

    //lista di tutte le batterie cariche e funzionanti sulla nave.
    private List<BatteryCompartment> getChargedBatteries() {
        return components.keySet().stream()
                .flatMap(component -> switch (component) {
                    case BatteryCompartment bc -> Stream.of(bc);
                    default -> Stream.empty();
                })
                .filter(bc -> !bc.isDamaged() && bc.getChargeLevel() > 0)
                .collect(Collectors.toList());
    }

    /**
     * Scarica il numero specificato di unità di energia dalle batterie della nave.
     * Se non c'è abbastanza energia, scarica tutta quella disponibile.
     *
     * @param energyToRemove Il numero di unità di energia da rimuovere
     * @return La quantità di energia effettivamente rimossa
     */
    public int dischargeBatteries(int energyToRemove) {
        if (energyToRemove <= 0) {
            return 0;
        }

        int energyRemoved = 0;
        List<BatteryCompartment> batteries = getAllBatteries();

        // Scorri tutte le batterie e scaricale una per una
        for (BatteryCompartment battery : batteries) {
            if (energyRemoved >= energyToRemove) {
                break;  // Energia rimossa a sufficienza
            }

            int currentCharge = battery.getChargeLevel();
            int chargeToRemove = Math.min(currentCharge, energyToRemove - energyRemoved);
            battery.discharge(chargeToRemove);
            energyRemoved += chargeToRemove;
        }

        return energyRemoved;
    }

    //lista di tutte le batterie funzionanti sulla nave.
    public List<BatteryCompartment> getAllBatteries() {
        return components.keySet().stream()
                .flatMap(component -> switch (component) {
                    case BatteryCompartment bc -> Stream.of(bc);
                    default -> Stream.empty();
                })
                .filter(bc -> !bc.isDamaged())
                .collect(Collectors.toList());
    }

    //Ottiene la lista di cannoni doppi funzionanti.
    public List<DoubleCannon> getOperationalDoubleCannons() {
        return components.keySet().stream()
                .flatMap(component -> switch (component) {
                    case DoubleCannon doubleCannon when !doubleCannon.isDamaged() -> Stream.of(doubleCannon);
                    default -> Stream.empty();
                })
                .collect(Collectors.toList());
    }

    public List<SingleCannon> getOperationalSingleCannons() {
        return components.keySet().stream()
                .flatMap(component -> switch (component) {
                    case SingleCannon singleCannon -> Stream.of(singleCannon);
                    default -> Stream.empty();
                })
                .filter(singleCannon -> !singleCannon.isDamaged())
                .collect(Collectors.toList());
    }

    public List<DoubleEngine> getOperationalDoubleEngines() {
        return components.keySet().stream()
                .flatMap(component -> switch (component) {
                    case DoubleEngine de when !de.isDamaged() -> Stream.of(de);
                    default -> Stream.empty();
                })
                .collect(Collectors.toList());
    }
    public List<SingleEngine> getOperationalSingleEngines() {
        return components.keySet().stream()
                .flatMap(component -> switch (component) {
                    case SingleEngine de when !de.isDamaged() -> Stream.of(de);
                    default -> Stream.empty();
                })
                .collect(Collectors.toList());
    }

    public void rotateComponentOfShipClockwise(Component component) {
        component = getComponentAt(components.get(component));
        if (component != null) {
            Direction newDirection = component.getComponentDirection().getClockwise();
            component.setComponentDirection(newDirection);

            List<Connector> connectors = component.getConnections();
            for (Connector connector : connectors) {
                Direction newConnectorDirection = connector.getDirection().getClockwise();
                connector.setDirection(newConnectorDirection);
            }
        }
    }

    public Component rotateComponentClockwise(Component component) {
        if (component != null) {
            Direction newDirection = component.getComponentDirection().getClockwise();
            component.setComponentDirection(newDirection);

            List<Connector> connectors = component.getConnections();
            for (Connector connector : connectors) {
                Direction newConnectorDirection = connector.getDirection().getClockwise();
                connector.setDirection(newConnectorDirection);
            }
        }
        return component;
    }

    public void rotateComponentOfShipCounterClockwise(Component component) {
        component = getComponentAt(components.get(component));
        if (component != null) {
            Direction newDirection = component.getComponentDirection().getCounterClockwise();
            component.setComponentDirection(newDirection);

            List<Connector> connectors = component.getConnections();
            for (Connector connector : connectors) {
                Direction newConnectorDirection = connector.getDirection().getCounterClockwise();
                connector.setDirection(newConnectorDirection);
            }
        }
    }

    public Component rotateComponentCounterClockwise(Component component) {
        if (component != null) {
            Direction newDirection = component.getComponentDirection().getCounterClockwise();
            component.setComponentDirection(newDirection);

            List<Connector> connectors = component.getConnections();
            for (Connector connector : connectors) {
                Direction newConnectorDirection = connector.getDirection().getCounterClockwise();
                connector.setDirection(newConnectorDirection);
            }
        }
        return component;
    }

    public void rotateComponentOfShipOpposite(Component component) {
        component = getComponentAt(components.get(component));
        if (component != null) {
            Direction newDirection = component.getComponentDirection().getOpposite();
            component.setComponentDirection(newDirection);

            List<Connector> connectors = component.getConnections();
            for (Connector connector : connectors) {
                Direction newConnectorDirection = connector.getDirection().getOpposite();
                connector.setDirection(newConnectorDirection);
            }
        }
    }

    public Component rotateComponentOpposite(Component component) {
        if (component != null) {
            Direction newDirection = component.getComponentDirection().getOpposite();
            component.setComponentDirection(newDirection);

            List<Connector> connectors = component.getConnections();
            for (Connector connector : connectors) {
                Direction newConnectorDirection = connector.getDirection().getOpposite();
                connector.setDirection(newConnectorDirection);
            }
        }
        return component;
    }

    public int calculateEnginePower() {
        int totalPower = components.keySet().stream()
                .mapToInt(component -> switch(component){
                    case SingleEngine e -> e.getEffectivePower();
                    case DoubleEngine e -> e.getEffectivePower();
                    default -> 0;
                })
                .sum();

        // Bonus dell'alieno marrone
        if (totalPower > 0 && crewCount.getOrDefault(CrewType.BROWN_ALIEN, 0) > 0) {
            totalPower += 2;
        }

        return totalPower;
    }
    public int calculateEnginePowerWithBatteries(boolean usingBattery) {
        int totalPower = components.keySet().stream()
                .mapToInt(component -> switch(component){
                    case SingleEngine e -> e.getEffectivePower();
                    case DoubleEngine e -> e.calculatePowerUsingBattery(usingBattery);
                    default -> 0;
                })
                .sum();

        // Bonus dell'alieno marrone
        if (totalPower > 0 && crewCount.getOrDefault(CrewType.BROWN_ALIEN, 0) > 0) {
            totalPower += 2;
        }

        return totalPower;
    }

    public Map<CrewType, Integer> getCrewCounts() {
        return this.crewCount;
    }

    public List<CargoHolds> getCargoHolds() {
        return components.keySet().stream()
                .flatMap(component -> switch (component) {
                    case CargoHolds de when !de.isDamaged() -> Stream.of(de);
                    default -> Stream.empty();
                })
                .collect(Collectors.toList());
    }

    public List<Cargo> getCargoList() {
        return components.keySet().stream()
                .flatMap(component -> switch (component) {
                    case CargoHolds cargoHolds -> cargoHolds.getCargos().stream();
                    default -> Stream.empty();
                })
                .collect(Collectors.toList());
    }

    public List<ShieldGenerator> getShieldsProtecting(Direction direction) {
        return components.keySet().stream()
                .flatMap(component -> switch (component) {
                    case ShieldGenerator de when (!de.isDamaged() && de.getProtectedDirections().contains(direction)) -> Stream.of(de);
                    default -> Stream.empty();
                })
                .collect(Collectors.toList());

    }

    public int chargeEngine() {
        int chargedEngines = 0;
        List<BatteryCompartment> availableBatteries = getChargedBatteries();

        if (availableBatteries.isEmpty()) {
            return 0;
        }

        List<DoubleEngine> doubleEngines = getOperationalDoubleEngines();
        for (DoubleEngine engine : doubleEngines) {
            // Trovo una batteria con carica sufficiente
            for (BatteryCompartment battery : availableBatteries) {
                if (engine.charge(battery)) {
                    chargedEngines++;
                    break;
                }
            }
        }

        return chargedEngines;
    }

    public int removeBrownAlien(int aliensToRemove) {
        return removeCrewOfType(CrewType.BROWN_ALIEN, aliensToRemove);
    }

    public int removePurpleAlien(int aliensToRemove) {
        return removeCrewOfType(CrewType.PURPLE_ALIEN, aliensToRemove);
    }

    public int removeAliens(int aliensToRemove) {
        if (aliensToRemove <= 0) {
            return 0;
        }

        int removedCount = 0;
        List<Cabin> cabinsWithAliens = new ArrayList<>();

        // Trova tutte le cabine con equipaggio del tipo specificato
        List<Cabin> allCabins = getAllCabins();
        for (Cabin cabin : allCabins) {
            if (!cabin.getCrew().isEmpty() &&
                    ( cabin.getCrew().getFirst().getType() == CrewType.BROWN_ALIEN ||
                            cabin.getCrew().getFirst().getType() == CrewType.PURPLE_ALIEN) ) {
                cabinsWithAliens.add(cabin);
            }
        }

        // Rimuovi fino a 'count' membri dell'equipaggio del tipo specificato
        for (Cabin cabin : cabinsWithAliens) {
            if (removedCount >= aliensToRemove) {
                break;
            }

            List<Crew> crewToRemove = cabin.getCrew();  // Ottieni il membro dell'equipaggio nella cabina
            if (crewToRemove != null) {
                for (Crew crew : crewToRemove) {
                    if (removeCrewFromCabin(crew, cabin)) {
                        removedCount++;
                    }
                    if(removedCount == aliensToRemove){
                        break;
                    }
                }
            }
        }

        return removedCount;
    }

    public int removeHumans(int humanToRemove) {
        return removeCrewOfType(CrewType.HUMAN, humanToRemove);
    }

    public void destroyComponentAt(Position targetCoord) {
        Component t = getComponentAt(targetCoord);
        t.setDamaged(true);
        System.out.println("The component" + t.getClass().getSimpleName() + "is Destroyed!");
    }

    public boolean activateShield(ShieldGenerator shield) {
        List<BatteryCompartment> availableBatteries = getChargedBatteries();

        if (availableBatteries.isEmpty()) {
            return false;
        }

        for (BatteryCompartment battery : availableBatteries) {
            return shield.charge(battery);
        }

        return false;
    }

    public void dischargeAllActiveDoubleEngine() {
        for(DoubleEngine de : getOperationalDoubleEngines()){
            if(de.isCharge()){
                de.discharge();
            }
        }
    }

    public void dischargeAllActiveDoubleCannon() {
        for(DoubleCannon de : getOperationalDoubleCannons()){
            if(de.isCharge()){
                de.discharge();
            }
        }
    }

    public List<Component> getNonDamageComponents(){
        return components.keySet().stream()
                .filter(component -> !component.isDamaged())
                .collect(Collectors.toList());
    }
}
