package it.polimi.ingsw.Components;

public class YellowCargo extends Cargo {

    public YellowCargo(int value) {
        super(value);
    }

    @Override
    public String getType() {
        return "Yellow";
    }

}
