package it.polimi.ingsw;

import it.polimi.ingsw.Components.Cabin;
import it.polimi.ingsw.Components.LifeSupportSystemType;

/**
 * Represents a Brown Alien Crew in the game.
 * This crew can only live in cabins with a brown life support system connected.
 */
public class BrownAlienCrew extends Crew {

    public BrownAlienCrew() {
        super(CrewType.BROWN_ALIEN);
    }

    @Override
    public boolean canLiveIn(Cabin cabin) {
        // Check if cabin has a brown life support system connected
        return cabin.hasConnectedLifeSupport(LifeSupportSystemType.BROWN);
    }

}
