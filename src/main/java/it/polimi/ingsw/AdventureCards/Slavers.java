package it.polimi.ingsw.AdventureCards;

import it.polimi.ingsw.Player;

/**
 * Advanced Enemies: Slavers

 * On your flight, you may also encounter Pirates and Slavers. When you defeat them, you don’t gain goods – you gain
 * cold, hard cosmic credits. Move your ship back the indicated number of empty spaces and take the credits from the
 * bank. If you don’t want to lose flight days, you can choose not to take the credits (or goods in the case of
 * Smugglers) and leave your ship where it is. Either way, no other player may collect the reward once the enemy has
 * been defeated.

 * If the Slavers defeat you, they force you to give up some of your crew. You choose which humans or aliens to
 * surrender in exchange for your own freedom.
 */

public class Slavers extends AdventureCard {

    public Slavers() {}

    public Slavers(int copies, int duration) {
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