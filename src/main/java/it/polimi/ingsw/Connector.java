package it.polimi.ingsw;

import it.polimi.ingsw.Components.Component;

// Classe Connector
public class Connector {
    private Direction direction;
    private Component component;

    public Connector(Direction direction, Component component) {
        this.direction = direction;
        this.component = component;
    }

    // Getters
    public Direction getDirection() {
        return direction;
    }

    public Component getComponent() {
        return component;
    }
}
