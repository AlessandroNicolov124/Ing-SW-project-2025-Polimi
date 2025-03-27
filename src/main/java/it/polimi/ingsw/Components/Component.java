package it.polimi.ingsw.Components;

import it.polimi.ingsw.*;
import it.polimi.ingsw.Visitors.Visitor;

import java.util.*;

public abstract class Component {
    protected List<Connector> connections; // Lista di connessioni
    protected Direction componentDirection; // Direzione del componente
    //protected Position componentPosition;
    protected boolean damaged; // Stato del componente (danneggiato o no)

    public Component() {
        this.connections = new ArrayList<>();
        damaged = false;
        componentDirection = Direction.NORTH;
    }

    public boolean isDamaged() {
        return damaged;
    }

    public void setDamaged(boolean damaged) {
        this.damaged = damaged;
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

    public void setConnections(List<Connector> connections) {
        this.connections = connections;
    }

    // Verifica se questo componente può connettersi a un altro componente in una data direzione
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
        return areConnectorsCompatible(myConnector.get(), otherConnector.get());
    }

    private boolean areConnectorsCompatible(Connector c1, Connector c2) {
        return c1.isCompatibleWith(c2);
    }

    public void initializeConnectors(ConnectorType n, ConnectorType e, ConnectorType s, ConnectorType w) {
        connections.add(new Connector(Direction.NORTH, n));
        connections.add(new Connector(Direction.EAST, e));
        connections.add(new Connector(Direction.SOUTH, s));
        connections.add(new Connector(Direction.WEST, w));
    }

    // Add this method to accept visitors
    public abstract void accept(Visitor visitor);


}











