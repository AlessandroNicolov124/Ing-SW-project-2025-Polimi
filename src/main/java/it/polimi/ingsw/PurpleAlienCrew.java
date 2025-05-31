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

//    public void applyAbility(ShipBuilder ship) {
//        // Adds +2 to fire power if ship already has fire power
//        if (ship.calculateFirePower() > 0) {
//            //ship.modifyFirePower(getCannonBonus());
//        }
//    }

//    public int getCannonBonus(){
//        return 2;
//    }
}
