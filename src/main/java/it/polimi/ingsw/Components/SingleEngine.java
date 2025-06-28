package it.polimi.ingsw.Components;

import it.polimi.ingsw.Direction;

/**
 * represents a single engine in the game.
 */
public final class SingleEngine extends Engine {

    public SingleEngine() {
        super(1, 0);
    }

    @Override
    public String getComponentString() {
        String s = this.getClass().getSimpleName().toLowerCase() + "_";
        s += getConnectionsString();
        return s;
    }

    /**
     * return the effective power of the engine.
     * If the engine is damaged, it returns 0.
     * If the engine is not damaged, it returns the power of the engine.
     * @return the effective power of the engine
     */
    @Override
    public int getEffectivePower() {
        if (damaged) {
            return 0;
        }
        return this.getPower();

    }
}
