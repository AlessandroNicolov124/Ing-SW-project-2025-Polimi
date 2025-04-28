package it.polimi.ingsw.AdventureCards;

import it.polimi.ingsw.Player;
import it.polimi.ingsw.GameModel;
import it.polimi.ingsw.Direction;
import it.polimi.ingsw.Components.*;
import java.util.*;

/**
 * Advanced Enemies: Pirates

 * On your flight, you may also encounter Pirates and Slavers. When you defeat them, you don't gain goods – you gain
 * cold, hard cosmic credits. Move your ship back the indicated number of empty spaces and take the credits from the
 * bank. If you don't want to lose flight days, you can choose not to take the credits (or goods in the case of
 * Smugglers) and leave your ship where it is. Either way, no other player may collect the reward once the enemy has
 * been defeated.

 * If the Pirates defeat you, your ship gets shot at. (The card indicates the size and direction of the cannon fire)
 * Keep track of all the players who were defeated and then have the first defeated player roll two dice to determine
 * the row or column of each shot. This roll applies to all defeated players.
 */

public class Pirates extends AdventureCard {
    private int pirateStrength;
    private int rewardCredits;
    private int flightDaysCost;
    private Direction cannonDirection;
    private boolean isDefeated;
    private Random random;

    public Pirates() {
        super();
        this.pirateStrength = 5; // Default pirate strength
        this.rewardCredits = 4;  // Default reward
        this.flightDaysCost = 2; // Default flight days cost
        this.cannonDirection = Direction.NORTH; // Default cannon direction
        this.isDefeated = false;
        this.random = new Random();
    }
    
    public Pirates(int copies, int duration, int pirateStrength, int rewardCredits, int flightDaysCost, Direction cannonDirection) {
        super(copies, duration);
        this.pirateStrength = pirateStrength;
        this.rewardCredits = rewardCredits;
        this.flightDaysCost = flightDaysCost;
        this.cannonDirection = cannonDirection;
        this.isDefeated = false;
        this.random = new Random();
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
     * Attempt to fight pirates
     * @param player The player trying to fight
     * @param gameModel The current game state
     * @return true if pirates were defeated, false if player was defeated
     */
    public boolean fightPirates(Player player, GameModel gameModel) {
        if (isDefeated) {
            return true; // Pirates already defeated
        }
        
        // Calculate player's cannon strength
        int playerStrength = calculateCannonStrength(player);
        
        // Check if player defeats pirates
        boolean playerWins = playerStrength >= pirateStrength;
        
        if (playerWins) {
            // Player wins, offer reward
            isDefeated = true;
            return true;
        } else {
            // Player is defeated, pirates will fire at their ship
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
                //    // Assuming player chooses to power double cannons when fighting pirates
                //    strength += 1;
                //}
            }
        }
        
        return strength;
    }
    
    /**
     * Offer the player a reward for defeating pirates
     * @param player The player who defeated the pirates
     * @param gameModel The current game state
     * @param acceptReward Whether the player accepts the reward
     */
    public void offerReward(Player player, GameModel gameModel, boolean acceptReward) {
        if (!isDefeated) {
            return; // Pirates weren't defeated
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
     * Fire at players who were defeated by pirates
     * @param defeatedPlayers List of players who were defeated
     * @return Number of components destroyed across all players
     */
    public int fireCannons(List<Player> defeatedPlayers) {
        if (defeatedPlayers.isEmpty()) {
            return 0;
        }
        
        int componentsDestroyed = 0;
        
        // Roll dice once for all players
        int diceRoll = rollDice();
        
        // Apply cannon fire to each defeated player
        for (Player player : defeatedPlayers) {
            if (fireCannonAt(player, diceRoll)) {
                componentsDestroyed++;
            }
        }
        
        return componentsDestroyed;
    }
    
    /**
     * Fire a cannon at a specific player's ship
     * @param player The player being fired at
     * @param position The position (row/column) to target
     * @return true if a component was destroyed, false otherwise
     */
    private boolean fireCannonAt(Player player, int position) {
        // Check if the shot hits the ship
        boolean hit = checkCannonHit(player, cannonDirection, position);
        
        if (hit) {
            // Destroy the component
            destroyComponent(player, cannonDirection, position);
            return true;
        }
        
        return false;
    }
    
    /**
     * Roll dice to determine the row or column of impact
     * @return Dice roll result (2-12)
     */
    private int rollDice() {
        return random.nextInt(6) + random.nextInt(6) + 2; // Simulating dice
    }
    
    /**
     * Check if cannon fire hits the player's ship
     * @param player The player whose ship may be hit
     * @param direction The direction the fire is coming from
     * @param position The position (row/column) of the potential impact
     * @return true if the fire hits the ship, false otherwise
     */
    private boolean checkCannonHit(Player player, Direction direction, int position) {
        // Implementation would depend on ship layout
        return true; // Placeholder for actual hit check
    }
    
    /**
     * Destroy a component on the player's ship
     * @param player The player whose ship is impacted
     * @param direction The direction the fire is coming from
     * @param position The position (row/column) of the impact
     */
    private void destroyComponent(Player player, Direction direction, int position) {
        // Implementation would remove component from player's ship
    }
    
    // Getters
    public int getPirateStrength() {
        return pirateStrength;
    }
    
    public int getRewardCredits() {
        return rewardCredits;
    }
    
    public int getFlightDaysCost() {
        return flightDaysCost;
    }
    
    public boolean isDefeated() {
        return isDefeated;
    }
}