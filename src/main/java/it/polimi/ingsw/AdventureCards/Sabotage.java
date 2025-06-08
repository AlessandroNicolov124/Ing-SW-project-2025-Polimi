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
    protected int additionalPls() {
        return 0;
    }

    @Override
    protected void resolveEvent(Player p) {
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
        Player targetPlayer = findPlayerWithFewestCrew(gameModel.getFlightBoard().getPlayersInFlightOrder());
        
        if (targetPlayer == null) {
            return false; // No valid target
        }
        
        // Sabotage the target's ship
        return sabotageShip(targetPlayer);
    }

    /**
     * Find the player with the fewest crew figures
     * In case of ties, the player furthest ahead is returned
     * @param players List of players
     * @return The player with fewest crew
     */
    private Player findPlayerWithFewestCrew(List<Player> players) {
        if (players.isEmpty()) {
            return null;
        }

        // This would need to count crew figures for each player
        Player selected = players.stream().reduce((p1, p2) ->
                        p1.getShip().getTotalCrewCount() > p2.getShip().getTotalCrewCount() ? p2 : p1)
                .get();

        return selected;
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
                player.getShip().destroyComponentAt(targetPosition);
                return player.removeComponent(targetComponent);
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

}