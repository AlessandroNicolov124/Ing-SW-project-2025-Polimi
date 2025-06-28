package it.polimi.ingsw;

import java.io.Serializable;

import it.polimi.ingsw.Components.*;

// Base class for all crew members
public abstract class Crew implements Serializable{
    protected CrewType type; 

    public Crew(CrewType crewType) {
        this.type = crewType;
    }
    /**
     * Checks if the crew can live in the specified cabin.
     *
     * @param cabin The cabin to check.
     * @return true if the crew can live in the cabin, false otherwise.
     */
    public abstract boolean canLiveIn(Cabin cabin);

    //public abstract void applyAbility(ShipBuilder ship);

    public CrewType getType() {
        return type;
    }
}




