package it.polimi.ingsw;

import it.polimi.ingsw.Components.*;

// Base class for all crew members
public abstract class Crew {
    protected CrewType type;

    public Crew(CrewType type) {
        this.type = type;
    }

    public CrewType getType() {
        return type;
    }

    // Abstract method to be implemented by specific crew types
    public abstract boolean canLiveIn(Cabin cabin);

    public abstract void applyAbility(ShipBuilder ship);
}




