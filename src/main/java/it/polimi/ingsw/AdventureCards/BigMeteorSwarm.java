package it.polimi.ingsw.AdventureCards;

import it.polimi.ingsw.Player;

/**
 * Big Meteor Swarm

 * Meteors can come at your ship from the left, the right, or even the rear!
 * Large meteors coming from in front of you can be blown up only by a forward-pointing cannon in the column from which
 * they are approaching, but large meteors from the side or the rear can be blown up by a cannon pointing toward the
 * meteor in the same or adjacent row or column. A reminder is printed on the flight board.
 */

public class BigMeteorSwarm extends AdventureCard {

    public BigMeteorSwarm() {

    }

    public BigMeteorSwarm(int copies, int duration) {
        super(copies, duration);
    }

    @Override
    int additionalPls() {
        return 0;
    }

    @Override
    void resolveEvent(Player p) {

    }

    @Override
    public void effect() {

    }
}