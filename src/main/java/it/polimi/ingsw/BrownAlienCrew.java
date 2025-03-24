package it.polimi.ingsw;

import it.polimi.ingsw.Components.Cabin;
import it.polimi.ingsw.Components.LifeSupportSystemType;

public class BrownAlienCrew extends Crew {

    public BrownAlienCrew() {
        super(CrewType.BROWN_ALIEN);
    }

    @Override
    public boolean canLiveIn(Cabin cabin) {
        // Check if cabin has a brown life support system connected
        return cabin.hasConnectedLifeSupport(LifeSupportSystemType.BROWN);
    }

    @Override
    public void applyAbility(ShipBuilder ship) {

    }


    public int getEngineBonus(){
        return 2;
    }
}
