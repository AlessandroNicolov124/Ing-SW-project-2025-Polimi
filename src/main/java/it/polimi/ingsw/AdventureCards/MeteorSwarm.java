package it.polimi.ingsw.AdventureCards;

import it.polimi.ingsw.Player;

/**
 * Meteor Swarm

 * A Meteor Swarm can really mess with your paint job. The card depicts several large and/or small meteors and the
 * directions they come from. Deal with meteors one at a time, top to bottom. They affect all players simultaneously.

 * For each meteor, the leader rolls two dice. The roll determines which row or column the meteor can impact.
 * The rows and columns are numbered on the edge of your ship board. Each player receives their own personal meteor and
 * checks to see if it hits or misses their ship. If the roll does not miss, proceed as follows:

 * A small meteor will harmlessly bounce off of a well-built ship. It is only a problem if it hits an exposed connector.
 * In this case, you can still avoid damage by powering up a shield if you have one that protects that side.
 * You must pay 1 battery token to do this. If you can’t or won’t avoid the impact, the component the meteor hits is
 * destroyed. Remove it from your ship and add it to your pile of components lost along the way.

 * A large meteor is, of course, even more dangerous. It will damage even a well-built ship, and shields cannot stop it.
 * Your only hope is to blast it. You can only shoot it if you have a cannon pointed at it in the same column.
 * If it is a double cannon, you will have to pay 1 battery token to use it. Large meteors tend to come from in front of
 * you, which is why we recommended you pay special attention to cannons pointing forward.
 * If you don’t shoot a large meteor, the component it hits is destroyed.
 */

public class MeteorSwarm extends AdventureCard {

    public MeteorSwarm() {}
    public MeteorSwarm(int copies, int duration) {
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