package it.polimi.ingsw;

public class OpenSpace extends AdventureCard {

    public OpenSpace() {}
    public OpenSpace(int copies, int duration) {
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