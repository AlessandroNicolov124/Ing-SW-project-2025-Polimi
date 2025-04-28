package it.polimi.ingsw.AdventureCards;

import it.polimi.ingsw.Player;
import it.polimi.ingsw.GameModel;
import it.polimi.ingsw.Components.*;
import java.util.*;

/**
 * Advanced Enemies: Slavers

 * On your flight, you may also encounter Pirates and Slavers. When you defeat them, you don't gain goods – you gain
 * cold, hard cosmic credits. Move your ship back the indicated number of empty spaces and take the credits from the
 * bank. If you don't want to lose flight days, you can choose not to take the credits (or goods in the case of
 * Smugglers) and leave your ship where it is. Either way, no other player may collect the reward once the enemy has
 * been defeated.

 * If the Slavers defeat you, they force you to give up some of your crew. You choose which humans or aliens to
 * surrender in exchange for your own freedom.
 */

public class Slavers extends AdventureCard {
    private int slaverStrength;
    private int rewardCredits;
    private int flightDaysCost;
    private int crewLoss;
    private boolean isDefeated;

    public Slavers() {
        super();
        this.slaverStrength = 4; // Default slaver strength
        this.rewardCredits = 3;  // Default reward
        this.flightDaysCost = 2; // Default flight days cost
        this.crewLoss = 1;       // Default crew loss when defeated
        this.isDefeated = false;
    }

    public Slavers(int copies, int duration, int slaverStrength, int rewardCredits, int flightDaysCost, int crewLoss) {
        super(copies, duration);
        this.slaverStrength = slaverStrength;
        this.rewardCredits = rewardCredits;
        this.flightDaysCost = flightDaysCost;
        this.crewLoss = crewLoss;
        this.isDefeated = false;
    }

    @Override
    int additionalPls() {
        return 0;
    }

    @Override
    void resolveEvent(Player p) {
        // Individual player resolution would depend on the game mechanics
    }

    @Override
    public void effect() {
        // This would be called when the card is drawn
    }
    
    /**
     * Attempt to fight slavers
     * @param player The player trying to fight
     * @param gameModel The current game state
     * @return true if slavers were defeated, false if player was defeated
     */
    public boolean fightSlavers(Player player, GameModel gameModel) {
        if (isDefeated) {
            return true; // Slavers already defeated
        }
        
        // Calculate player's cannon strength
        int playerStrength = calculateCannonStrength(player);
        
        // Check if player defeats slavers
        boolean playerWins = playerStrength >= slaverStrength;
        
        if (playerWins) {
            // Player wins, offer reward
            isDefeated = true;
            return true;
        } else {
            // Player is defeated, they must surrender crew
            return false;
        }
    }
    
    /**
     * Calculate the cannon strength of a player's ship
     * @param player The player
     * @return Total cannon strength
     */
    private int calculateCannonStrength(Player player) {
        int strength = 0;
        List<Component> components = player.getShip().getAllComponents();
        
        // Find all cannons and count their strength
        for (Component component : components) {
            if (component instanceof Cannon) {
                Cannon cannon = (Cannon) component;
                strength += 1;
                
                // Double cannons provide extra strength if powered
                //if (cannon.isDoubleCannon()) {
                //    // Assuming player chooses to power double cannons when fighting slavers
                //    strength += 1;
                //}
            }
        }
        
        return strength;
    }
    
    /**
     * Offer the player a reward for defeating slavers
     * @param player The player who defeated the slavers
     * @param gameModel The current game state
     * @param acceptReward Whether the player accepts the reward
     */
    public void offerReward(Player player, GameModel gameModel, boolean acceptReward) {
        if (!isDefeated) {
            return; // Slavers weren't defeated
        }
        
        if (acceptReward) {
            // Give credits to player
            int currentCredits = gameModel.getCredits(player);
            gameModel.setCredits(player, currentCredits + rewardCredits);
            
            // Move player back on flight board
            //gameModel.getFlightBoard().updatePosition(player, -flightDaysCost);
        }
        // If player doesn't accept reward, nothing happens to their position
    }
    
    /**
     * Force a defeated player to surrender crew
     * @param player The player who was defeated
     * @return true if crew was successfully surrendered, false otherwise
     */
    public boolean surrenderCrew(Player player) {
        // This implements the mechanism for player to choose which crew to surrender

        // Verify player has enough crew to surrender
        int playerCrew = getCrewCount(player);
        
        if (playerCrew < crewLoss) {
            return false; // Not enough crew to surrender
        }
        
        // Logic to remove the crew

        return true;
    }
    
    /**
     * Get the crew count for a player
     * @param player The player
     * @return Number of crew members
     */
    private int getCrewCount(Player player) {
        // This would need to count crew based on the game's implementation
        return 2; // Placeholder for crew count
    }
    
    // Getters
    public int getSlaverStrength() {
        return slaverStrength;
    }
    
    public int getRewardCredits() {
        return rewardCredits;
    }
    
    public int getFlightDaysCost() {
        return flightDaysCost;
    }
    
    public int getCrewLoss() {
        return crewLoss;
    }
    
    public boolean isDefeated() {
        return isDefeated;
    }
}