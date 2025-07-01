package it.polimi.ingsw.Components;

import it.polimi.ingsw.Direction;

public final class DoubleEngine extends Engine {
    private static final long serialVersionUID = 1L;

    private boolean isCharged; // Stato di carica

    public DoubleEngine() {
        super(2, 1);
        this.isCharged = false;
    }

    @Override
    protected String calculateComponentString() {
        String s = this.getClass().getSimpleName().toLowerCase() + "_";
        s += getConnectionsString();
        return s;
    }

    @Override
    public int getEffectivePower() {
        if (damaged) {
            return 0;
        }
        return getPowerWithBattery();
    }

    private int getPowerWithBattery(){
        if(isCharged){
            return this.getPower();
        }else{
            return this.getPower() / 2;
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