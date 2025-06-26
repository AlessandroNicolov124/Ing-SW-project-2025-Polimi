package it.polimi.ingsw.Components;

import java.util.*;

/**
 * This class represents a cargo hold that can contain various types of cargo.
 * It is an abstract class that defines the basic structure and functionality for cargo holds.
 * Subclasses must implement the method to check if a specific type of cargo can be contained.
 */
public sealed abstract class CargoHolds extends Component permits NormalCargoHolds, SpecialCargoHolds {

    private int capacity;
    private List<Cargo> cargos = new ArrayList<>();

    public CargoHolds() {
        super();
    }

    public void setCapacity(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than zero");
        }
        this.capacity = capacity;
    }

    public int getCapacity() {
        return capacity;
    }

    public List<Cargo> getCargos() {
        return new ArrayList<>(cargos.stream().toList());
    }

    /**
     * This method checks if the cargo hold can contain a specific type of cargo.
     * It is abstract and must be implemented by subclasses to define their own rules for cargo compatibility.
     *
     * @param cargo The cargo to check for compatibility.
     * @return true if the cargo can be contained, false otherwise.
     */
    public abstract boolean canContainCargo(Cargo cargo);

    /**
     * This method checks if the cargo hold can contain a specific type of cargo.
     * It is abstract and must be implemented by subclasses to define their own rules for cargo compatibility.
     *
     * @param cargo The cargo to check for compatibility.
     */
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