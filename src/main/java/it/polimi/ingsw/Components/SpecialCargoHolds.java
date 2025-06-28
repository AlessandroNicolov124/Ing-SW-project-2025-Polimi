package it.polimi.ingsw.Components;

public final class SpecialCargoHolds extends CargoHolds {

    public SpecialCargoHolds(CargoHoldsSize size) {
        super();
        if(size == CargoHoldsSize.TRIPLE) {
            throw new IllegalArgumentException("SpecialCargoHolds cannot be of TRIPLE size");
        }
        this.setCapacity(size.getCapacity());
        this.setSize(size);
    }

    @Override
    public boolean canContainCargo(Cargo cargo) {
        // Special CargoHolds can contain all types of cargo
        return switch (cargo) {
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
        return "specialcargoholds_" + sizeStr + "_" + getConnectionsString();
    }
            
}