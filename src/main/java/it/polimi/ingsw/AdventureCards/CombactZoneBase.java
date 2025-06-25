package it.polimi.ingsw.AdventureCards;

import it.polimi.ingsw.*;
import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.Components.DoubleCannon;
import it.polimi.ingsw.Components.DoubleEngine;
import it.polimi.ingsw.Components.ShieldGenerator;
import java.util.*;

public abstract class CombactZoneBase extends AdventureCard {
    protected final Random random = new Random();

    public CombactZoneBase() { super(1, 1); }
    public CombactZoneBase(int copies, int duration) { super(copies, duration); }

    public abstract void applyCombatZone(GameModel gameModel);

    /**
     * Apply heavy cannon fire from behind to a player
     * @param player The player being attacked
     * @param ATTACK_DIRECTION The direction of the attack
     * @return true if component was destroyed, false otherwise
     */
    protected boolean applyHeavyCannonFire(Player player, Direction ATTACK_DIRECTION) {
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
     * Apply light cannon fire from behind to a player
     * @param player The player being attacked
     * @param ATTACK_DIRECTION The direction of the attack
     * @return true if component was destroyed, false otherwise
     */
    protected boolean applyLightCannonFire(Player player, Direction ATTACK_DIRECTION) {
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
     * Roll two dice and return the sum (2-12)
     * @return The result of the dice roll
     */
    protected int rollDice() {
        return random.nextInt(6) + random.nextInt(6) + 2;
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

    /**
     * Let player decide which double cannons to activate
     * @param player The player making decisions
     */
    protected void activateDoubleCannons(Player player) {
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
    protected void activateDoubleEngines(Player player) {
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
     * Find the player with the fewest crew figures
     * In case of ties, the player furthest ahead is returned
     * @param players List of players
     * @return The player with fewest crew
     */
    protected Player findPlayerWithFewestCrew(List<Player> players) {
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
    protected Player findPlayerWithWeakestEngines(List<Player> players) {
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
}
