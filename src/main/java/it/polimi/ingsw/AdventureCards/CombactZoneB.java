package it.polimi.ingsw.AdventureCards;

import it.polimi.ingsw.*;
import it.polimi.ingsw.Components.Cargo;
import it.polimi.ingsw.Components.CargoHolds;
import it.polimi.ingsw.Components.DoubleCannon;
import it.polimi.ingsw.Components.DoubleEngine;

import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * CombatZoneB: versione alternativa (come CombatZone1)
 * 1. Il giocatore con meno firepower perde 4 giorni di volo.
 * 2. Il giocatore con meno forza motrice perde 3 cargo.
 * 3. Il giocatore con meno equipaggio subisce cannonate leggere da nord, est, ovest e una pesante da sud.
 */
public class CombactZoneB extends CombactZoneBase {
    private int flightDayPenalty;
    private int goodsLostPenalty;

    public CombactZoneB(int copies, int duration, int flightDayPenalty, int goodsLostPenalty) {
        super(copies, duration);
        this.flightDayPenalty = flightDayPenalty;
        this.goodsLostPenalty = goodsLostPenalty;
    }

    /**
     * Default constructor with standard values
     */
    public CombactZoneB() {
        super(1, 1);
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
     * Remove cargo from a player's ship
     * @param player The player losing crew
     */
    protected void removeCargo(Player player) {
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

    public int getFlightDayPenalty() {
        return flightDayPenalty;
    }

    public int getGoodsLostPenalty() {
        return goodsLostPenalty;
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
