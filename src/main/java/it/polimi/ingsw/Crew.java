package it.polimi.ingsw;

import it.polimi.ingsw.Components.*;

/**
 * Represents a Crew in the game.
 * This class serves as a base for different types of crews.
 */
public abstract class Crew {
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

    public CrewType getType() {
        return type;
    }
}




