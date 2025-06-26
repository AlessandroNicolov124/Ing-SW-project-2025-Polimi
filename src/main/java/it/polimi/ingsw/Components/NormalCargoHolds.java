package it.polimi.ingsw.Components;

/**
 * NormalCargoHolds represents a type of cargo hold that can contain various types of cargo,
 * except for RedCargo. It is initialized with a size that is not SINGLE.
 */
public final class NormalCargoHolds extends CargoHolds {

    public NormalCargoHolds(CargoHoldsSize size) {
        super();
        if(size == CargoHoldsSize.SINGLE) {
            throw new IllegalArgumentException("NormalCargoHolds cannot be of SINGLE size");
        }
        this.setCapacity(size.getCapacity());
    }

    /**
     * checks if the cargo hold can contain a specific type of cargo.
     * @param cargo The cargo to check for compatibility.
     * @return true if the cargo can be contained, false otherwise.
     */
    @Override
    public boolean canContainCargo(Cargo cargo) {
        // Normal CargoHolds can contain all types of cargo except RedCargo
        return switch (cargo) {
            case RedCargo r -> false;
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