package it.polimi.ingsw.AdventureCards;

import it.polimi.ingsw.Player;
import it.polimi.ingsw.GameModel;

/**
 * Abandoned Station

 * When fleeing the tragic disaster that befell this space station, the inhabitants probably left behind some good loot.
 * It will take a big crew to search for it, though. To use this opportunity, you must have at least as many crew as shown
 * on the card.

 * Only one player can use this opportunity. The leader decides first. If the leader does not have enough crew or if the
 * leader does not wish to lose those flight days, the leader can pass the opportunity to the next player along the
 * flight. Once someone decides to dock, the others are out of luck.

 * When you dock with a space station, load the indicated goods on your ship. You can rearrange or discard goods at this
 * time. Move your marker back the indicated number of flight days.

 * Note that on an Abandoned Station you lose no crew.
 */

public class AbandonedStation extends AdventureCard{
    private int minCrewRequired;
    private int flightDaysCost;
    private int goodsToLoad;
    private boolean opportunityUsed;

    public AbandonedStation(int copies, int duration, int minCrewRequired, int flightDaysCost, int goodsToLoad) {
        super(copies, duration);
        this.minCrewRequired = minCrewRequired;
        this.flightDaysCost = flightDaysCost;
        this.goodsToLoad = goodsToLoad;
        this.opportunityUsed = false;
    }

    @Override
    int additionalPls(){
        return 0;
    }

    @Override
    void resolveEvent(Player p) {
        // Implementation depends on how player choice is handled in the game
    }

    @Override
    public void effect(){
        // This would be called when the card is drawn
    }
    
    /**
     * Allow a player to dock at the abandoned station
     * @param player The player who wants to dock
     * @param gameModel The current game state
     * @return true if docking was successful, false otherwise
     */
    public boolean dockAtStation(Player player, GameModel gameModel) {
        if (opportunityUsed) {
            return false; // Opportunity already used by another player
        }
        
        // Check if player has enough crew
        ShipBuilder ship = player.getShip();
        // Logic to check crew count would go here
        // For example: int crewCount = ship.getCrewCount();
        int crewCount = 0; // Placeholder
        
        if (crewCount < minCrewRequired) {
            return false; // Not enough crew
        }
        
        // Load goods on player's ship
        // Logic to add goods to player's inventory here
        
        // Apply flight day cost
        gameModel.getFlightBoard().updatePosition(player, -flightDaysCost);
        
        opportunityUsed = true;
        return true;
    }
    
    // Getters and setters
    public int getMinCrewRequired() {
        return minCrewRequired;
    }
    
    public int getFlightDaysCost() {
        return flightDaysCost;
    }
    
    public int getGoodsToLoad() {
        return goodsToLoad;
    }
    
    public boolean isOpportunityUsed() {
        return opportunityUsed;
    }
}