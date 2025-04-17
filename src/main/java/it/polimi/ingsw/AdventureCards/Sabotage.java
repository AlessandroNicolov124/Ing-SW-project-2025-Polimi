package it.polimi.ingsw.AdventureCards;

import it.polimi.ingsw.Player;
import it.polimi.ingsw.GameModel;
import it.polimi.ingsw.ShipBuilder;
import it.polimi.ingsw.Position;
import it.polimi.ingsw.Components.*;
import java.util.*;

/**
 * Sabotage

 * Sabotage destroys a random component on the ship with the smallest crew. (Among multiple ships tied for smallest
 * crew, only the one farthest ahead is sabotaged.) To select the component, the affected player rolls 2 dice to
 * determine the column and then 2 dice to determine the row. If there is no component at those coordinates, roll again
 * for new coordinates. If there is no component there, roll again. If three sets of coordinates are rolled without
 * hitting a component, the saboteurs give up and nothing happens.

 * If the saboteurs destroy a component, it goes to your discard pile, as do any others that fall off as a result.
 * A hit to the center of the ship can be devastating.
 */

public class Sabotage extends AdventureCard {
    private Random random;
    private int maxAttempts;

    public Sabotage() {
        super();
        this.random = new Random();
        this.maxAttempts = 3; // Maximum number of attempts to hit a component
    }
    
    public Sabotage(int copies, int duration, int maxAttempts) {
        super(copies, duration);
        this.random = new Random();
        this.maxAttempts = maxAttempts;
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
     * Apply sabotage to the appropriate player
     * @param gameModel The current game state
     * @return true if sabotage was successful, false otherwise
     */
    public boolean applySabotage(GameModel gameModel) {
        // Find player with the smallest crew
        Player targetPlayer = findPlayerWithSmallestCrew(gameModel);
        
        if (targetPlayer == null) {
            return false; // No valid target
        }
        
        // Sabotage the target's ship
        return sabotageShip(targetPlayer);
    }
    
    /**
     * Find the player with the smallest crew count
     * If tied, return the player furthest ahead
     * @param gameModel The current game state
     * @return The player with the smallest crew
     */
    private Player findPlayerWithSmallestCrew(GameModel gameModel) {
        List<Player> players = gameModel.getPlayers();
        
        if (players.isEmpty()) {
            return null;
        }
        
        Player smallestCrewPlayer = players.get(0);
        int smallestCrew = getCrewCount(smallestCrewPlayer);
        
        for (int i = 1; i < players.size(); i++) {
            Player currentPlayer = players.get(i);
            int currentCrew = getCrewCount(currentPlayer);
            
            if (currentCrew < smallestCrew) {
                smallestCrewPlayer = currentPlayer;
                smallestCrew = currentCrew;
            } else if (currentCrew == smallestCrew) {
                // If tied, check who is furthest ahead on the flight board
                if (isPlayerAheadOf(currentPlayer, smallestCrewPlayer, gameModel)) {
                    smallestCrewPlayer = currentPlayer;
                }
            }
        }
        
        return smallestCrewPlayer;
    }
    
    /**
     * Get the crew count for a player
     * @param player The player
     * @return Number of crew members
     */
    private int getCrewCount(Player player) {
        return 3; //placeholder
    }
    
    /**
     * Check if player1 is ahead of player2 on the flight board
     * @param player1 First player
     * @param player2 Second player
     * @param gameModel The game model
     * @return true if player1 is ahead of player2
     */
    private boolean isPlayerAheadOf(Player player1, Player player2, GameModel gameModel) {
        // This would compare player positions on the flight board
        return true;
    }
    
    /**
     * Attempt to sabotage a player's ship
     * @param player The target player
     * @return true if sabotage was successful, false otherwise
     */
    private boolean sabotageShip(Player player) {
        ShipBuilder ship = player.getShip();
        
        // Try to hit a component up to maxAttempts times
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            // Roll for coordinates
            int row = rollDice();
            int column = rollDice();
            
            Position targetPosition = new Position(column, row);
            
            // Check if there's a component at these coordinates
            Component targetComponent = ship.getComponentAt(targetPosition);
            
            if (targetComponent != null) {
                return destroyComponent(player, targetComponent, ship);
            }
            
            // If missed, retry
        }
        
        // All attempts failed, saboteurs give up
        return false;
    }
    
    /**
     * Roll dice to determine a coordinate
     * @return Dice roll result (2-12)
     */
    private int rollDice() {
        return random.nextInt(6) + random.nextInt(6) + 2; // Simulating dice
    }
    
    /**
     * Destroy a component and handle any cascading destruction
     * @param player The target player
     * @param component The component to destroy
     * @param ship The player's ship
     * @return true if component was destroyed, false otherwise
     */
    private boolean destroyComponent(Player player, Component component, ShipBuilder ship) {
        // Remove the component from the ship
        boolean removed = ship.removeComponent(component);
        
        if (removed) {
            // Check for components that would fall off as a result
            // handled by the ShipBuilder.removeComponent method
            // If the ship is disconnected, additional components would be removed
            
            // Add to player's discard pile
            return true;
        }
        
        return false;
    }
}