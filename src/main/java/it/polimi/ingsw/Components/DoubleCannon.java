package it.polimi.ingsw.Components;

import it.polimi.ingsw.Direction;

public class DoubleCannon extends Cannon {
    public DoubleCannon() {
        super(2,2);
    }

    public DoubleCannon(Direction firingDirection) {
        super(2, 2, firingDirection);
    }

}


