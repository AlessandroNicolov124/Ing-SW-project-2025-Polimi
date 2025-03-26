package it.polimi.ingsw.AdventureCards;

import it.polimi.ingsw.*;


/**
 * Abandoned Ship

 * If you find an Abandoned Ship, you’re in gravy! There’s probably some sort of protocol for reporting these things, but
 * who cares? Fix it up and sell it to your crew. (Yeah, they’re so sick of flying with you, they’ll pay for the chance to
 * jump ship.)

 * Only one player can use this opportunity. The leader decides first. The leader can give up the specified number of crew
 * figures and take the indicated number of cosmic credits. This also costs flight days.

 * If the leader chooses not to use the opportunity, it falls to the next player in line, and so on. Once one player fixes
 * up the ship, the remaining players are out of luck.

 * Sometimes you’ll find a ship that you’ll be tempted to keep for yourself. Don’t be greedy. Let the crew have it.
 * There’s probably a reason that ship was abandoned.
 */

public class AbandonedShip extends AdventureCard {

    public AbandonedShip(){

    }
    public AbandonedShip(int copies, int duration) {
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