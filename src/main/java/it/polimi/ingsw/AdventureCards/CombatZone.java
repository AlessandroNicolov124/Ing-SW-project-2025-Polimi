package it.polimi.ingsw.AdventureCards;

import it.polimi.ingsw.*;
import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.Components.DoubleCannon;
import it.polimi.ingsw.Components.DoubleEngine;
import it.polimi.ingsw.Components.ShieldGenerator;
import javafx.geometry.Pos;
import javafx.scene.control.skin.TextInputControlSkin;

import java.util.*;

/**
 * Combat Zone

 * The true test of any spaceship is to fly it through a combat zone. The Combat Zone card has 3 lines which are
 * evaluated in succession. Each line gives a criterion and a penalty for the player who is weakest in that area.

 * First, the player with the fewest crew figures loses 3 flight days.

 * Next, the player with the weakest engines loses 2 crew members. Players count up their engine strength in order,
 * starting with the leader, deciding whether or not to spend battery tokens on double engines.

 * Finally, the player with the weakest cannons is threatened by light cannon fire from behind and heavy cannon fire
 * from behind. Again, players decide in order, starting with the leader, which double cannons they will pay for.

 * Hits from cannon fire work like hits from meteors except they are more difficult to defend against. Each hit has a
 * direction. The player rolls two dice to determine which row or column the hit is coming from, thus determining which
 * component of the ship (if any) is in danger.

 * The only way to defend against light cannon fire is with a shield that protects against hits from that direction.
 * This can be powered at the cost of 1 battery token. Otherwise, the component is destroyed.

 * There is no way to defend against heavy cannon fire. Your only hope is to roll high enough or low enough that it
 * misses your ship entirely. Otherwise, the component it hits is destroyed.

 * In each criterion – smallest crew, weakest engines, weakest cannons – it is possible for players to be tied. Among
 * tied players, the one farthest ahead is the only one who must face the penalty. (Usually, being ahead is good; in a
 * combat zone, it's dangerous.)
 */

public class CombatZone extends AdventureCard {
    private Random random;
    private int flightDayPenalty;
    private int crewLossPenalty;
    private static final Direction ATTACK_DIRECTION = Direction.SOUTH; // Attacks come from behind


    public CombatZone(int copies, int duration, int flightDayPenalty, int crewLossPenalty) {
        super(copies, duration);
        this.random = new Random();
        this.flightDayPenalty = flightDayPenalty;
        this.crewLossPenalty = crewLossPenalty;
    }

    // TODO: deve essere implementata una seconda carta CombactZone le cui tre linee sono diverse

    /**
     * Default constructor with standard values
     */
    public CombatZone() {
        super(1, 1);
        this.random = new Random();
        this.flightDayPenalty = 3;  // Default: lose 3 flight days
        this.crewLossPenalty = 2;   // Default: lose 2 crew members
    }

    @Override
    protected int additionalPls(){
        return 0;
    }

    @Override
    protected void resolveEvent(Player p) {
        // Individual player resolution would depend on the game mechanics
    }

    @Override
    public void effect(GameModel gameModel){
        applyCombatZone(gameModel);
    }
    
    /**
     * Apply combat zone effects to all players
     * @param gameModel The current game state
     */
    public void applyCombatZone(GameModel gameModel) {
        List<Player> players = gameModel.getPlayers();

        // 1. Evaluate crew phase: player with fewest crew loses flight days
        evaluateCrewPhase(players, gameModel);

        // 2. Evaluate engine phase: player with weakest engines loses crew
        evaluateEnginePhase(players, gameModel);

        // 3. Evaluate firepower phase: player with weakest cannons faces cannon fire
        evaluateFirepowerPhase(players, gameModel);
    }

