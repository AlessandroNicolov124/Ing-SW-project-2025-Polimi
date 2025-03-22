package it.polimi.ingsw.Components;

import java.util.*;
import it.polimi.ingsw.*;
import it.polimi.ingsw.Visitors.Visitor;

public abstract class Component {
    protected List<Connector> connectors; // Lista di connettori
    protected Direction componentDirection; // Direzione del componente

    public Component() {
        connectors = new ArrayList<>();
    }

    public List<Connector> getConnectors() {
        return connectors;
    }

    public void setConnectors(List<Connector> connectors) {
        this.connectors = connectors;
    }

    public Direction getComponentDirection() {
        return componentDirection;
    }

    public void setComponentDirection(Direction componentDirection) {
        this.componentDirection = componentDirection;
    }

    public abstract void accept(Visitor visitor);


    // Verifica se questo componente può connettersi a un altro componente in una data direzione
    public boolean canConnectTo(Component other, Direction direction) {
        // Trova il connettore in questa direzione
        Optional<Connector> myConnector = connectors.stream()
                .filter(c -> c.getDirection() == direction)
                .findFirst();

        if (!myConnector.isPresent()) {
            return false;
        }

        // Trova il connettore nell'altra direzione
        Direction oppositeDirection = direction.getOpposite();
        Optional<Connector> otherConnector = other.getConnectors().stream()
                .filter(c -> c.getDirection() == oppositeDirection)
                .findFirst();

        if (!otherConnector.isPresent()) {
            return false;
        }

        // Verifica se i connettori sono compatibili
        return areConnectorsCompatible(myConnector.get(), otherConnector.get());
    }

    private boolean areConnectorsCompatible(Connector c1, Connector c2) {
        // Il connettore universale può connettersi a qualsiasi tipo
        if (c1.getConnectorType() == ConnectorType.UNIVERSAL ||
                c2.getConnectorType() == ConnectorType.UNIVERSAL) {
            return true;
        }

        // I connettori devono essere dello stesso tipo
        return c1.getConnectorType() == c2.getConnectorType();
    }

}