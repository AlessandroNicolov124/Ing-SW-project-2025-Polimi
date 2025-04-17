package it.polimi.ingsw.Components;

public enum CargoHoldsSize {
    DOUBLE(2),
    TRIPLE(3);

    private final int capacity;

    CargoHoldsSize(int capacity) {
        this.capacity = capacity;
    }

    public int getCapacity() {
        return capacity;
    }
}
