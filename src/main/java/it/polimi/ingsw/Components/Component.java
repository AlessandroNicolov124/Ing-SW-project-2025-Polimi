package it.polimi.ingsw.Components;

import it.polimi.ingsw.*;
import it.polimi.ingsw.network.messages.RotationType;

import java.io.Serializable;
import java.util.*;

/**
 * This class represents a generic component of a spaceship.
 * It contains methods to manage connections, check compatibility with other components,
 * and handle the component's state (damaged or not).
 * It is an abstract class that can be extended by specific component types.
 */
public sealed abstract class Component implements Serializable permits BatteryCompartment, Cabin, Cannon, CargoHolds, Engine, LifeSupportSystem, ShieldGenerator, StructuralModule {
    protected List<Connector> connections; // Lista di connessioni
    protected Direction componentDirection; // Direzione del componente
    protected boolean damaged; // Stato del componente (danneggiato o no)

    public Component() {
        this.connections = new ArrayList<>();
        damaged = false;
        componentDirection = Direction.NORTH;
    }

    public void deleteConnection(){
        connections.clear();
    }

    /**
     * Remove a connection based on its direction.
     * @param direction The direction of the connection to remove
     * @return true if the connection was successfully removed, false otherwise
     */
    public boolean removeConnectionByDirection(Direction direction){
        Connector c = connections.stream()
                .filter(cc -> cc.getDirection() == direction)
                .findFirst().orElse(null);

        if (c == null) {
            return false;
        }

        return connections.remove(c);
    }

    /**
     * Add a connection in the specified direction with the given connector type.
     * If a connection already exists in that direction, it will be replaced.
     * @param direction The direction in which to add the connection
     * @param connectorType The type of connector to add
     * @return true if the operation was successful, false otherwise
     */
    public boolean addConnectionInDirection(Direction direction, ConnectorType connectorType) {
        // Prima rimuoviamo eventuali connettori esistenti in quella direzione
        removeConnectionByDirection(direction);

        // Creiamo il nuovo connettore
        Connector newConnector = new Connector(direction, connectorType);

        // Aggiungiamo il nuovo connettore alla lista delle connessioni
        return connections.add(newConnector);
    }

    /**
    * Adds a specific connector in the indicated direction.
    * If a connector already exists in that direction, it will be replaced.
    *
    * @param connector The connector to add
    * @return true if the operation was successful, false otherwise
    */
    public boolean addConnection(Connector connector) {
        if (connector == null) {
            return false;
        }

        // Rimuoviamo eventuali connettori esistenti nella stessa direzione
        removeConnectionByDirection(connector.getDirection());

        // Aggiungiamo il nuovo connettore
        return connections.add(connector);
    }

    /**
     * Changes the type of an existing connector in a given direction.
     * If there is no connector in that direction, a new one is created.
     *
     * @param direction The direction of the connector to modify
     * @param newType The new type of connector
     * @return true if the operation was successful, false otherwise
     */
    public boolean changeConnectorType(Direction direction, ConnectorType newType) {
        // Cerchiamo il connettore nella direzione specificata
        Connector existingConnector = connections.stream()
                .filter(conn -> conn.getDirection() == direction)
                .findFirst().orElse(null);

        if (existingConnector != null) {
            // Se esiste, rimuoviamo il connettore esistente
            connections.remove(existingConnector);

            // Creiamo un nuovo connettore con lo stesso indirizzo ma tipo diverso
            Connector newConnector = new Connector(direction, newType);

            // Aggiungiamo il nuovo connettore
            return connections.add(newConnector);
        } else {
            // Se non esiste, creiamo un nuovo connettore
            return addConnectionInDirection(direction, newType);
        }
    }

    /**
    * Ensures that there is a connector of the specified type in a given direction.
    * If a connector of the same type already exists, does nothing.
    * If a connector of a different type exists, it is replaced.
    * If no connector exists, a new one is created.
    *
    * @param direction The direction in which to ensure the connector
    * @param requiredType The required type of connector
    * @return true if a modification was made, false if the required connector was already present
    */
    public boolean ensureConnector(Direction direction, ConnectorType requiredType) {
        // Cerchiamo il connettore esistente
        Connector existingConnector = getConnectorByDirection(direction);

        // Se non esiste un connettore o è di tipo diverso, facciamo la modifica
        if (existingConnector == null || !existingConnector.getConnectorType().getClass().equals(requiredType.getClass())) {
            // Rimuovi eventuale connettore esistente
            if (existingConnector != null) {
                connections.remove(existingConnector);
            }

            // Aggiungi il nuovo connettore
            Connector newConnector = new Connector(direction, requiredType);
            connections.add(newConnector);
            return true;
        }

        // Nessuna modifica necessaria, il connettore richiesto è già presente
        return false;
    }

