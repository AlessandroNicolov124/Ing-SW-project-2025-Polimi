package it.polimi.ingsw.AdventureCards;

import it.polimi.ingsw.Player;

/**
 * Sabotage

 * Sabotage destroys a random component on the ship with the smallest crew. (Among multiple ships tied for smallest
 * crew, only the one farthest ahead is sabotaged.) To select the component, the affected player rolls 2 dice to
 * determine the column and then 2 dice to determine the row. If there is no component at those coordinates, roll again
 * for new coordinates. If there is no component there, roll again. If three sets of coordinates are rolled without
 * hitting a component, the saboteurs give up and nothing happens.

 * If the saboteurs destroy a component, it goes to your discard pile, as do any others that fall off as a result.
 * A hit to the center of the ship can be devastating.
 */

public class Sabotage extends AdventureCard {

    public Sabotage() {

    }
    public Sabotage(int copies, int duration) {
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