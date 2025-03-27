package it.polimi.ingsw.Components;

public class GreenCargo extends Cargo {

    public GreenCargo(int value) {
        super(value);
    }

    @Override
    public String getType() {
        return "Green";
    }

}
