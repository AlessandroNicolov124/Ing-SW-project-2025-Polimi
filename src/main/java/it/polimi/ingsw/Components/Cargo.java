package it.polimi.ingsw.Components;

public sealed abstract class Cargo
    permits BlueCargo, RedCargo, GreenCargo, YellowCargo {

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