package it.polimi.ingsw.AdventureCards;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import it.polimi.ingsw.GameModel;
import it.polimi.ingsw.Player;

public abstract class AdventureCard implements Serializable{
    private static final long serialVersionUID = 1L;
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

    public abstract void effect(GameModel gameModel);

    /**
     * Utility method to apply flight day costs to multiple players in reverse order.
     * Players farthest behind move first, as required by game rules.
     * 
     * @param gameModel The game model
     * @param playerFlightDayCosts Map of players to their flight day costs (positive values = days lost)
     */
    protected void applyFlightDayCostsInReverseOrder(GameModel gameModel, Map<Player, Integer> playerFlightDayCosts) {
        if (playerFlightDayCosts.isEmpty()) {
            return;
        }
        
        // Apply flight day costs directly (no conversion needed - positive values move to worse positions)
        gameModel.updatePositionsInReverseOrder(playerFlightDayCosts);
        
        // Log the operation
        System.out.println("[AdventureCard] Applied flight day costs in reverse order for " + 
                          playerFlightDayCosts.size() + " players");
        for (Map.Entry<Player, Integer> entry : playerFlightDayCosts.entrySet()) {
            System.out.println("  - " + entry.getKey().getNickName() + " lost " + entry.getValue() + " flight days");
        }
    }

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



