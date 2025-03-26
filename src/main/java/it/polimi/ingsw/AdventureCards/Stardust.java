package it.polimi.ingsw.AdventureCards;

import it.polimi.ingsw.Player;

/**
 * Stardust

 * Yellow cards are special events. In your learning flight, the only special event is Stardust.
 * Every player loses 1 flight day for every exposed connector.
 * (Each exposed connector only counts once, regardless of whether it is one pipe, two pipes, or universal.) In reverse
 * order, starting with the last player, each player counts exposed connectors and moves back that many empty spaces.
 */

public class Stardust extends AdventureCard {

    public Stardust() {}
    public Stardust(int copies, int duration) {
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