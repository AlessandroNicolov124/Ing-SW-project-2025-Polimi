package it.polimi.ingsw.Components;

import it.polimi.ingsw.*;
import it.polimi.ingsw.Visitors.Visitor;

import java.util.*;

public abstract class Component {
    protected List<Connector> connections; // Lista di connessioni
    protected Direction componentDirection; // Direzione del componente

    public Component() {
        this.connections = new ArrayList<>();
        this.componentDirection = null;
    }

    public Component(int numCon, Direction componentDirection) {
        this.connections = new ArrayList<>(numCon - 1);
        this.componentDirection = componentDirection;
    }
    public void setComponentDirection(Direction direction) {
        componentDirection = direction;
    }

    public Direction getComponentDirection() {
        return componentDirection;
    }

    // Add this method to accept visitors
    public abstract void accept(Visitor visitor);

    // Get connections for visitor operations
    public List<Connector> getConnections() {
        return new ArrayList<>(connections);
    }
}











