package it.polimi.ingsw.Components;

import java.io.Serializable;

public sealed abstract class Cargo implements Serializable
    permits BlueCargo, RedCargo, GreenCargo, YellowCargo  {

    private static final long serialVersionUID = 1L;
    private final int value;

    public Cargo(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    // Pattern matching to check if the cargo is red 

    public boolean isRed() {
        return switch (this) {
            case RedCargo r -> true;
            default -> false;
        };
    }
}
