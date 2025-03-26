package it.polimi.ingsw.AdventureCards;

import it.polimi.ingsw.Player;

/**
 * Combat Zone

 * The true test of any spaceship is to fly it through a combat zone. The Combat Zone card has 3 lines which are
 * evaluated in succession. Each line gives a criterion and a penalty for the player who is weakest in that area.

 * First, the player with the fewest crew figures loses 3 flight days.

 * Next, the player with the weakest engines loses 2 crew members. Players count up their engine strength in order,
 * starting with the leader, deciding whether or not to spend battery tokens on double engines.

 * Finally, the player with the weakest cannons is threatened by light cannon fire from behind and heavy cannon fire
 * from behind. Again, players decide in order, starting with the leader, which double cannons they will pay for.

 * Hits from cannon fire work like hits from meteors except they are more difficult to defend against. Each hit has a
 * direction. The player rolls two dice to determine which row or column the hit is coming from, thus determining which
 * component of the ship (if any) is in danger.

 * The only way to defend against light cannon fire is with a shield that protects against hits from that direction.
 * This can be powered at the cost of 1 battery token. Otherwise, the component is destroyed.

 * There is no way to defend against heavy cannon fire. Your only hope is to roll high enough or low enough that it
 * misses your ship entirely. Otherwise, the component it hits is destroyed.

 * In each criterion – smallest crew, weakest engines, weakest cannons – it is possible for players to be tied. Among
 * tied players, the one farthest ahead is the only one who must face the penalty. (Usually, being ahead is good; in a
 * combat zone, it’s dangerous.)
 */

public class CombatZone extends AdventureCard {

    public CombatZone(){

    }
    public CombatZone(int copies, int duration) {
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
