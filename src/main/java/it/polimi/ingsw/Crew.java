package it.polimi.ingsw;

import java.io.Serializable;

import it.polimi.ingsw.Components.*;

// Base class for all crew members
public abstract class Crew implements Serializable{
    protected CrewType type; 

    public Crew(CrewType crewType) {
        this.type = crewType;
    }

    // Abstract method to be implemented by specific crew types
    public abstract boolean canLiveIn(Cabin cabin);

    //public abstract void applyAbility(ShipBuilder ship);

    public CrewType getType() {
        return type;
    }
}




