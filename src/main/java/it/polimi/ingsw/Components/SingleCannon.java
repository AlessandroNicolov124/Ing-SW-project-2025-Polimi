package it.polimi.ingsw.Components;

import it.polimi.ingsw.Direction;

public final class SingleCannon extends Cannon {
    public SingleCannon() {
        super(1,0);
    }

    public SingleCannon(Direction firingDirection) {
        super(1, 0);
        this.setComponentDirection(firingDirection);
    }

    @Override
    public double calculateBasePower() {
        return this.getFirePower();
    }

    @Override
    public double calculatePowerWithBattery() {
        return this.getFirePower(); // cannone singolo non beneficia della batteria
    }
}
