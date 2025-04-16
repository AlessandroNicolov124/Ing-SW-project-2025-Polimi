package it.polimi.ingsw;

import it.polimi.ingsw.Components.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ShipBuilder {

    private Position startingPosition;
    private int width, height; //dimensioni plancia
    private Map<Component, Position> components;
    private boolean isAssembled;
    private Map<CrewType, Integer> crewCount = new HashMap<>(); // tiene conto del numero di umani e alieni sulla navicella

    public ShipBuilder(int width, int height) {
        this.startingPosition = new Position(width/2, height/2); // Posizione iniziale
        this.components = new HashMap<>();
        this.isAssembled = false;
        this.width = width;
        this.height = height;
    }

    //posizione allinterno della plancia
    public boolean isPositionValid(Position position) {
        return position.getX() >= 0 && position.getX() < width &&
                position.getY() >= 0 && position.getY() < height;
    }

    public boolean isPositionOccupied(Position position) {
        return components.containsValue(position);
    }

    public boolean placeComponent(Component component, Position position) {
        // Verifica se la posizione è già occupata
        if (isPositionOccupied(position)) {
            return false;
        }

        // Se è il primo componente, deve essere posizionato nella posizione iniziale
        if (components.isEmpty() && !position.equals(startingPosition)) {
            return false;
        }

        // Se non è il primo componente, deve essere collegato a un componente esistente
        if (!components.isEmpty()) {
            boolean isConnected = false;

            for (Direction direction : Direction.values()) {
                Position adjacentPosition = direction.getAdjacentPosition(position);
                Component adjacentComponent = getComponentAt(adjacentPosition);

                if (adjacentComponent != null && component.canConnectTo(adjacentComponent, direction)) {
                    isConnected = true;
                    break;
                }
            }

            if (!isConnected) {
                return false;
            }
        }

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

        // Check if removing this component would disconnect the ship
        if (components.size() > 1 && wouldDisconnectShip(component)) {
            return false;
        }

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
                    if (current.canConnectTo(adjacentComponent, direction)){
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
            return false;
        }

        // 1. Check if all components are connected
        if (!areAllComponentsConnected()) {
            return false;
        }

        // 2. Check for Cannon
        boolean validationCannonFire = checkCannonFire();

        // 3. Check for required components
        //boolean hasEngine = components.keySet().stream().anyMatch(c -> c instanceof Engine);
        //boolean hasCabin = components.keySet().stream().anyMatch(c -> c instanceof Cabin);
        //isAssembled = hasEngine && hasCabin && validationCannonFire; // La nave è assemblata se ha almeno un motore e una cabina


        long hasEngine = components.keySet().stream()
                .flatMap(component -> switch(component){
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

        isAssembled = hasEngine>0 && hasCabin>0 && validationCannonFire; // La nave è assemblata se ha almeno un motore e una cabina

        return isAssembled;
    }

    public boolean checkCannonFire() {
        List<SingleCannon> singleCannons = getOperationalSingleCannons();
        List<DoubleCannon> doubleCannons = getOperationalDoubleCannons();
        for (SingleCannon singleCannon : singleCannons) {
            Position inFiringDirection = getPositionOf(singleCannon).getAdjacentPosition(singleCannon.getFiringDirection());
            if(getAllPositions().contains(inFiringDirection)) {
                return false;
            }
        }
        for (DoubleCannon dc : doubleCannons) {
            Position inFiringDirection = getPositionOf(dc).getAdjacentPosition(dc.getFiringDirection());
            if(getAllPositions().contains(inFiringDirection)) {
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
    private void updateLifeSupportConnections(LifeSupportSystem lifeSupport) {
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
                default -> {}
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
            if (crew.getType() != CrewType.HUMAN) {
                crewCount.put(crew.getType(), crewCount.getOrDefault(crew.getType(), 0) + 1);
            }
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

//        List<SingleCannon> singleCannons = getOperationalSingleCannons();
//        for (SingleCannon cannon : singleCannons) {
//            for (BatteryCompartment battery : availableBatteries) {
//                if (cannon.charge(battery)) {
//                    chargedCannons++;
//                    break;
//                }
//            }
//        }

        return chargedCannons;
    }

    public double calculateFirePower() {
        double totalFirePower = 0.0;

//        for (Component component : components.keySet()) {
//            if (component instanceof Cannon cannon) {
//                totalFirePower += cannon.getEffectivePower();
//            }
//        }

        totalFirePower = components.keySet().stream()
                .mapToDouble(component -> switch(component){
                    case Cannon c -> c.getEffectivePower();
                    default -> 0.0;
                })
                .sum();

        // Bonus dell'alieno viola
//        if (totalFirePower > 0 && crewCount.getOrDefault(CrewType.PURPLE_ALIEN, 0) > 0) {
//            totalFirePower += 2.0;
//        }

        return totalFirePower;
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

    //Ottiene la lista di cannoni doppi funzionanti.
    private List<DoubleCannon> getOperationalDoubleCannons() {
        return components.keySet().stream()
                .flatMap(component -> switch (component) {
                    case DoubleCannon doubleCannon when !doubleCannon.isDamaged() -> Stream.of(doubleCannon);
                    default -> Stream.empty();
                })
                .collect(Collectors.toList());
    }

    private List<SingleCannon> getOperationalSingleCannons() {
        return components.keySet().stream()
                .flatMap(component -> switch (component) {
                    case SingleCannon singleCannon -> Stream.of(singleCannon);
                    default -> Stream.empty();
                })
                .filter(singleCannon -> !singleCannon.isDamaged())
                .collect(Collectors.toList());
    }

    public void rotateComponentClockwise(Component component) {
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

    public void rotateComponentCounterClockwise(Component component) {
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

    public void rotateComponentOpposite(Component component) {
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

    public int calculateEnginePower() {
        return components.keySet().stream()
                .mapToInt(component -> switch(component){
                    case Engine e -> e.getPower();
                    default -> 0;
                })
                .sum();
    }
}