    /**
     * Phase 1: Evaluate crew counts and apply penalty to player with fewest crew
     * @param players List of players
     * @param gameModel The game model
     */
    private void evaluateCrewPhase(List<Player> players, GameModel gameModel) {
        if (players.isEmpty()) {
            return;
        }
        // Find player with fewest crew
        Player weakestCrew = findPlayerWithFewestCrew(players);
        if (weakestCrew != null) {
            // Apply flight day penalty (positive value = move to worse position)
            gameModel.updatePosition(weakestCrew, flightDayPenalty);
            
            System.out.println("[CombatZone] " + weakestCrew.getNickName() + " had fewest crew, lost " + 
                             flightDayPenalty + " flight days");
        }
    }

    /**
     * Phase 2: Evaluate engine strength and apply penalty to player with weakest engines
     * @param players List of players
     * @param gameModel The game model
     */
    private void evaluateEnginePhase(List<Player> players, GameModel gameModel) {
        if (players.isEmpty()) {
            return;
        }

        // Players decide on engine power in order of route position
        List<Player> sortedPlayers = gameModel.getFlightBoard().getPlayersInFlightOrder();

        // Each player decides which double engines to power
        for (Player player : sortedPlayers) {
            activateDoubleEngines(player);
        }

        // Find player with weakest engines
        Player weakestEngines = findPlayerWithWeakestEngines(players);
        if (weakestEngines != null) {
            // Apply crew loss penalty
            removeCrewMembers(weakestEngines, crewLossPenalty);
        }
        //dopo averli usati devono essere scaricati
        for (Player player : sortedPlayers) {
            player.getShip().dischargeAllActiveDoubleEngine();
        }
    }

