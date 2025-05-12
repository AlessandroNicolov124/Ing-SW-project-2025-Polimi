package it.polimi.ingsw.Components;

import it.polimi.ingsw.Direction;

public final class DoubleCannon extends Cannon {
    public DoubleCannon() {
        super(2,2);
    }

    public DoubleCannon(Direction firingDirection) {
        super(2, 2);
    }

    @Override
    public double calculateBasePower() {
        return 0.0; // Un cannone doppio non può funzionare senza batteria
    }

    @Override
    public double calculatePowerWithBattery() {
        return this.getFirePower(); // Potenza piena solo con batteria
    }
}
