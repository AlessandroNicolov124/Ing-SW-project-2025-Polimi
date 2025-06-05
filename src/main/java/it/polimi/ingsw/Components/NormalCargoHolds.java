package it.polimi.ingsw.Components;

public final class NormalCargoHolds extends CargoHolds {

    public NormalCargoHolds(CargoHoldsSize size) {
        super(size.getCapacity());
    }

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