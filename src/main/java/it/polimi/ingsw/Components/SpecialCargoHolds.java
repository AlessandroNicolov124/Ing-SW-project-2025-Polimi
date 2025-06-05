package it.polimi.ingsw.Components;

public final class SpecialCargoHolds extends CargoHolds {

    public SpecialCargoHolds(CargoHoldsSize size) {
        super(size.getCapacity());
    }

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