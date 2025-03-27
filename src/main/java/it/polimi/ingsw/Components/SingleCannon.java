package it.polimi.ingsw.Components;

import it.polimi.ingsw.Direction;

public class SingleCannon extends Cannon {
    public SingleCannon() {
        super(1,1, Direction.NORTH);
    }

    @Override
    public String getType() {
        return "SingleCannon";
    }

}
