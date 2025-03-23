package it.polimi.ingsw.Components;

import it.polimi.ingsw.*;
import it.polimi.ingsw.Visitors.Visitor;

// Classe Engine
public abstract class Cannon extends Component {
    private int firePower;
    private int energyConsumption;
    private Direction firingDirection;
    private boolean isCharged; // Stato di carica del cannone

    public Cannon(int power, int energyConsumption, Direction firingDirection) {
        super();
        this.firePower = power;
        this.energyConsumption = energyConsumption;
        this.isCharged = false;
        this.firingDirection = firingDirection;
        initializeConnectors();
    }

    @Override
    public void initializeConnectors() {
        // Il cannone ha connettori in tutte le direzioni tranne quella in cui spara
        for (Direction dir : Direction.values()) {
            if (dir != firingDirection) {
                connections.add(new Connector(dir, ConnectorType.UNIVERSAL));
            }
        }
    }

    public void setFiringDirection(Direction firingDirection) {
        this.firingDirection  = firingDirection;

        // Aggiorna i connettori in base alla nuova direzione di sparo
        connections.clear();
        for (Direction dir : Direction.values()) {
            if (dir != firingDirection) {
                connections.add(new Connector(dir, ConnectorType.UNIVERSAL));
            }
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

    public Position getTargetPosition() {
        if (componentPosition == null || componentDirection == null) {
            return null;
        }
        return componentDirection.getAdjacentPosition(componentPosition);
    }

    public boolean charge(BatteryCompartment battery) {
        if (battery == null || damaged || isCharged) {
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

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public abstract String getType();
}


