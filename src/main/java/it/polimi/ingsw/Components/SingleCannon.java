package it.polimi.ingsw.Components;

import it.polimi.ingsw.Direction;

/** represents a single cannon component
 *
 */
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

    /**
     * Returns the effective power of the cannon.
     * If the cannon is damaged, it returns 0.0.
     * If the cannon is facing forward, it returns the fire power.
     * If the cannon is not facing forward, it returns half the fire power.
     *
     * @return the effective power of the cannon
     */
    @Override
    public double getEffectivePower() {
        if (damaged) {
            return 0.0;
        }

        boolean isForwardFacing = getComponentDirection() == Direction.NORTH;

        return isForwardFacing ? this.getFirePower() : this.getFirePower() / 2.0;
    }
}
