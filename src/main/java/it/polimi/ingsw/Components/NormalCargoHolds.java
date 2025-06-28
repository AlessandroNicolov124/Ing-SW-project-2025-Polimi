package it.polimi.ingsw.Components;

public final class NormalCargoHolds extends CargoHolds {

    public NormalCargoHolds(CargoHoldsSize size) {
        super();
        if(size == CargoHoldsSize.SINGLE) {
            throw new IllegalArgumentException("NormalCargoHolds cannot be of SINGLE size");
        }
        this.setCapacity(size.getCapacity());
        this.setSize(size);
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
    protected String calculateComponentString() {
        String sizeStr = switch(getSize()) {
            case SINGLE -> "single";
            case DOUBLE -> "double";
            case TRIPLE -> "triple";
        };
        return "normalcargoholds_" + sizeStr + "_" + getConnectionsString();
    }
}