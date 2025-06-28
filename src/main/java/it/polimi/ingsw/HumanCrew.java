package it.polimi.ingsw;

import it.polimi.ingsw.Components.Cabin;

/**
 * Represents a crew of humans in the game.
 * Humans can live in any cabin as they wear spacesuits.
 */
public class HumanCrew extends Crew {

    public HumanCrew() {
        super(CrewType.HUMAN);
    }

    @Override
    public boolean canLiveIn(Cabin cabin) {
        // Humans can live in any cabin (they wear spacesuits)
        return true;
    }

//    @Override
//    public void applyAbility(ShipBuilder ship) {
//
//    }
}
