package it.polimi.ingsw.AdventureCards;

import it.polimi.ingsw.CrewType;
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
    protected int additionalPls() {
        return 0;
    }

    @Override
    protected void resolveEvent(Player p) {
        // Individual player resolution would depend on the game mechanics
    }

    @Override
    public void effect(GameModel gameModel) {
        fightSlavers(gameModel);
    }

    /**
     * Attempt to fight slavers
     * @param gameModel The current game state
     */
    public void fightSlavers(GameModel gameModel) {
        List<Player> playersDedeated = new ArrayList<>();
        for(Player p : gameModel.getFlightBoard().getPlayersInFlightOrder()) {
            if (!fightSlaversFor(p)){
                playersDedeated.add(p);
            }else{
                // TODO This would be implemented in the UI
                offerReward(p, gameModel, true);
                break; // Only the first player to defeat pirates gets the reward if he chooses to accept it
            }
        }
        surrenderCrew(playersDedeated);
    }
    
    /**
     * Attempt to fight slavers
     * @param player The player trying to fight
     * @return true if slavers were defeated, false if player was defeated
     */
    public boolean fightSlaversFor(Player player) {
        if (isDefeated) {
            return true; // Smugglers already defeated
        }

        activateDoubleCannons(player);

        // Calculate player's cannon strength
        double playerStrength = player.getShip().calculateFirePower();

        player.getShip().dischargeAllActiveDoubleCannon();

        // Check if player defeats slavers
        if (playerStrength >= slaverStrength) {
            // Player wins, offer reward
            System.out.println("Player " + player.getNickName() + " defeated the slavers!");
            isDefeated = true;
            return true;
        } else {
            // Player is defeated, they must surrender crew
            System.out.println("Player " + player.getNickName() + " was defeated by the slavers!");
            return false;
        }
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
            
            // Move player back on flight board (positive flight day cost = move to worse position)
            gameModel.updatePosition(player, flightDaysCost);
            
            System.out.println("[Slavers] " + player.getNickName() + " paid the slavers: +" + rewardCredits + 
                             " credits, lost " + flightDaysCost + " flight days");
        }
        // If player doesn't accept reward, nothing happens to their position
    }
    
    /**
     * Force a defeated player to surrender crew
     * @param defeatedPlayers List of player who was defeated
     */
    public void surrenderCrew(List<Player> defeatedPlayers) {
        // This implements the mechanism for player to choose which crew to surrender

        if (defeatedPlayers.isEmpty()) {
            return;
        }

        for (Player player : defeatedPlayers) {
            // Verify player has crew to surrender
            int playerCrew = player.getShip().getTotalCrewCount();

            //TODO scelta del giocatore UI
            player.getShip().removeCrewMembers(Math.min(playerCrew, crewLoss));

            //            List<Cabin> cabinWithCrew = player.getShip().getAllCabins()
            //                    .stream().filter(c -> !c.getCrew().isEmpty()).toList();
            //            int count = 0;
            //            while(count < crewLoss) {
            //
            //                for(Cabin c : cabinWithCrew) {
            //                    c.getCrew();
            //                    count++;
            //                }
            //            }
        }


    }

    /**
     * Let player decide which double cannons to activate
     * @param player The player making decisions
     */
    private void activateDoubleCannons(Player player) {
        List<DoubleCannon> doubleCannons = player.getShip().getOperationalDoubleCannons();

        for (DoubleCannon cannon : doubleCannons) {
            // Check if player has batteries and wants to activate this cannon
            if (player.getShip().calculateTotalBatteryCharge() > 0) {
                // TODO UI scelta del giocatore su uso delle batterie
                // ipotizzo lo voglia fare
                player.getShip().chargeWeapon(cannon);
            }
        }
    }

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
