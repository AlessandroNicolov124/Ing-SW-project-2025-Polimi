package it.polimi.ingsw;

import it.polimi.ingsw.Components.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class is responsible for building and managing the ship in the game.
 * It allows adding, removing, and validating components of the ship,
 * as well as managing crew members and their interactions with the ship's components.
 */
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

    /** check if the position is valid on the ship's grid.
     * A position is valid if it is within the bounds of the ship and
     * corresponds to a valid square according to the ship's layout.
     *
     * @param position The position to check
     * @return true if the position is valid, false otherwise
     */
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

    /**
     * Check if the position is occupied by any component.
     *
     * @param position The position to check
     * @return true if the position is occupied, false otherwise
     */
    public boolean isPositionOccupied(Position position) {
        return components.containsValue(position);
    }

    /**
     * Place a component at a specific position on the ship.
     * The component must not be already placed and the position must not be occupied.
     * If the component is the first one, it must be placed at the starting position.
     *
     * @param component The component to place
     * @param position  The position where to place the component
     * @return true if the component was successfully placed, false otherwise
     */
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

    /**
     * check if removing a component would disconnect the ship.
     * This method uses a breadth-first search (BFS) algorithm
     * to check if all components are still connected
     * after removing the specified component.
     *
     * @param componentToRemove The component to check for removal
     * @return true if removing the component would disconnect the ship, false otherwise
     */
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

    /**
     * Remove a component from the ship.
     * If the component is a Cabin and it is occupied, it will remove the crew from that cabin.
     * If removing the component would disconnect the ship, it will not be removed.
     *
     * @param component The component to remove
     * @return true if the component was successfully removed, false otherwise
     */
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

    /**
     * Check if all components of the ship are connected.
     * This method uses a breadth-first search (BFS) algorithm
     * to check if all components are reachable from the first component.
     *
     * @return true if all components are connected, false otherwise
     */
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

    /**
     * Validate the ship by checking if all components are connected,
     * if there is at least one Cannon and one Engine, and if the ship is assembled.
     *
     * @return true if the ship is valid, false otherwise
     */
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

    /** Check if the cannon has a clear firing path.
     * This method checks if there are any components in the firing direction of each cannon.
     * @return true if all cannons have a clear firing path, false otherwise
     */
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

    /** Check if the engine has a clear firing path.
     * This method checks if there are any components in the opposite direction of each engine.
     * @return true if all engines have a clear firing path, false otherwise
     */
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

    /** update the life support connections for the given life support system.
     * This method checks adjacent positions for cabins and updates their life support connections
     * based on the type of life support system.
     *
     * @param lifeSupport The life support system to update connections for
     */
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

    /** disconnect the life support connections for the given life support system.
     * This method checks adjacent positions for cabins and disconnects their life support connections
     * based on the type of life support system.
     *
     * @param lifeSupport The life support system to disconnect connections for
     */
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

    /** add a crew member to a cabin.
     * This method checks if the crew member can be added based on the ship's rules
     * and updates the crew count accordingly.
     *
     * @param crew  The crew member to add
     * @param cabin The cabin where the crew member will be added
     * @return true if the crew member was successfully added, false otherwise
     */
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

    /** remove a crew member from a cabin.
     * This method checks if the crew member can be removed based on the ship's rules
     * and updates the crew count accordingly.
     *
     * @param crew  The crew member to remove
     * @param cabin The cabin from which the crew member will be removed
     * @return true if the crew member was successfully removed, false otherwise
     */
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

    /** remove a specified number of crew members from the ship.
     * This method prioritizes removing aliens first, then humans,
     * and returns the total number of crew members removed.
     *
     * @param count The number of crew members to remove
     * @return The total number of crew members removed
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
     * return a list of cabins that have crew members.
     * This method iterates through all cabins and checks if they contain crew members.
     * It also sorts the cabins based on the priority of crew removal,
     * with aliens being removed first.
     * @return A list of cabins that have crew members, sorted by removal priority
     *
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

    /** remove a specified number of aliens from the ship.
     * This method iterates through all cabins and removes the specified number of aliens.
     *
     * @param count The number of aliens to remove
     * @return The total number of aliens removed
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


    /** add a specified number of crew members of a specific type to the ship.
     * This method handles both human and alien crew members, ensuring that the ship's rules are followed.
     * For aliens, it ensures only one of each type can be added per ship.
     *
     * @param crewType The type of crew to add (HUMAN, BROWN_ALIEN, PURPLE_ALIEN)
     * @param count    The number of crew members to add
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
    /** Distribute crew members automatically based on the ship's rules.
     * This method places 2 humans in the starting cabin and then distributes
     * crew members in other cabins according to the specified rules.
     */
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

    /** Charge all operational double cannons using available batteries.
     * This method iterates through all double cannons and charges them
     * using the available batteries. It returns the number of cannons charged.
     *
     * @return The number of double cannons successfully charged
     */
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

    /** Charge a specific double cannon using available batteries.
     * This method attempts to charge the specified double cannon using
     * the available batteries. It returns true if the cannon was charged,
     * false otherwise.
     *
     * @param cannon The double cannon to charge
     * @return true if the cannon was successfully charged, false otherwise
     */
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

    /** Calculate the total fire power of the ship.
     * This method sums up the effective power of all cannons and applies
     * a bonus if there is at least one purple alien crew member.
     *
     * @return The total fire power of the ship
     */
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

    /** Calculate the fire power of the ship considering battery usage.
     * This method sums up the effective power of all cannons and applies
     * a bonus if there is at least one purple alien crew member.
     * It also considers whether batteries are being used for double cannons.
     *
     * @param usingBattery true if batteries are being used, false otherwise
     * @return The total fire power of the ship considering battery usage
     */
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

    /** Discharge batteries to remove energy from the ship.
     * This method iterates through all batteries and discharges them
     * until the specified amount of energy is removed or all batteries are discharged.
     *
     * @param energyToRemove The amount of energy to remove from the batteries
     * @return The total amount of energy removed from the batteries
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

    /** Calculate the total engine power of the ship.
     * This method sums up the effective power of all engines and applies
     * a bonus if there is at least one brown alien crew member.
     *
     * @return The total engine power of the ship
     */
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
    /** Calculate the total engine power of the ship considering battery usage.
     * This method sums up the effective power of all engines and applies
     * a bonus if there is at least one brown alien crew member.
     * It also considers whether batteries are being used for double engines.
     *
     * @param usingBattery true if batteries are being used, false otherwise
     * @return The total engine power of the ship considering battery usage
     */
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

    /** Charge all operational engines using available batteries.
     * This method iterates through all double engines and charges them
     * using the available batteries. It returns the number of engines charged.
     *
     * @return The number of double engines successfully charged
     */
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

    /** Remove a specified number of aliens from the ship.
     * This method iterates through all cabins and removes the specified number of aliens.
     *
     * @param aliensToRemove The number of aliens to remove
     * @return The total number of aliens removed
     */
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
