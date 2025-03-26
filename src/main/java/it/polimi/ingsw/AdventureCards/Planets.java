package it.polimi.ingsw.AdventureCards;

import it.polimi.ingsw.Player;

/**
 * Planets

 * A Planets card has 2 to 4 planets where you can pick up goods. Landing will cost you the number of flight days shown in
 * the lower right corner. If you want to land, mark the planet you choose with your second rocket (the one not on the
 * flight board). Only one rocket is allowed per planet.

 * The leader chooses first, followed by the other players in order. No one is required to land. In fact, players in front
 * sometimes prevent the others from landing at all.

 * Those who landed load the indicated goods on their ships. (See Gaining Goods on page 11.) Goods can be rearranged or
 * discarded at this time. It is legal to land just to block others from landing, but be sure it is worth the loss of
 * flight days.

 * Once everyone has decided whether to land, those who landed move their markers back that many empty spaces, with the
 * player who is farthest behind moving first.
 */

public class Planets extends AdventureCard{

    public Planets(){

    }

    public Planets(int copies, int duration) {
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