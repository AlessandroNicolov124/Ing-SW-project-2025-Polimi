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

    public void addComponent(Component c, Position p){

    }

    public Component getCompByPosition(Position pos){
        return null;
    }

    public void removeComponent(Component c){

    }

    public boolean validateShip(){
        return false;
    }

    public void mapPosition(Position pos){

    }


}
