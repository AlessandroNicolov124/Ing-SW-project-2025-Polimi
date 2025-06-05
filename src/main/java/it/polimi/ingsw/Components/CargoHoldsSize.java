package it.polimi.ingsw.Components;

public enum CargoHoldsSize {
    SINGLE(1),
    DOUBLE(2),
    TRIPLE(3);

    private final int capacity;

    CargoHoldsSize(int capacity) {
        this.capacity = capacity;
    }

    public int getCapacity() {
        return capacity;
    }

    public static CargoHoldsSize fromInt(int i) {
        switch (i) {
            case 1: return SINGLE;
            case 2: return DOUBLE;
            case 3: return TRIPLE;
            default: throw new IllegalArgumentException("Invalid CargoHoldsSize value: " + i);
        }
    }
}

