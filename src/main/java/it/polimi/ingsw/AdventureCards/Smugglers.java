package it.polimi.ingsw.AdventureCards;

import it.polimi.ingsw.Player;

/**
 * Smugglers

 * Smugglers and other enemies pose a threat to everyone, but they attack the players’ ships in order. First, they
 * attack the leader. If they win, they attack the next player in line, and so on, until they have attacked everyone or
 * until someone defeats them.

 * The upper right corner of the card shows what happens if you lose. (These smugglers take your 2 most valuable goods.
 * If you run out of goods, they take batteries instead.) The bottom part of the card shows what you get if you win.
 * (If you defeat Smugglers, you can load the indicated goods – see Gaining Goods on page 11.)
 * The strength of the enemy is given by the number next to the cannon symbol. (These Smugglers have strength 4.)

 * The leader counts up cannon strength, paying for any double cannons used. If this strength is greater than the
 * enemy’s, the player wins and may claim the reward by losing flight days. If any player wins, the enemies go away and
 * the remaining players are not attacked.

 * If the player loses (has a lower strength than the enemy), the player pays the penalty specified in the upper half of
 * the card. Then the enemy attacks the next player.

 * In case of a tie (when the player has strength equal to the enemy’s) nothing happens to that player, but the enemy is
 * not defeated, either. The enemy moves on to attack the next player.
 */

public class Smugglers extends AdventureCard {

    public Smugglers() {}
    public Smugglers(int copies, int duration) {
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