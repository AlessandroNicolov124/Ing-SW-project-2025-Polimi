package it.polimi.ingsw.Components;

public class BlueCargo extends Cargo {

    public BlueCargo(int value) {
        super(value);
    }

    @Override
    public String getType() {
        return "Blue";
    }

}
