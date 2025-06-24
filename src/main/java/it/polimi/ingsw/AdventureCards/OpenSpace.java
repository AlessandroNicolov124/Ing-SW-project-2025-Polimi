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
    protected int additionalPls() {
        return 0;
    }

    @Override
    protected void resolveEvent(Player p) {
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
            activateDoubleEngines(player);

            double engineStrength = calculateEngineStrength(player);
            
            // Move player forward on flight board
            gameModel.updatePosition(player, (int) engineStrength);

            //una volta usati li scarico
            player.getShip().dischargeAllActiveDoubleEngine();
        }

    }

    /**
     * Let player decide if activate double engines
     * @param player The player making decisions
     */
    private void activateDoubleEngines(Player player) {
        List<DoubleEngine> doubleEngines = player.getShip().getOperationalDoubleEngines();

        //for (DoubleEngine engine : doubleEngines) {
        // Check if player has batteries and wants to activate this engine
        if (player.getShip().calculateTotalBatteryCharge() > 0) {
            // TODO UI scelta del giocatore su uso delle batterie
            // ipotizzo lo voglia fare
            player.getShip().chargeEngine();
            //player.getShip().chargeEngine(engine);
        }
        //}
    }
    
    /**
     * Calculate the engine strength of a player's ship
     * @param player The player
     * @return Total engine strength
     */
    private int calculateEngineStrength(Player player) {
        int strength = player.getShip().getComponents().keySet().stream()
                .mapToInt(component -> switch(component){
                    case Engine c -> c.getEffectivePower();
                    default -> 0;
                })
                .sum();
        
        return strength;
    }
}