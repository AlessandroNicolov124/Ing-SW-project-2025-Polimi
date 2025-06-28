package it.polimi.ingsw;

import it.polimi.ingsw.Components.Cabin;
import it.polimi.ingsw.Components.LifeSupportSystemType;

/**
 * This class represents a crew of purple aliens in the game.
 * It extends the Crew class and provides specific functionality for purple alien crews.
 */
public class PurpleAlienCrew extends Crew {

    public PurpleAlienCrew() {
        super(CrewType.PURPLE_ALIEN);
    }

    @Override
    public boolean canLiveIn(Cabin cabin) {
        // Check if cabin has a purple life support system connected
        return cabin.hasConnectedLifeSupport(LifeSupportSystemType.PURPLE);
    }

}
