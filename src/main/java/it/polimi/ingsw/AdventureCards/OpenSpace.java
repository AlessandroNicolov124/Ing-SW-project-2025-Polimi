package it.polimi.ingsw.AdventureCards;

import it.polimi.ingsw.Player;

/**
 * Open Space

 * Open Space is like a wide-open drag strip. Each player will have a chance to gain flight days.

 * In turn, each player declares their engine strength, beginning with the leader, and continuing in the order shown by
 * the rockets on the flight board.
 * You must decide whether to spend battery tokens on any double engines when it is your turn to declare engine strength.
 * Then you immediately move your rocket marker that many empty spaces forward. This may allow you to pass
 * players ahead of you (occupied spaces are skipped) and perhaps even take the lead.
 */

public class OpenSpace extends AdventureCard {

    public OpenSpace() {}
    public OpenSpace(int copies, int duration) {
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