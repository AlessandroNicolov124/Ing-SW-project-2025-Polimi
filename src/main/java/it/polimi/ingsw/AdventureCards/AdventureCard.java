package it.polimi.ingsw.AdventureCards;

import java.io.Serializable;

import it.polimi.ingsw.Player;

public abstract class AdventureCard implements Serializable{
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

    protected abstract int additionalPls();

    protected abstract void resolveEvent(Player p);

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

    /**
     * Returns a description of what this adventure card does.
     * To be overridden by specific card implementations.
     * 
     * @return A string describing the card's effects
     */
    public String getDescription() {
        return "Adventure card with unknown effects.";
    }

    /**
     * Returns the name of this adventure card.
     * By default returns the simple class name.
     * 
     * @return The card's name
     */
    public String getName() {
        return this.getClass().getSimpleName();
    }

}



