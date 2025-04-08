package it.polimi.ingsw.Components;

import it.polimi.ingsw.*;

public sealed abstract class Cannon extends Component permits SingleCannon, DoubleCannon {
    private int firePower;
    private Direction firingDirection;
    private boolean isCharged; // Stato di carica del cannone
    private int energyConsumption;

    public Cannon(int power, int energyConsumption, Direction firingDirection) {
        super();
        this.firePower = power;
        this.energyConsumption = energyConsumption;
        this.isCharged = false;
        this.firingDirection = firingDirection;
    }

    public Cannon(int power, int energyConsumption) {
        this(power, energyConsumption, Direction.NORTH); // Default direction
    }

    public boolean validateFiringDirection(Direction firingDirection) {
        for(Connector conn : connections){
            if(conn.getDirection() == firingDirection && conn.getConnectorType() != ConnectorType.NULL){
                    return false;
            }
        }
        return true;
    }

    public void setFiringDirection(Direction firingDirection) {
        if(validateFiringDirection(firingDirection)){
            this.firingDirection = firingDirection;
        }else{
            System.out.println("Firing direction is not valid");
        }
    }


    public int getFirePower() {
        return firePower;
    }
    public void setFirePower(int firePower) {
        this.firePower = firePower;
    }
    public int getEnergyConsumption() {
        return energyConsumption;
    }
    public void setEnergyConsumption(int energyConsumption) {
        this.energyConsumption = energyConsumption;
    }

    public Direction getFiringDirection() {
        return firingDirection;
    }

    public boolean isCharged() {
        return isCharged;
    }

    public boolean charge(BatteryCompartment battery) {
        if (battery == null || damaged || isCharged || battery.getChargeLevel() < energyConsumption) {
            return false;
        }

        boolean success = battery.useCharge(energyConsumption);
        if (success) {
            isCharged = true;
        }
        return success;
    }

    public boolean fire() {
        if (!isCharged || damaged) {
            return false;
        }

        isCharged = false;
        return true;
    }

    public abstract double calculateBasePower();
    public abstract double calculatePowerWithBattery();

    public double getEffectivePower() {
        if (damaged) {
            return 0.0;
        }

        boolean isForwardFacing = getFiringDirection() == Direction.NORTH;

        if (isCharged) {
            return isForwardFacing ? calculatePowerWithBattery() : calculatePowerWithBattery() / 2.0;
        } else {
            return isForwardFacing ? calculateBasePower() : calculateBasePower() / 2.0;
        }
    }

}


