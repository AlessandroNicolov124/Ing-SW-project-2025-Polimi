package it.polimi.ingsw.AdventureCards;

import it.polimi.ingsw.Player;
import it.polimi.ingsw.GameModel;
import it.polimi.ingsw.Components.*;
import java.util.*;

/**
 * Open Space

 * Open Space is like a wide-open drag strip. Each player will have a chance to gain flight days.

 * In turn, each player declares their engine strength, beginning with the leader, and continuing in the order shown by
 * the rockets on the flight board.
 * You must decide whether to spend battery tokens on any double engines when it is your turn to declare engine strength.
 * Then you immediately move your rocket marker that many empty spaces forward. This may allow you to pass
 * players ahead of you (occupied spaces are skipped) and perhaps even take the lead.
 */

public class OpenSpace extends AdventureCard {

    public OpenSpace() {
        super();
    }
    
    public OpenSpace(int copies, int duration) {
        super(copies, duration);
    }

    @Override
    int additionalPls() {
        return 0;
    }

    @Override
    void resolveEvent(Player p) {
        // Individual player resolution
    }

    @Override
    public void effect() {
        // This would be called when the card is drawn
    }
    
    /**
     * Apply the Open Space effect to all players in order
     * @param gameModel The current game state
     */
    public void applyOpenSpace(GameModel gameModel) {
        List<Player> playersInOrder = getPlayersInOrder(gameModel);
        
        for (Player player : playersInOrder) {
            // Calculate engine strength for player
            int engineStrength = calculateEngineStrength(player, true);
            
            // Move player forward on flight board
            movePlayerForward(player, engineStrength, gameModel);
        }
    }
    
    /**
     * Get players in order based on their position on the flight board
     * @param gameModel The current game state
     * @return Ordered list of players starting with the leader
     */
    private List<Player> getPlayersInOrder(GameModel gameModel) {
        // This would need to get players in order based on their position
        // For now, return a copy of the players list
        return new ArrayList<>(gameModel.getPlayers());
    }
    
    /**
     * Calculate the engine strength of a player's ship
     * @param player The player
     * @param useBatteries Whether the player chooses to use batteries for double engines
     * @return Total engine strength
     */
    private int calculateEngineStrength(Player player, boolean useBatteries) {
        int strength = 0;
        List<Component> components = player.getShip().getAllComponents();
        
        // Find all engines and count their strength
        for (Component component : components) {
            if (component instanceof Engine engine) {

                // If it's a double engine and player wants to use batteries
                if (engine.isDoubleEngine() && useBatteries) {
                    // This would consume a battery token
                    strength += 2; // Double strength
                } else {
                    strength += 1; // Normal strength
                }
            }
        }
        
        return strength;
    }
    
    /**
     * Move a player forward on the flight board
     * @param player The player to move
     * @param spaces Number of empty spaces to move forward
     * @param gameModel The current game state
     */
    private void movePlayerForward(Player player, int spaces, GameModel gameModel) {
        // This would need to handle the logic of moving forward
        // and skipping occupied spaces
        
        // For now, just update the position
        gameModel.getFlightBoard().updatePosition(player, spaces);
    }
}