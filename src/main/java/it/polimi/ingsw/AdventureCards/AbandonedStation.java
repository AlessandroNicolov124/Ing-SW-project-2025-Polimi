package it.polimi.ingsw.AdventureCards;

import it.polimi.ingsw.Player;

/**
 * Abandoned Station

 * When fleeing the tragic disaster that befell this space station, the inhabitants probably left behind some good loot.
 * It will take a big crew to search for it, though. To use this opportunity, you must have at least as many crew as shown
 * on the card.

 * Only one player can use this opportunity. The leader decides first. If the leader does not have enough crew or if the
 * leader does not wish to lose those flight days, the leader can pass the opportunity to the next player along the
 * flight. Once someone decides to dock, the others are out of luck.

 * When you dock with a space station, load the indicated goods on your ship. You can rearrange or discard goods at this
 * time. Move your marker back the indicated number of flight days.

 * Note that on an Abandoned Station you lose no crew.
 */

public class AbandonedStation extends AdventureCard{

    public AbandonedStation(){

    }

    public AbandonedStation(int copies, int duration) {
        super(copies, duration);
    }

    @Override
    int additionalPls(){
        return 0;
    }

    @Override
    void resolveEvent(Player p) {

    }

    @Override
    public void effect(){

    }
}