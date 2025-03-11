package it.polimi.ingsw;

public abstract class AdventureCard {
    private int copies;
    private int duration;

    public AdventureCard(int copies, int duration) {
        this.copies = copies;
        this.duration = duration;
    }

    abstract int additionalPls();

    // abstract void resolveEvent(Player p);

    abstract void effect();

    public void setCopies(int copies) {
        this.copies = copies;
    }
}



