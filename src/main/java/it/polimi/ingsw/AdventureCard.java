package it.polimi.ingsw;

public abstract class AdventureCard {
    private int copies;
    private int duration;

    public AdventureCard(int copies, int duration) {
        this.copies = copies;
        this.duration = duration;
    }

    public AdventureCard() {
        this.copies = 0;
        this.duration = 1;
    }

    abstract int additionalPls();

    abstract void resolveEvent(Player p);

    abstract void effect();

    public void setCopies(int copies) {
        this.copies = copies;
    }

    public int getCopies() {
        return copies;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getDuration() {
        return duration;
    }

}



