package it.polimi.ingsw.AdventureCards;

import it.polimi.ingsw.Player;

/**
 * Epidemic

 * Epidemic makes you remove 1 crew member (human or alien) from every occupied cabin that is joined to another occupied
 * cabin. The safe thing to do is to build your ship so that no two cabins are joined. If you do have joined cabins,
 * maybe you can find an adventure that will allow you to empty one of them before the epidemic strikes.
 */

public class Epidemic extends AdventureCard {

    public Epidemic() {}
    public Epidemic(int copies, int duration) {
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