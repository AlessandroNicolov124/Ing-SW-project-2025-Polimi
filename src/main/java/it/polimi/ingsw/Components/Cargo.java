package it.polimi.ingsw.Components;

/**
 * cargo is an abstract class representing a cargo component in the game.
 * Cargo can be of different types, such as BlueCargo, RedCargo, GreenCargo, and YellowCargo.
 * Each type of cargo has a specific value associated with it.
 */
public sealed abstract class Cargo implements java.io.Serializable

    permits BlueCargo, RedCargo, GreenCargo, YellowCargo {

    private static final long serialVersionUID = 1L;

    private final int value;

    public Cargo(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    // Pattern matching to check if the cargo is red 

    /**
     * This method uses pattern matching to determine if the cargo is of type RedCargo.
     * It returns true if the cargo is RedCargo, false otherwise.
     **/
    public boolean isRed() {
        return switch (this) {
            case RedCargo r -> true;
            default -> false;
        };
    }
}