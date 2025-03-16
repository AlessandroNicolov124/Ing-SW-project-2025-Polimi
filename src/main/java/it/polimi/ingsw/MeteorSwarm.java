package it.polimi.ingsw;

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
    void effect() {

    }
}