package it.polimi.ingsw.AdventureCards;

import it.polimi.ingsw.Player;

public abstract class AdventureCard {
    private int copies;
    private int duration;
    private int cost;

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

    public abstract void effect();

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

    public void setCost(int cost) {
        this.cost = cost;
    }

    public int getCost() {
        return cost;
    }

}



