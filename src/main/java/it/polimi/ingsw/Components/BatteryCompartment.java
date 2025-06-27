package it.polimi.ingsw.Components;

import it.polimi.ingsw.*;

/**
 * Represents a battery compartment that can hold batteries of a specific type and charge level.
 * It allows for recharging and discharging of the batteries.
 */
public final class BatteryCompartment extends Component {
    private int maxCapacity; // numero di batterie
    private BatteryType type;
    private int chargeLevel; // carica

    public BatteryCompartment() {
        super();
    }

    @Override
    public String getComponentString() {
        String s = this.getClass().getSimpleName().toLowerCase() + "_";
        s += type.toString().toLowerCase() + "_" + getConnectionsString();
        return s;
    }

    public BatteryCompartment(int maxCapacity, BatteryType type, int chargeLevel) {
        super();
        this.maxCapacity = maxCapacity;
        this.type = type;
        this.chargeLevel = chargeLevel;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public BatteryType getType() {
        return type;
    }

    public int getChargeLevel() {
        return chargeLevel;
    }

    /**
     * Recharge the battery to its maximum capacity.
     */
    public void recharge() {
        chargeLevel = maxCapacity;
    }

    /**
     * Recharge the battery by a specified amount.
     *
     * @param amount The amount of energy to recharge
     */
    public int recharge(int amount) {
        if (amount <= 0) {
            return 0;
        }

        int spaceLeft = maxCapacity - chargeLevel;
        int actualRecharge = Math.min(spaceLeft, amount);
        chargeLevel += actualRecharge;
        return actualRecharge;
    }

    /**
     * Discharge the battery by a specified amount.
     *
     * @param amount The amount of energy to discharge
     * @return The actual amount discharged
     */
    public int discharge(int amount) {
        if (amount <= 0) {
            return 0;
        }

        int actualDischarge = Math.min(chargeLevel, amount);
        chargeLevel -= actualDischarge;
        return actualDischarge;
    }
}
