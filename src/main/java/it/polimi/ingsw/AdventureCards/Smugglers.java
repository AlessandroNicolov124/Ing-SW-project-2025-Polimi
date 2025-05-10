package it.polimi.ingsw.AdventureCards;

import it.polimi.ingsw.Player;
import it.polimi.ingsw.GameModel;
import it.polimi.ingsw.Components.*;
import java.util.*;

/**
 * Smugglers

 * Smugglers and other enemies pose a threat to everyone, but they attack the players' ships in order. First, they
 * attack the leader. If they win, they attack the next player in line, and so on, until they have attacked everyone or
 * until someone defeats them.

 * The upper right corner of the card shows what happens if you lose. (These smugglers take your 2 most valuable goods.
 * If you run out of goods, they take batteries instead.) The bottom part of the card shows what you get if you win.
 * (If you defeat Smugglers, you can load the indicated goods – see Gaining Goods on page 11.)
 * The strength of the enemy is given by the number next to the cannon symbol. (These Smugglers have strength 4.)

 * The leader counts up cannon strength, paying for any double cannons used. If this strength is greater than the
 * enemy's, the player wins and may claim the reward by losing flight days. If any player wins, the enemies go away and
 * the remaining players are not attacked.

 * If the player loses (has a lower strength than the enemy), the player pays the penalty specified in the upper half of
 * the card. Then the enemy attacks the next player.

 * In case of a tie (when the player has strength equal to the enemy's) nothing happens to that player, but the enemy is
 * not defeated, either. The enemy moves on to attack the next player.
 */

public class Smugglers extends AdventureCard {
    private int smugglerStrength;
    private int goodsLostOnDefeat;
    private int goodsGainedOnVictory;
    private int flightDaysCost;
    private boolean isDefeated;

    public Smugglers(int copies, int duration, int smugglerStrength, int goodsLostOnDefeat, 
                     int goodsGainedOnVictory, int flightDaysCost) {
        super(copies, duration);
        this.smugglerStrength = smugglerStrength;
        this.goodsLostOnDefeat = goodsLostOnDefeat;
        this.goodsGainedOnVictory = goodsGainedOnVictory;
        this.flightDaysCost = flightDaysCost;
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
     * Process the smuggler encounter with all players in order
     * @param gameModel The current game state
     */
    public void processSmugglers(GameModel gameModel) {
        if (isDefeated) {
            return; // Smugglers already defeated
        }
        
        List<Player> playersInOrder = getPlayersInOrder(gameModel);
        
        for (Player player : playersInOrder) {
            // Calculate player's cannon strength
            int playerStrength = calculateCannonStrength(player);
            
            // Compare strengths
            if (playerStrength > smugglerStrength) {
                // Player defeats smugglers
                isDefeated = true;
                
                // Offer reward (player can choose whether to accept)
                // This would be implemented in the UI
                offerReward(player, gameModel, true); // Assuming player accepts reward
                
                break; // Smugglers are defeated, no more attacks
            } else if (playerStrength < smugglerStrength) {
                // Player is defeated by smugglers
                applyPenalty(player);
                
                // Continue to next player
            } else {
                // Tie - nothing happens, continue to next player
            }
        }
    }
    
    /**
     * Get players in order based on their position on the flight board
     * @param gameModel The current game state
     * @return Ordered list of players starting with the leader
     */
    private List<Player> getPlayersInOrder(GameModel gameModel) {
        // This would need to get players in order based on their position
        return new ArrayList<>(gameModel.getPlayers());
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
                strength += cannon.getFirePower();
                
                // Double cannons provide extra strength if powered
                //if (cannon.isDoubleCannon()) {
                 //   // Assuming player chooses to power double cannons when fighting smugglers
                //    strength += 1;
                //}
//                if (cannon.isDoubleCannon()) {
//                    // Assuming player chooses to power double cannons when fighting smugglers
//                    strength += 1;
//                }
            }
        }
        
        return strength;
    }
    
    /**
     * Offer the player a reward for defeating smugglers
     * @param player The player who defeated the smugglers
     * @param gameModel The current game state
     * @param acceptReward Whether the player accepts the reward
     */
    public void offerReward(Player player, GameModel gameModel, boolean acceptReward) {
        if (!isDefeated) {
            return; // Smugglers weren't defeated
        }
        
        if (acceptReward) {
            // Load goods on player's ship

            // Move player back on flight board
            //gameModel.getFlightBoard().updatePosition(player, -flightDaysCost);
        }
        // If player doesn't accept reward, nothing happens to their position
    }
    
    /**
     * Apply the penalty to a player defeated by smugglers
     * @param player The player who was defeated
     */
    private void applyPenalty(Player player) {
        // Take most valuable goods from player
        // If player has no goods, take batteries instead

        // placeholder implementation:
        int goodsCount = getGoodsCount(player);
        
        if (goodsCount > 0) {
            // Remove goods
        } else {
            // Remove batteries instead
        }
    }
    
    /**
     * Get the goods count for a player
     * @param player The player
     * @return Number of goods
     */
    private int getGoodsCount(Player player) {
        // This would need to count goods based on the game's implementation
        // For now, return a placeholder value
        return 2;
    }
    
    // Getters
    public int getSmugglerStrength() {
        return smugglerStrength;
    }
    
    public int getGoodsLostOnDefeat() {
        return goodsLostOnDefeat;
    }
    
    public int getGoodsGainedOnVictory() {
        return goodsGainedOnVictory;
    }
    
    public int getFlightDaysCost() {
        return flightDaysCost;
    }
    
    public boolean isDefeated() {
        return isDefeated;
    }
}