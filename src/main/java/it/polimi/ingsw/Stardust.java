package it.polimi.ingsw;

public class Stardust extends AdventureCard {

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
    void effect() {

    }
}