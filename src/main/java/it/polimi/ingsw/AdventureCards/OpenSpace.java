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
        List<Player> playersInOrder = gameModel.getFlightBoard().getPlayersInFlightOrder();
        
        for (Player player : playersInOrder) {
            // Calculate engine strength for player

            // UI il giocatore potrebbe decidere di non usare le batterie
            double engineStrength = calculateEngineStrength(player, true);
            
            // Move player forward on flight board
            gameModel.updatePosition(player, (int) engineStrength);
        }
    }
    
    /**
     * Calculate the engine strength of a player's ship
     * @param player The player
     * @param useBatteries Whether the player chooses to use batteries for double engines
     * @return Total engine strength
     */
    private double calculateEngineStrength(Player player, boolean useBatteries) {
        double strength = player.getShip().getComponents().keySet().stream()
                .mapToDouble(component -> switch(component){
                    case SingleCannon sc -> sc.getEffectivePower();
                    case DoubleCannon dc -> dc.calculatePowerUsingBattery(useBatteries);
                    default -> 0.0;
                })
                .sum();
        
        return strength;
    }
}