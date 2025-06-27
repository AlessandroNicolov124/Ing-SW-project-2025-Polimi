package it.polimi.ingsw.AdventureCards;

import it.polimi.ingsw.*;
import it.polimi.ingsw.Components.DoubleCannon;
import it.polimi.ingsw.Components.DoubleEngine;
import java.util.List;

/**
 * CombatZoneA: versione classica (come CombatZone originale)
 * 1. Il giocatore con meno equipaggio perde 3 giorni di volo.
 * 2. Il giocatore con meno forza motrice perde 2 membri equipaggio.
 * 3. Il giocatore con meno cannoni subisce cannonate leggere e pesanti da sud.
 */

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
public class CombactZoneA extends CombactZoneBase {

    private int flightDayPenalty;
    private int crewLossPenalty;
    private static final Direction ATTACK_DIRECTION = Direction.SOUTH; // Attacks come from behind

    /**
     * Default constructor with standard values
     */
    public CombactZoneA() {
        super();
        this.flightDayPenalty = 3;  // Default: lose 3 flight days
        this.crewLossPenalty = 2;   // Default: lose 2 crew members
    }

    public CombactZoneA(int copies, int duration, int flightDayPenalty, int crewLossPenalty) {
        super(copies, duration);
        this.flightDayPenalty = flightDayPenalty;
        this.crewLossPenalty = crewLossPenalty;
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

    @Override
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
            // Apply flight day penalty
            gameModel.updatePosition(weakestCrew, -flightDayPenalty);
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
            boolean lightHit = applyLightCannonFire(weakestCannons, ATTACK_DIRECTION);
            // Apply heavy cannon fire
            boolean heavyHit = applyHeavyCannonFire(weakestCannons, ATTACK_DIRECTION);

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
     * Remove crew members from a player's ship
     * @param player The player losing crew
     * @param count Number of crew members to remove
     */
    private void removeCrewMembers(Player player, int count) {
        player.getShip().removeCrewMembers(Math.min(player.getShip().getTotalCrewCount(), count));
    }
}
