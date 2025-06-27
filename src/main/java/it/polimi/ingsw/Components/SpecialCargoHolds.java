package it.polimi.ingsw.Components;

/**
 * Represents a special cargo hold in the game.
 * it can contain be of all types except for TRIPLE size.
 * It can contain all types of cargo, including special ones.
 */
public final class SpecialCargoHolds extends CargoHolds {

    public SpecialCargoHolds(CargoHoldsSize size) {
        super();
        if(size == CargoHoldsSize.TRIPLE) {
            throw new IllegalArgumentException("SpecialCargoHolds cannot be of TRIPLE size");
        }
        this.setCapacity(size.getCapacity());
    }

    /** special cargo holds can contain all types of cargo */
    @Override
    public boolean canContainCargo(Cargo cargo) {
        // Special CargoHolds can contain all types of cargo
        return switch (cargo) {
            default -> true;
        };
    }

    @Override
    public String getComponentString() {
        String s = this.getClass().getSimpleName().toLowerCase() + "_" + CargoHoldsSize.fromInt(this.getCapacity()).toString().toLowerCase() +"_";
        s += getConnectionsString();
        return s;
    }
}