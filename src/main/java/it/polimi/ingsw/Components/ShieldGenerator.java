package it.polimi.ingsw.Components;

import it.polimi.ingsw.Direction;

import java.util.ArrayList;
import java.util.List;

public final class ShieldGenerator extends Component {

    private boolean isCharged; // Stato di carica
    private int energyConsumption;
    private List<Direction> protectedDirections;

    public ShieldGenerator() {
        super();
        this.isCharged = false;
        this.energyConsumption = 1;
        protectedDirections = new ArrayList<Direction>();
    }

    @Override
    public String getComponentString() {
        String s = this.getClass().getSimpleName().toLowerCase() + "_";
        s += getConnectionsString();
        return s;
    }

    public void add(Direction direction) {
        if( !protectedDirections.contains(direction)){
            protectedDirections.add(direction);
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