    /**
     * Phase 3: Evaluate cannon strength and apply penalty to player with weakest cannons
     * @param players List of players
     * @param gameModel The game model
     */
    private void evaluateFirepowerPhase(List<Player> players, GameModel gameModel) {
        if (players.isEmpty()) {
            return;
        }

        // Players decide on cannon power in order of route position
        List<Player> sortedPlayers = gameModel.getFlightBoard().getPlayersInFlightOrder();

        // Each player decides which double cannons to power
        for (Player player : sortedPlayers) {
            activateDoubleCannons(player);
        }

        // Find player with weakest cannons
        Player weakestCannons = findPlayerWithWeakestCannons(players);
        if (weakestCannons != null) {
            System.out.println("The player with the weakest cannons is: " + weakestCannons.getNickName());

            // Apply light cannon fire
            boolean lightHit = applyLightCannonFire(weakestCannons);
            // Apply heavy cannon fire
            boolean heavyHit = applyHeavyCannonFire(weakestCannons);

            if (!lightHit && !heavyHit) {
                System.out.println(weakestCannons.getNickName() + " got lucky! Both cannon shots missed.");
            }else if(lightHit) {
                System.out.println(weakestCannons.getNickName() + " was hit by light cannon fire!");
            }else if(heavyHit) {
                System.out.println(weakestCannons.getNickName() + " was hit by heavy cannon fire!");
            }
        }

        //scarico i cannoni
        for (Player player : sortedPlayers) {
            player.getShip().dischargeAllActiveDoubleCannon();
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
     * Remove crew members from a player's ship
     * @param player The player losing crew
     * @param count Number of crew members to remove
     */
    private void removeCrewMembers(Player player, int count) {
        player.getShip().removeCrewMembers(Math.min(player.getShip().getTotalCrewCount(), count));
        //        ShipBuilder ship = player.getShip();
//        int remainingToRemove = count;
//
//        // First try to remove aliens
//        int aliensToRemove = Math.min(ship.getAlienCount(), remainingToRemove);
//        if (aliensToRemove > 0) {
//            remainingToRemove -= ship.removeAliens(aliensToRemove);
//        }
//
//        // Then remove humans if needed
//        if (remainingToRemove > 0) {
//            ship.removeHumans(Math.min(ship.getHumanCount(), remainingToRemove));
//        }
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
     * Find the player with the weakest engines
     * In case of ties, the player furthest ahead is returned
     * @param players List of players
     * @return The player with weakest engines
     */
    private Player findPlayerWithWeakestEngines(List<Player> players) {
        if (players.isEmpty()) {
            return null;
        }

        Player selected = players.stream().reduce((p1, p2) ->
                        p1.getShip().calculateEnginePower() > p2.getShip().calculateEnginePower() ? p2 : p1)
                        .get();

        return selected;
    }

    /**
     * Find the player with the weakest cannons
     * In case of ties, the player furthest ahead is returned
     * @param players List of players
     * @return The player with weakest cannons
     */
    protected Player findPlayerWithWeakestCannons(List<Player> players) {
        if (players.isEmpty()) {
            return null;
        }

        Player selected = players.stream().reduce((p1, p2) ->
                        p1.getShip().calculateFirePower() > p2.getShip().calculateFirePower() ? p2 : p1)
                .get();

        return selected;
    }
    
    /**
     * Apply light cannon fire from behind to a player
     * @param player The player being attacked
     * @return true if component was destroyed, false otherwise
     */
    protected boolean applyLightCannonFire(Player player) {
        // 1. Roll dice to determine row/column of hit
        int diceRoll = rollDice();

        // 2. Check if hit lands on ship
        Position hit = checkCannonHit(player, ATTACK_DIRECTION, diceRoll);
        
        if (hit!=null) {
            //System.out.println("Light cannon fire target component at position " + hit.toString());

            // 3. Check if player can defend with shield
            ShieldGenerator defended = canDefendWithShield(player, ATTACK_DIRECTION);
            if (defended==null) {

                //System.out.println("Light cannon fire hit component at position " + hit.toString());
                // 4. Destroy component if no defense
                player.getShip().destroyComponentAt(hit);
                return true;
            }
            System.out.println(player.getNickName() + "Defends with a shield!");
            //dopo averlo usato scarico lo scudo
            defended.discharge();

        }
        return false;
    }
    
    /**
     * Apply heavy cannon fire from behind to a player
     * @param player The player being attacked
     * @return true if component was destroyed, false otherwise
     */
    private boolean applyHeavyCannonFire(Player player) {
        // 1. Roll dice to determine row/column of hit
        int diceRoll = rollDice();

        // 2. Check if hit lands on ship
        Position hit = checkCannonHit(player, ATTACK_DIRECTION, diceRoll);
        
        if (hit!=null) {
            System.out.println("Heavy cannon fire hit component at position " + hit.toString());
            // No defense against heavy cannon fire
            player.getShip().destroyComponentAt(hit);
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
     * @param targetCoord The position (row/column) of the potential impact
     * @return Position hit if the fire hits the ship
     */
    protected Position checkCannonHit(Player player, Direction attack, int targetCoord) {
        // 4. Check if there's a component at those coordinates
        Component targetComponent = null;
        Position target = null;

        if(attack == Direction.SOUTH) {
            for(int i=player.getShip().getHeight()-1; i > 0; i--){
                target = new Position(targetCoord, i);
                targetComponent = player.getShip().getComponentAt(target);
                if(targetComponent != null) {
                    break;
                }
            }
        }else if(attack == Direction.NORTH) {
            for(int i=0; i < player.getShip().getHeight(); i++){
                target = new Position(targetCoord, i);
                targetComponent = player.getShip().getComponentAt(target);
                if(targetComponent != null) {
                    break;
                }
            }
        }else if(attack == Direction.WEST) {
            for(int i=0; i < player.getShip().getWidth(); i++){
                target = new Position(i, targetCoord);
                targetComponent = player.getShip().getComponentAt(target);
                if(targetComponent != null) {
                    break;
                }
            }
        }else if(attack == Direction.EAST) {
            for(int i=player.getShip().getWidth()-1; i > 0; i--){
                target = new Position(i, targetCoord);
                targetComponent = player.getShip().getComponentAt(target);
                if(targetComponent != null) {
                    break;
                }
            }
        }

        if (targetComponent == null) {
            System.out.println("The cannon fire targets an empty space! ");
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
    protected ShieldGenerator canDefendWithShield(Player player, Direction direction) {
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
}
