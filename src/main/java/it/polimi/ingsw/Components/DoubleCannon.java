package it.polimi.ingsw.Components;

import it.polimi.ingsw.Direction;

/**
 * Represents a double cannon component in the game.
 * This cannon can fire with a specific power and can be charged using a battery.
 * It has a unique firing mechanism where it can only fire if it is charged and not damaged.
 */
public final class DoubleCannon extends Cannon {
    private boolean isCharged; // Stato di carica del cannone

    public DoubleCannon() {
        super(2, 1);
        this.isCharged = false;
    }

    @Override
    public String getComponentString() {
        String s = this.getClass().getSimpleName().toLowerCase() + "_";
        s += getConnectionsString();
        return s;
    }

    /**
     * checks if the cannon can fire.
     * it is possible to fire only if the cannon is charged and not damaged.
     * @return true if the cannon can fire, false otherwise
     */
    @Override
    public boolean fire() {
        if (!isCharged || damaged) {
            return false;
        }

        isCharged = false;
        return true;
    }

    /**
     * The effective power is the fire power of the cannon if it point
     * direction NORTH, otherwise it is half of the fire power.
     * @return the effective power of the cannon
     */
    @Override
    public double getEffectivePower() {
        if (damaged) {
            return 0.0;
        }
        boolean isForwardFacing = getComponentDirection() == Direction.NORTH;

        return isForwardFacing ? getPowerWithBattery() : getPowerWithBattery() / 2.0;
    }

    /**
     * Calculates the power of the cannon based on whether it is using a battery or not.
     * If the cannon is damaged, it returns 0.0.
     * If usingBattery is true, it returns the fire power if facing forward, otherwise half of the fire power.
     * If usingBattery is false, it returns half of the fire power.
     *
     * @param usingBattery true if using battery, false otherwise
     * @return the calculated power
     */
    public double calculatePowerUsingBattery(boolean usingBattery){
        if (damaged) {
            return 0.0;
        }

        boolean isForwardFacing = getComponentDirection() == Direction.NORTH;

        if(usingBattery){
            return isForwardFacing ? getFirePower() : getFirePower()  / 2.0;
        }else{
            return (double) this.getFirePower() / 2;
        }
    }

    /**
     * Returns the power of the cannon considering if it is charged or not.
     * If charged, it returns the fire power, otherwise it returns half of the fire power.
     *
     * @return the power of the cannon with battery consideration
     */
    private double getPowerWithBattery(){
        if(isCharged){
            return this.getFirePower();
        }else{
            return (double) this.getFirePower() / 2;
        }
    }

    /**
     * Charges the cannon using the provided battery.
     * The cannon can only be charged if it is not damaged, not already charged,
     * and the battery has enough charge to cover the energy consumption.
     *
     * @param battery the battery compartment to charge from
     * @return true if the cannon was successfully charged, false otherwise
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

    public boolean isCharge() {
        return this.isCharged;
    }

    public void discharge() {
        this.isCharged = false;
    }
}
