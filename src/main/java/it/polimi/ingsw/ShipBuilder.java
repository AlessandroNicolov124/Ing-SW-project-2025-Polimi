package it.polimi.ingsw;

import java.util.Map;

public class ShipBuilder {

    private final Position position;
    private final Map<Component, Position> components;
    private final boolean isAssembled;

    public ShipBuilder(Position position, Map<Component, Position> components) {
        this.position = position;
        this.components = components;
        this.isAssembled = false;
    }

}
