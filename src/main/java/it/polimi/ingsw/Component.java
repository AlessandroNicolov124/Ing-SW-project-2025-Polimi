package it.polimi.ingsw;

import java.util.*;

public abstract class Component {
    protected List<Connector> connections; // Lista di connessioni
    protected Direction componentDirection; // Direzione del componente

    public Component() {
        connections = new ArrayList<>();
    }

    public void setComponentDirection(Direction direction) {
        componentDirection = direction;
    }

    public Direction getComponentDirection() {
        return componentDirection;
    }

    public abstract void connectTo(Component c, Direction dir);
    public abstract void disconnect();
}

