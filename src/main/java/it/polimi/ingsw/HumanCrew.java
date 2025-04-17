package it.polimi.ingsw;

import it.polimi.ingsw.Components.Cabin;

public class HumanCrew extends Crew {

    public HumanCrew() {
        super(CrewType.HUMAN);
    }

    @Override
    public boolean canLiveIn(Cabin cabin) {
        // Humans can live in any cabin (they wear spacesuits)
        return true;
    }

    @Override
    public void applyAbility(ShipBuilder ship) {

    }
}