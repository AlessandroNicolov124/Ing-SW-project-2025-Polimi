package it.polimi.ingsw.Components;

import it.polimi.ingsw.Direction;
import it.polimi.ingsw.network.messages.RotationType;

import java.util.ArrayList;
import java.util.List;
/** this class represents a shield generator component in the game.
 * It can protect max 2 (near) directions from damage when charged.
 * The shield generator consumes energy from a battery compartment to charge.
 */
public final class ShieldGenerator extends Component {

    private boolean isCharged; // Stato di carica
    private int energyConsumption;
    private List<Direction> protectedDirections;

    public ShieldGenerator() {
        super();
        this.isCharged = false;
        this.energyConsumption = 1;
        protectedDirections = new ArrayList<Direction>();
        protectedDirections.add(this.componentDirection); // Default protected direction
        protectedDirections.add(this.componentDirection.getClockwise()); // Default protected direction
    }

    @Override
    public String getComponentString() {
        String s = this.getClass().getSimpleName().toLowerCase() + "_";
        s += getConnectionsString();
        return s;
    }

    /**
     * Add a direction to the list of protected directions.
     * If the direction is already protected, it will not be added again.
     * if the direction is not adjacent to the already protected directions, returns an error message.
     * @param direction the direction to be added
     */
    public void add(Direction direction) {
        if( !protectedDirections.contains(direction)){
            if (protectedDirections.size() >= 2) {
                System.out.println("Cannot protect more than 2 directions");
                return;
            }
            if (direction == null) {
                System.out.println("Direction cannot be null");
                return;
            }
            // Consente di aggiungere la direzione solo se è adiacente a quelle già presenti (se ce ne sono)
            if (!protectedDirections.isEmpty()) {
                boolean isAdjacent = direction.isAdjacentTo(protectedDirections.getFirst());
                if (!isAdjacent) {
                    System.out.println("Direction " + direction + " is not adjacent to the already protected directions: " + protectedDirections);
                    return;
                }
            }
            protectedDirections.add(direction);
        }
    }

    /**
     * Rotates the protected directions based on the specified rotation type.
     * This method updates the protected directions according to the rotation type.
     *
     * @param rotationType the type of rotation to apply (CLOCKWISE, ANTI_CLOCKWISE, OPPOSITE)
     */
    public void rotateProtectedDirections(RotationType rotationType) {
        // If the component is a ShieldGenerator, we need to update the protected directions
        for (int i = 0; i < protectedDirections.size(); i++) {
            Direction dir = protectedDirections.get(i);
            switch (rotationType) {
                case CLOCKWISE -> protectedDirections.set(i, dir.getClockwise());
                case ANTI_CLOCKWISE -> protectedDirections.set(i, dir.getCounterClockwise());
                case OPPOSITE -> protectedDirections.set(i, dir.getOpposite());
            }
        }
    }

    public void remove(Direction direction) {
        protectedDirections.remove(direction);
    }

    public List<Direction> getProtectedDirections(){
        return protectedDirections;
    }

    private boolean isActive(){
        if (damaged || !isCharged) {
            return false;
        }
        return true;
    }

    public int getEnergyConsumption() {
        return this.energyConsumption;
    }

    public boolean isCharged() {
        return this.isCharged;
    }


    /**
     * Charges the shield generator using the provided battery compartment.
     * The shield generator can only be charged if it is not damaged, is not already charged,
     * and the battery compartment has enough charge to cover the energy consumption.
     *
     * @param battery the battery compartment to charge from
     * @return true if the shield generator was successfully charged, false otherwise
     */
    public boolean charge(BatteryCompartment battery) {
        if (battery == null || damaged || isCharged || battery.getChargeLevel() < energyConsumption) {
            return false;
        }

        int success = battery.discharge(energyConsumption);
        if (success>0) {
            isCharged = true;
            return true;
        }
        return false;
    }

    public void discharge() {
        this.isCharged = false;
    }
}

