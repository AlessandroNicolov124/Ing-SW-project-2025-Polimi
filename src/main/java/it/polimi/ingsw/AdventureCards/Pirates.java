package it.polimi.ingsw.AdventureCards;

import it.polimi.ingsw.Player;
import it.polimi.ingsw.GameModel;
import it.polimi.ingsw.Direction;
import it.polimi.ingsw.Components.*;
import it.polimi.ingsw.Position;

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
    private int rewardCredits;
    private int flightDaysCost;
    private int pirateStrength;

    private List<PiratesCannon> piratesCannons;
    private boolean isDefeated;
    private Random random;

    public Pirates() {
        super();
        this.pirateStrength = 5; // Default pirate strength
        this.rewardCredits = 4;  // Default reward
        this.flightDaysCost = 2; // Default flight days cost
        this.piratesCannons = createRandomPiratesCannon();
        this.isDefeated = false;
        this.random = new Random();
    }
    
    public Pirates(int copies, int duration, int pirateStrength, int rewardCredits, int flightDaysCost, List<PiratesCannon> pc) {
        super(copies, duration);
        this.pirateStrength = pirateStrength;
        this.rewardCredits = rewardCredits;
        this.flightDaysCost = flightDaysCost;
        this.piratesCannons = pc;
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

    @Override
    public String getDescription() {
        return "Encounter pirates! Fight with cannons (strength " + pirateStrength + ") or face cannon fire. " +
               "Victory rewards " + rewardCredits + " credits and " + flightDaysCost + " flight days.";
    }

    /**
     * Attempt to fight pirates
     * @param gameModel The current game state
     */
    public void fightPirates(GameModel gameModel) {
        List<Player> playersDedeated = new ArrayList<>();
        for(Player p : gameModel.getPlayers()) {
            if (!fightPiratesFor(p)){
                playersDedeated.add(p);
            }else{
                offerReward(p, gameModel, true);
            }
        }
        fireCannons(playersDedeated);
    }


        /**
         * Attempt to fight pirates
         * @param player The player trying to fight
         * @return true if pirates were defeated, false if player was defeated
         */
    public boolean fightPiratesFor(Player player) {
        if (isDefeated) {
            return true; // Pirates already defeated
        }

        activateDoubleCannons(player);

        // Calculate player's cannon strength
        double playerStrength = player.getShip().calculateFirePower();

        player.getShip().dischargeAllActiveDoubleCannon();

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
            gameModel.updatePosition(player, -flightDaysCost);
        }
        // If player doesn't accept reward, nothing happens to their position
    }
    
    /**
     * Fire at players who were defeated by pirates
     * @param defeatedPlayers List of players who were defeated
     */
    public void fireCannons(List<Player> defeatedPlayers) {
        if (defeatedPlayers.isEmpty()) {
            return;
        }

        for(PiratesCannon pc : piratesCannons) {
            // Roll dice once for all players
            int diceRoll = rollDice();

            // Apply cannon fire to each defeated player
            for (Player player : defeatedPlayers) {
                processPiratesCannonHitFor(player, pc, diceRoll);
            }
        }

    }

    private void processPiratesCannonHitFor(Player player, PiratesCannon pc, int diceRoll) {
        // Check if meteor hits the ship
        Position hit = checkCannonHit(player, pc.getDirection(), diceRoll);

        if (hit!=null) {
            // Process impact based on cannon size
            if (pc.getSize() == CannonSize.SMALL) {
                // Check for shield
                ShieldGenerator shieldUsed = canDefendWithShield(player, pc.getDirection());
                if (shieldUsed==null) {
                    // Destroy component
                    player.getShip().destroyComponentAt(hit);
                }else {
                    shieldUsed.discharge();
                }

            } else {
                player.getShip().destroyComponentAt(hit);
            }
        }
    }

    /**
     * Check if Pirates cannon hits the player's ship
     * @param player The player whose ship may be hit
     * @param targetCoord The position (row/column) of the potential impact
     * @return Position hit if the fire hits the ship
     */
    private Position checkCannonHit(Player player, Direction attack, int targetCoord) {
        // 4. Check if there's a component at those coordinates
        Component targetComponent = null;
        Position target = null;

        if(attack == Direction.SOUTH) {
            for(int i=player.getShip().getHeight()-1; i > 0; i--){
                target = new Position(i,targetCoord);
                targetComponent = player.getShip().getComponentAt(target);
                if(targetComponent != null) {
                    break;
                }
            }
        }else if(attack == Direction.NORTH) {
            for(int i=0; i < player.getShip().getHeight(); i++){
                target = new Position(i, targetCoord);
                targetComponent = player.getShip().getComponentAt(target);
                if(targetComponent != null) {
                    break;
                }
            }
        }else if(attack == Direction.WEST) {
            for(int i=0; i < player.getShip().getWidth(); i++){
                target = new Position(targetCoord, i);
                targetComponent = player.getShip().getComponentAt(target);
                if(targetComponent != null) {
                    break;
                }
            }
        }else if(attack == Direction.EAST) {
            for(int i=player.getShip().getWidth()-1; i > 0; i--){
                target = new Position(targetCoord,i);
                targetComponent = player.getShip().getComponentAt(target);
                if(targetComponent != null) {
                    break;
                }
            }
        }

        if (targetComponent == null) {
            System.out.println("The cannon fire targets an empty space!");
            return null;
        }

        return target;
    }

    /**
     * Check if player can and wants to defend with a shield
     * @param player The player attempting to defend
     * @param direction The direction of the attack
     * @return ShieldGenerator activated if defense is successful
     */
    private ShieldGenerator canDefendWithShield(Player player, Direction direction) {
        // Check if player has a shield that can protect against this direction and position
        List<ShieldGenerator> shields = player.getShip().getShieldsProtecting(direction);

        if (!shields.isEmpty() && player.getShip().calculateTotalBatteryCharge() > 0) {
            // Player has shield(s) and batteries - ask if they want to defend

            // TODO UI scelta del giocatore su uso delle batterie
            ShieldGenerator count = null;
            for(ShieldGenerator shield : shields){
                if(player.getShip().activateShield(shield)){
                    count = shield;
                    break;
                }
            }
            return count;
        }

        return null;
    }
    
    /**
     * Roll dice to determine the row or column of impact
     * @return Dice roll result (2-12)
     */
    private int rollDice() {
        return random.nextInt(6) + random.nextInt(6) + 2; // Simulating dice
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

    /**
     * Inner class to represent a meteor
     */
    public static class PiratesCannon {
        private CannonSize size;
        private Direction direction;

        public PiratesCannon(CannonSize size, Direction direction) {
            this.size = size;
            this.direction = direction;
        }

        public CannonSize getSize() {
            return size;
        }

        public Direction getDirection() {
            return direction;
        }
    }

    /**
     * Enum to represent meteor sizes
     */
    public enum CannonSize {
        SMALL,
        LARGE
    }

    /**
     * Create default set of PiratesCannon
     * @return List of PiratesCannon
     */
    private List<PiratesCannon> createRandomPiratesCannon() {
        List<PiratesCannon> defaultPC = new ArrayList<>();

        Random random = new Random();
        int numberOfMeteors = random.nextInt(5) + 1; // Random number between 1 and 5 meteors

        for (int i = 0; i < numberOfMeteors; i++) {
            // Randomly select a size
            CannonSize size = CannonSize.values()[random.nextInt(CannonSize.values().length)];
            // Randomly select a direction
            Direction direction = Direction.values()[random.nextInt(Direction.values().length)];
            // Add the meteor with random properties
            defaultPC.add(new PiratesCannon(size, direction));
        }
        return defaultPC;
    }
}