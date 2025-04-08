package it.polimi.ingsw.Components;

import java.util.*;

public final class CargoHolds extends Component {

    private CargoHoldsSize cargoHoldsSize;
    private CargoHoldsType cargoHoldsType;
    private List<Cargo> cargos;

    public CargoHolds() {
        super();
        this.cargos = new ArrayList<>();
    }

    public CargoHolds(CargoHoldsSize cargoHoldsSize, CargoHoldsType cargoHoldsType) {
        super();
        this.cargoHoldsSize = cargoHoldsSize;
        this.cargoHoldsType = cargoHoldsType;
        this.cargos = new ArrayList<>();
    }

    public CargoHoldsSize getSize() {
        return cargoHoldsSize;
    }

    public CargoHoldsType getType() {
        return cargoHoldsType;
    }

    public int getCapacity() {
        return cargoHoldsSize.getCapacity();
    }

    public List<Cargo> getCargos() {
        return new ArrayList<>(cargos.stream().toList());
    }

    public boolean canContainCargo(Cargo cargo) {
        if (cargoHoldsType == CargoHoldsType.NORMAL) {
            return !(cargo instanceof RedCargo);
        } else if (cargoHoldsType == CargoHoldsType.SPECIAL) {
            return cargo instanceof RedCargo;
        }
        return false;
    }

    public void loadCargo(Cargo cargo) {
        if (cargoHoldsSize == null) {
            throw new IllegalStateException("Cargo holds size is not set");
        }

        if (cargos.size() >= getCapacity()) {
            throw new IllegalStateException("Cargo holds is at full capacity");
        }

        if (!canContainCargo(cargo)) {
            throw new IllegalArgumentException("This cargo type is not compatible with this cargo hold category");
        }

        cargos.add(cargo);
    }

    public boolean isFull() {
        return cargoHoldsSize != null && cargos.size() >= getCapacity();
    }

    public boolean isEmpty() {
        return cargos.isEmpty();
    }

    public int getAvailableSpace() {
        return getCapacity() - cargos.size();
    }

    public void unloadCargo(Cargo cargo) {
        cargos.remove(cargo);
    }

    public int sumCargoValue() {
        int totalValue = 0;
        for (Cargo cargo : cargos) {
            totalValue += cargo.getValue();
        }
        return totalValue;
    }

}
