package it.polimi.ingsw.AdventureCards;

import it.polimi.ingsw.Player;

/**
 * Advanced Enemies: Pirates

 * On your flight, you may also encounter Pirates and Slavers. When you defeat them, you don’t gain goods – you gain
 * cold, hard cosmic credits. Move your ship back the indicated number of empty spaces and take the credits from the
 * bank. If you don’t want to lose flight days, you can choose not to take the credits (or goods in the case of
 * Smugglers) and leave your ship where it is. Either way, no other player may collect the reward once the enemy has
 * been defeated.

 * If the Pirates defeat you, your ship gets shot at. (The card indicates the size and direction of the cannon fire)
 * Keep track of all the players who were defeated and then have the first defeated player roll two dice to determine
 * the row or column of each shot. This roll applies to all defeated players.
 */

public class Pirates extends AdventureCard {

    public Pirates() {}
    public Pirates(int copies, int duration) {
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