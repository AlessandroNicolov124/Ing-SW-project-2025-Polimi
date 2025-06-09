package it.polimi.ingsw.Components;

import it.polimi.ingsw.Direction;

public final class SingleCannon extends Cannon {
    public SingleCannon() {
        super(1,0);
    }

    @Override
    public String getComponentString() {
        String s = this.getClass().getSimpleName().toLowerCase() + "_";
        s += getConnectionsString();
        return s;
    }


    @Override
    public boolean fire() {
        if(damaged){
            return false;
        }
        return true;
    }

    @Override
    public double getEffectivePower() {
        if (damaged) {
            return 0.0;
        }

        boolean isForwardFacing = getComponentDirection() == Direction.NORTH;

        return isForwardFacing ? this.getFirePower() : this.getFirePower() / 2.0;
    }
}
