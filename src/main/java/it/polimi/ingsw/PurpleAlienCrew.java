package it.polimi.ingsw;

import it.polimi.ingsw.Components.Cabin;
import it.polimi.ingsw.Components.LifeSupportSystemType;

public class PurpleAlienCrew extends Crew {

    public PurpleAlienCrew() {
        super(CrewType.PURPLE_ALIEN);
    }

    @Override
    public boolean canLiveIn(Cabin cabin) {
        // Check if cabin has a purple life support system connected
        return cabin.hasConnectedLifeSupport(LifeSupportSystemType.PURPLE);
    }

    public void applyAbility(ShipBuilder ship) {

    }

    public int getCannonBonus(){
        return 2;
    }
}
