package it.polimi.ingsw;

import it.polimi.ingsw.Components.*;
import it.polimi.ingsw.AdventureCards.*;

import java.util.*;

public class ShipBuilder {

    private Position startingPosition;
    private int width, height; //dimensioni plancia
    private Map<Component, Position> components;
    private boolean isAssembled;

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
        component.setPosition(position);
        components.put(component, position);
        return true;
    }

    private boolean wouldDisconnectShip(Component componentToRemove) {
        if (components.size() <= 1) {
            return false; // Only one component, can't disconnect
        }

        Position positionToRemove = components.get(componentToRemove);
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
                    visited.add(adjacentComponent);
                    queue.add(adjacentComponent);
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

        // 2. Check for required components
        boolean hasEngine = components.keySet().stream().anyMatch(c -> c instanceof Engine);
        boolean hasCabin = components.keySet().stream().anyMatch(c -> c instanceof Cabin);

        isAssembled = hasEngine && hasCabin; // La nave è assemblata se ha almeno un motore e una cabina
        return isAssembled;
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
        Position position = lifeSupport.getPosition();
        LifeSupportSystemType type = lifeSupport.getType();

        // Check adjacent positions for cabins
        for (Direction dir : Direction.values()) {
            Position adjacentPos = dir.getAdjacentPosition(position);
            Component adjacentComponent = getComponentAt(adjacentPos);

            if (adjacentComponent instanceof Cabin) {
                Cabin cabin = (Cabin)adjacentComponent;
                cabin.connectLifeSupport(type);
            }
        }
    }

}
