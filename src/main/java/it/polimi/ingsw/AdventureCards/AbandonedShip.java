package it.polimi.ingsw.AdventureCards;

import it.polimi.ingsw.*;


/**
 * Abandoned Ship

 * If you find an Abandoned Ship, you're in gravy! There's probably some sort of protocol for reporting these things, but
 * who cares? Fix it up and sell it to your crew. (Yeah, they're so sick of flying with you, they'll pay for the chance to
 * jump ship.)

 * Only one player can use this opportunity. The leader decides first. The leader can give up the specified number of crew
 * figures and take the indicated number of cosmic credits. This also costs flight days.

 * If the leader chooses not to use the opportunity, it falls to the next player in line, and so on. Once one player fixes
 * up the ship, the remaining players are out of luck.

 * Sometimes you'll find a ship that you'll be tempted to keep for yourself. Don't be greedy. Let the crew have it.
 * There's probably a reason that ship was abandoned.
 */

public class AbandonedShip extends AdventureCard {
    private int crewRequired;
    private int creditsGained;
    private int flightDaysCost;
    private boolean opportunityUsed;

    public AbandonedShip(int copies, int duration, int crewRequired, int creditsGained, int flightDaysCost) {
        super(copies, duration);
        this.crewRequired = crewRequired;
        this.creditsGained = creditsGained;
        this.flightDaysCost = flightDaysCost;
        this.opportunityUsed = false;
    }

    @Override
    int additionalPls(){
        return 0;
    }

    @Override
    void resolveEvent(Player p) {
        // The leader gets first opportunity, if declined it passes to the next player
    }

    @Override
    public void effect(){
        // This would be called when the card is drawn
    }

    /**
     * Allow a player to use the abandoned ship opportunity
     * @param player The player who wants to use the opportunity
     * @param gameModel The current game state
     * @return true if the opportunity was successfully used, false otherwise
     */
    public boolean useOpportunity(Player player, GameModel gameModel) {
        if (opportunityUsed) {
            return false; // Opportunity already used by another player
        }
        
        // Implement crew figure check and exchange logic
        // This is a simplified implementation
        int currentCredits = gameModel.getCredits(player);
        
        // Check if player has enough crew to trade
        ShipBuilder ship = player.getShip();
        // Logic to check and remove crew would go here
        
        // If successful, update player's credits and mark opportunity as used
        gameModel.setCredits(player, currentCredits + creditsGained);
        
        // Apply flight day cost
        // This would involve updating the flight board
        gameModel.getFlightBoard().updatePosition(player, -flightDaysCost);
        
        opportunityUsed = true;
        return true;
    }

    // Getters and setters
    public int getCrewRequired() {
        return crewRequired;
    }

    public int getCreditsGained() {
        return creditsGained;
    }

    public int getFlightDaysCost() {
        return flightDaysCost;
    }

    public boolean isOpportunityUsed() {
        return opportunityUsed;
    }
}