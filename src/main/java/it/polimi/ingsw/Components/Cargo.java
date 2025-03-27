package it.polimi.ingsw.Components;

public abstract class Cargo {

    private final int value;

    public Cargo(int value) {
        this.value = value;
    }

    public Cargo(int value, int amount) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public abstract String getType();

}