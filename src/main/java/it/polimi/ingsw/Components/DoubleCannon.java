package it.polimi.ingsw.Components;

import it.polimi.ingsw.Direction;

public class DoubleCannon extends Cannon {
    public DoubleCannon() {
        super(2,2, Direction.NORTH);
    }

    @Override
    public String getType() {
        return "DoubleCannon";
    }

}


