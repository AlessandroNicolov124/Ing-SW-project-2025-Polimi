package it.polimi.ingsw.Components;

import it.polimi.ingsw.Direction;

public final class DoubleCannon extends Cannon {
    private boolean isCharged; // Stato di carica del cannone

    public DoubleCannon() {
        super(2, 1);
        this.isCharged = false;
    }

    @Override
    public boolean fire() {
        if (!isCharged || damaged) {
            return false;
        }

        isCharged = false;
        return true;
    }

    @Override
    public double getEffectivePower() {
        if (damaged) {
            return 0.0;
        }
        //boolean isForwardFacing = getFiringDirection() == Direction.NORTH;
        boolean isForwardFacing = getComponentDirection() == Direction.NORTH;

        return isForwardFacing ? getPowerWithBattery() : getPowerWithBattery() / 2.0;
    }

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

    private double getPowerWithBattery(){
        if(isCharged){
            return this.getFirePower();
        }else{
            return (double) this.getFirePower() / 2;
        }
    }

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
