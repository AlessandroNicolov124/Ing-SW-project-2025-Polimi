package it.polimi.ingsw.Components;

import it.polimi.ingsw.Direction;

/**
 * Represent a double engine component in the game.
 * it can be charged using a battery compartment.
 */
public final class DoubleEngine extends Engine {

    private boolean isCharged; // Stato di carica

    public DoubleEngine() {
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
     * the effective power is the power of the engine if it is not damaged
     * and it is charged, otherwise it is half of the power.
     */
    @Override
    public int getEffectivePower() {
        if (damaged) {
            return 0;
        }
        if(isCharged){
            return this.getPower();
        }else{
            return this.getPower() / 2;
        }
    }

    /**
     * Charges the engine using the provided battery compartment.
     * The engine can only be charged if it is not damaged,
     * it is not already charged,
     * and the battery compartment has enough charge.
     * @param battery the battery compartment to charge from
     * @return true if the engine was successfully charged, false otherwise
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

    /**
     * Calculates the power of the engine based on whether it is using a battery or not.
     * If the engine is damaged, it returns 0.
     * If usingBattery is true, it returns the power if charged, otherwise half of the power.
     * If usingBattery is false, it returns half of the power.
     *
     * @param usingBattery true if using battery, false otherwise
     * @return the calculated power
     */
    public int calculatePowerUsingBattery(boolean usingBattery){
        if (damaged) {
            return 0;
        }

        if(usingBattery){
            return getPower();
        }else{
            return this.getPower() / 2;
        }
    }
    public void discharge() {
        this.isCharged = false;
    }

    public boolean isCharge() {
        return isCharged;
    }
}