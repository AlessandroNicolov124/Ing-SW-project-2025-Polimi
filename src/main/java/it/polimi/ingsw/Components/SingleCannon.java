package it.polimi.ingsw.Components;

import it.polimi.ingsw.Direction;

public class SingleCannon extends Cannon {
    public SingleCannon() {
        super(1,1);
    }

    public SingleCannon(Direction firingDirection) {
        super(1, 1, firingDirection);
    }

    @Override
    public boolean isDoubleCannon() {return false;}


}