    public boolean isDamaged() {
        return damaged;
    }

    public void setDamaged(boolean damaged) {
        this.damaged = damaged;
    }

    /**
     * Determines if this component is a starting cabin that cannot be removed.
     * Default implementation returns false. Cabin class overrides this.
     * 
     * @return true if this is a starting cabin, false otherwise
     */
    public boolean isStartingCabin() {
        return false;
    }

    public void setComponentDirection(Direction direction) {
        componentDirection = direction;
    }

    public Direction getComponentDirection() {
        return componentDirection;
    }

    // Get connections for visitor operations
    public List<Connector> getConnections() {
        return new ArrayList<>(connections);
    }

    /** Get a connector by its direction.
     * Returns null if no connector exists in that direction.
     *
     * @param direction The direction of the connector to retrieve
     * @return The connector in the specified direction, or null if not found
     */
    public Connector getConnectorByDirection(Direction direction) {
        return connections.stream().filter(c -> c.getDirection() == direction).findFirst().orElse(null);
    }

    /**
     * Checks if this component can connect to another component in a specified direction.
     * It verifies that both components have compatible connectors in the specified direction.
     *
     * @param other The other component to check connection compatibility with
     * @param direction The direction in which to check the connection
     * @return true if the components can connect, false otherwise
     */
    public boolean canConnectTo(Component other, Direction direction) {
        // Trova il connettore in questa direzione
        Optional<Connector> myConnector = connections.stream()
                .filter(c -> c.getDirection() == direction)
                .findFirst();

        if (!myConnector.isPresent()) {
            return false;
        }

        // Trova il connettore nell'altra direzione
        Direction oppositeDirection = direction.getOpposite();
        Optional<Connector> otherConnector = other.getConnections().stream()
                .filter(c -> c.getDirection() == oppositeDirection)
                .findFirst();

        if (!otherConnector.isPresent()) {
            return false;
        }

        // Verifica se i connettori sono compatibili
        boolean comp = myConnector.get().isCompatibleWith(otherConnector.get());
        System.out.println(" Connector are compatible: " + comp);
        return comp;
    }

    public void initializeConnectors(ConnectorType n, ConnectorType e, ConnectorType s, ConnectorType w) {
        connections.add(new Connector(Direction.NORTH, n));
        connections.add(new Connector(Direction.EAST, e));
        connections.add(new Connector(Direction.SOUTH, s));
        connections.add(new Connector(Direction.WEST, w));
    }

    public abstract String getComponentString();
    /**
     * Returns a string representation of the connections in the order: North, East, South, West.
     * Each character represents the type of connector in that direction:
     * 'N' for NullConnectorType, 'S' for SingleConnectorType,
     * 'D' for DoubleConnectorType, 'U' for UniversalConnectorType.
     *
     * @return A string representing the connections of the component.
     */
    public String getConnectionsString(){
        StringBuilder sb = new StringBuilder();
        Direction dir = componentDirection;
        for (int i = 0; i < 4; i++) {
            Connector c = getConnectorByDirection(dir);
            if (c == null) {
                sb.append("N");
            } else {
                switch(c.getConnectorType()){
                    case NullConnectorType n -> sb.append("N");
                    case SingleConnectorType n -> sb.append("S");
                    case DoubleConnectorType n -> sb.append("D");
                    case UniversalConnectorType n -> sb.append("U");
                }
            }
            dir = dir.getClockwise();
        }
        return sb.toString();
    }

    /**
     * Rotates the component and its connections based on the specified rotation type.
     * The component's direction and each connection's direction are updated accordingly.
     *
     * @param rotationType The type of rotation to apply (CLOCKWISE, ANTI_CLOCKWISE, OPPOSITE)
     */
    public void rotate(RotationType rotationType) {
        switch (rotationType) {
            case CLOCKWISE:
                componentDirection = componentDirection.getClockwise();
                connections.forEach(conn -> conn.setDirection(conn.getDirection().getClockwise()));
                break;
            case ANTI_CLOCKWISE:
                componentDirection = componentDirection.getCounterClockwise();
                connections.forEach(conn -> conn.setDirection(conn.getDirection().getCounterClockwise()));
                break;
            case OPPOSITE:
                componentDirection = componentDirection.getOpposite();
                connections.forEach(conn -> conn.setDirection(conn.getDirection().getOpposite()));
                break;
            default:
                throw new IllegalArgumentException("Invalid rotation type: " + rotationType);
        }
        switch(this){
            case ShieldGenerator shieldGenerator -> {
                shieldGenerator.rotateProtectedDirections(rotationType);
            }
            default -> {
                // No additional actions needed for other component types
            }
        }
    }
}
