package it.polimi.ingsw.Components;

import it.polimi.ingsw.Direction;

public final class SingleEngine extends Engine {

    public SingleEngine() {
        super(1, 0);
    }

    @Override
    public int getEffectivePower() {
        if (damaged) {
            return 0;
        }
        return this.getPower();

    }
}
