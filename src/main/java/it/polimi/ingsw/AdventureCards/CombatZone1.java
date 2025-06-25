package it.polimi.ingsw.AdventureCards;

import it.polimi.ingsw.Components.*;
import it.polimi.ingsw.Direction;
import it.polimi.ingsw.GameModel;
import it.polimi.ingsw.Player;
import it.polimi.ingsw.Position;

import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Applica la variante di Combat Zone:
 * 1. Il giocatore con meno potenza di fuoco perde 4 giorni di volo.
 * 2. Il giocatore con meno forza motrice perde 3 cargo.
 * 3. Il giocatore con meno equipaggio subisce cannonate leggere da nord, est, ovest e una pesante da sud.
 * In caso di parità, la penalità va al primo in ordine di rotta.
 */
public class CombatZone1 extends AdventureCard {
    private Random random;
    private int flightDayPenalty;
    private int goodsLostPenalty;

    public CombatZone1(int copies, int duration, int flightDayPenalty, int goodsLostPenalty) {
        super(copies, duration);
        this.random = new Random();
        this.flightDayPenalty = flightDayPenalty;
        this.goodsLostPenalty = goodsLostPenalty;
    }

    /**
     * Default constructor with standard values
     */
    public CombatZone1() {
        super(1, 1);
        this.random = new Random();
        this.flightDayPenalty = 4;  // Default: lose 4 flight days
        this.goodsLostPenalty = 3;   // Default: lose 3 cargo
    }

    /**
     * Apply combat zone effects to all players
     * @param gameModel The current game state
     */
    public void applyCombatZone(GameModel gameModel) {
        List<Player> players = gameModel.getPlayers();

        // 1. Evaluate firepower phase: player with weakest cannons loses flight days
        evaluateFirepowerPhase(players, gameModel);

        // 2. Evaluate engine phase: player with weakest engines loses cargo
        evaluateEnginePhase(players, gameModel);

        // 3. Evaluate crew phase: player with fewest crew faces cannon fire
        evaluateCrewPhase(players, gameModel);

    }

    /**
     * Phase 1: Evaluate cannon strength and apply penalty to player with weakest cannons
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
            gameModel.updatePosition(weakestCannons, -flightDayPenalty);
        }

        //scarico i cannoni
        for (Player player : sortedPlayers) {
            player.getShip().dischargeAllActiveDoubleCannon();
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
            System.out.println("The player with the weakest engine is: " + weakestEngines.getNickName());

            // Apply crew loss penalty
            removeCargo(weakestEngines);
        }
        //dopo averli usati devono essere scaricati
        for (Player player : sortedPlayers) {
            player.getShip().dischargeAllActiveDoubleEngine();
        }
    }

    /**
     * Phase 3: Evaluate crew counts and apply penalty to player with fewest crew
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
            System.out.println("The player with the weakest crew is: " + weakestCrew.getNickName());

            // Apply light cannon fire
            boolean lightHitN = applyLightCannonFire(weakestCrew, Direction.NORTH);
            boolean lightHitE = applyLightCannonFire(weakestCrew, Direction.EAST);
            boolean lightHitW = applyLightCannonFire(weakestCrew, Direction.WEST);
            // Apply heavy cannon fire
            boolean heavyHit = applyHeavyCannonFire(weakestCrew, Direction.SOUTH);

            if (!lightHitN && !lightHitE && !lightHitW && !heavyHit) {
                System.out.println(weakestCrew.getNickName() + " got lucky! Both cannon shots missed.");
            }else if(lightHitN) {
                System.out.println(weakestCrew.getNickName() + " was hit by light cannon fire from NORTH!");
            }else if(lightHitE) {
                System.out.println(weakestCrew.getNickName() + " was hit by light cannon fire from EAST!");
            }else if(lightHitW) {
                System.out.println(weakestCrew.getNickName() + " was hit by light cannon fire from WEST!");
            }else if(heavyHit) {
                System.out.println(weakestCrew.getNickName() + " was hit by heavy cannon fire!");
            }
        }
    }

    /**
     * Apply heavy cannon fire from behind to a player
     * @param player The player being attacked
     * @param ATTACK_DIRECTION The direction of the attack
     * @return true if component was destroyed, false otherwise
     */
    private boolean applyHeavyCannonFire(Player player, Direction ATTACK_DIRECTION) {
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
     * Remove cargo from a player's ship
     * @param player The player losing crew
     */
    private void removeCargo(Player player) {
        List<Cargo> allCargo = player.getShip().getCargoList();
        // Ordina le merci per valore decrescente
        allCargo.sort(Comparator.comparing(Cargo::getValue).reversed());

        if(allCargo.isEmpty()) {
            return;
        }

        int removedCount = 0;

        // Se il giocatore ha merci, rimuovi le più preziose
        for (int i = 0; i < allCargo.size() && removedCount < goodsLostPenalty; i++) {
            Cargo cargo = allCargo.get(i);

            // Trova la stiva che contiene questa merce
            for (CargoHolds cargoHold : player.getShip().getCargoHolds()) {
                if (cargoHold.getCargos().contains(cargo)) {
                    cargoHold.unloadCargo(cargo);
                    removedCount++;
                    break;
                }
            }
        }
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
     * Roll dice to determine the row or column of impact
     * @return Dice roll result (2-12)
     */
    private int rollDice() {
        return random.nextInt(6) + random.nextInt(6) + 2;
    }

    /**
     * Check if cannon fire hits the player's ship
     * @param player The player whose ship may be hit
     * @param targetCoord The position (row/column) of the potential impact
     * @return Position hit if the fire hits the ship
     */
    private Position checkCannonHit(Player player, Direction attack, int targetCoord) {
        int width = player.getShip().getWidth();
        int height = player.getShip().getHeight();
        Position target = null;
        if (attack == Direction.SOUTH) {
            for (int i = height - 1; i >= 0; i--) {
                target = new Position(targetCoord, i);
                if (player.getShip().getComponentAt(target) != null) break;
            }
        } else if (attack == Direction.NORTH) {
            for (int i = 0; i < height; i++) {
                target = new Position(targetCoord, i);
                if (player.getShip().getComponentAt(target) != null) break;
            }
        } else if (attack == Direction.WEST) {
            for (int i = 0; i < width; i++) {
                target = new Position(i, targetCoord);
                if (player.getShip().getComponentAt(target) != null) break;
            }
        } else if (attack == Direction.EAST) {
            for (int i = width - 1; i >= 0; i--) {
                target = new Position(i, targetCoord);
                if (player.getShip().getComponentAt(target) != null) break;
            }
        }
        if (target != null && player.getShip().getComponentAt(target) != null) {
            return target;
        }
        return null;
    }

    /**
     * Check if player can and wants to defend with a shield
     * @param player The player attempting to defend
     * @param direction The direction of the attack
     * @return ShieldGenerator activated if defense is successful
     */
    private ShieldGenerator canDefendWithShield(Player player, Direction direction) {
        List<ShieldGenerator> shields = player.getShip().getShieldsProtecting(direction);
        if (!shields.isEmpty() && player.getShip().calculateTotalBatteryCharge() > 0) {
            for (ShieldGenerator shield : shields) {
                if (player.getShip().activateShield(shield)) {
                    return shield;
                }
            }
        }
        return null;
    }

    @Override
    protected int additionalPls() {
        return 0;
    }

    @Override
    protected void resolveEvent(Player p) {

    }

    @Override
    public void effect() {

    }
}
