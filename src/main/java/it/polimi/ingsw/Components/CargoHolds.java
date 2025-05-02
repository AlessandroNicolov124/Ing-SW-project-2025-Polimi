package it.polimi.ingsw.Components;

import java.util.*;

public sealed abstract class CargoHolds extends Component permits NormalCargoHolds, SpecialCargoHolds {

    private int capacity;
    private List<Cargo> cargos = new ArrayList<>();

    public CargoHolds(int capacity) {
        super();
        this.capacity = capacity;
    }

    public int getCapacity() {
        return capacity;
    }

    public List<Cargo> getCargos() {
        return new ArrayList<>(cargos.stream().toList());
    }

    public abstract boolean canContainCargo(Cargo cargo);

    public void loadCargo(Cargo cargo) {
        if (cargos.size() >= capacity) {
            throw new IllegalStateException("Cargo holds is at full capacity");
        }
        if (!canContainCargo(cargo)) {
            throw new IllegalArgumentException("This cargo type is not compatible with this cargo hold");
        }
        cargos.add(cargo);
    }

    public boolean isFull() {
        return cargos.size() >= capacity;
    }

    public boolean isEmpty() {
        return cargos.isEmpty();
    }

    public int getAvailableSpace() {
        return capacity - cargos.size();
    }

    public void unloadCargo(Cargo cargo) {
        cargos.remove(cargo);
    }

    public int sumCargoValue() {
        return cargos.stream().mapToInt(Cargo::getValue).sum();
    }

}