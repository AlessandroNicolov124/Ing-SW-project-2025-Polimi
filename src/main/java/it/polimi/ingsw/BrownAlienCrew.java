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

//    @Override
//    public void applyAbility(ShipBuilder ship) {
//    // Adds +2 to engine power if ship already has engine power
////        if (ship.getEnginePower() > 0) {
////            ship.modifyEnginePower(getEngineBonus());
////        }
//    }


//    public int getEngineBonus(){
//        return 2;
//    }
}
