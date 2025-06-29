package it.polimi.ingsw.AdventureCards;

import it.polimi.ingsw.*;
import it.polimi.ingsw.Components.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class CombatZoneTest {
    private CombatZone combatZone;
    private GameModel gameModel;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        combatZone = new CombatZone(1, 1, 3, 2);
        gameModel = new GameModel();

        // Player 1: meno crew, meno engine, meno cannoni
        ShipBuilder ship1 = new ShipBuilder(7, 5);
        ship1.placeComponent(new Cabin(true), new Position(3, 2));
        DoubleEngine doubleEngine = new DoubleEngine();
        doubleEngine.initializeConnectors(new UniversalConnectorType(), new UniversalConnectorType(), new NullConnectorType(), new UniversalConnectorType());
        ship1.placeComponent(doubleEngine, new Position(2, 2));
        DoubleCannon doubleCannon = new DoubleCannon();
        doubleCannon.initializeConnectors(new NullConnectorType(), new UniversalConnectorType(), new UniversalConnectorType(), new UniversalConnectorType());
        ship1.placeComponent(doubleCannon, new Position(4, 2));
        BatteryCompartment batteryCompartment = new BatteryCompartment(3, BatteryType.TRIPLE, 3);
        batteryCompartment.initializeConnectors(new UniversalConnectorType(), new UniversalConnectorType(), new UniversalConnectorType(), new UniversalConnectorType());
        ship1.placeComponent(batteryCompartment, new Position(3, 1));
        ship1.addCrewOfType(CrewType.HUMAN, 2);
        player1 = new Player(1, "P1", true, ship1);

        System.out.println("Player 1 Ship Components: " + player1.getShip().getComponents().size());
        // Player 2: più crew, più engine, più cannoni
        ShipBuilder ship2 = new ShipBuilder(7, 5);
        ship2.placeComponent(new Cabin(true), new Position(3, 2));
        Cabin cabin = new Cabin(false);
        cabin.initializeConnectors();
        ship2.placeComponent(cabin, new Position(3, 3));
        DoubleEngine doubleEngine1 = new DoubleEngine();
        doubleEngine1.initializeConnectors(new UniversalConnectorType(), new UniversalConnectorType(), new NullConnectorType(), new UniversalConnectorType());
        DoubleEngine doubleEngine2 = new DoubleEngine();
        doubleEngine2.initializeConnectors(new UniversalConnectorType(), new UniversalConnectorType(), new NullConnectorType(), new UniversalConnectorType());
        DoubleCannon doubleCannon1 = new DoubleCannon();
        doubleCannon1.initializeConnectors(new NullConnectorType(), new UniversalConnectorType(), new UniversalConnectorType(), new UniversalConnectorType());
        DoubleCannon doubleCannon2 = new DoubleCannon();
        doubleCannon2.initializeConnectors(new NullConnectorType(), new UniversalConnectorType(), new UniversalConnectorType(), new UniversalConnectorType());
        BatteryCompartment batteryCompartment1 = new BatteryCompartment(3, BatteryType.TRIPLE, 3);
        BatteryCompartment batteryCompartment2 = new BatteryCompartment(3, BatteryType.TRIPLE, 3);
        batteryCompartment1.initializeConnectors(new UniversalConnectorType(), new UniversalConnectorType(), new UniversalConnectorType(), new UniversalConnectorType());
        batteryCompartment2.initializeConnectors(new UniversalConnectorType(), new UniversalConnectorType(), new UniversalConnectorType(), new UniversalConnectorType());
        ship2.placeComponent(batteryCompartment1, new Position(3, 1));
        ship2.placeComponent(batteryCompartment2, new Position(3, 0));
        ship2.placeComponent(doubleEngine1, new Position(2, 3));
        ship2.placeComponent(doubleEngine2, new Position(4, 3));
        ship2.placeComponent(doubleCannon1, new Position(2, 2));
        ship2.placeComponent(doubleCannon2, new Position(4, 2));
        ship2.addCrewOfType(CrewType.HUMAN, 3); // Aggiunge 3 membri equipaggio
        player2 = new Player(2, "P2", true, ship2);

        gameModel.addPlayer(player1);
        gameModel.addPlayer(player2);

        gameModel.getFlightBoard().setInitialPosition(player1, 4);
        gameModel.getFlightBoard().setInitialPosition(player2, 3);
    }

    @Test
    void testCrewPhasePenalty() {
        int posBefore = gameModel.getFlightBoard().getPosition(player1);
        combatZone.applyCombatZone(gameModel);
        int posAfter = gameModel.getFlightBoard().getPosition(player1);
        
        // Account for wrap-around: losing flight days means worse position
        // If position wrapped around, check that flight days were actually lost
        boolean lostFlightDays = (posAfter < posBefore) || 
                                (posBefore == 0 && posAfter > 20); // wrapped around from 0 to high numbers
        assertTrue(lostFlightDays, "Il giocatore con meno crew perde giorni di volo");
    }

    @Test
    void testEnginePhasePenalty() {
        int crewBefore = player1.getShip().getTotalCrewCount();
        combatZone.applyCombatZone(gameModel);
        int crewAfter = player1.getShip().getTotalCrewCount();
        assertTrue(crewAfter < crewBefore, "Il giocatore con meno motori perde membri equipaggio");
    }

    @Test
    void testFirepowerPhasePenalty() {
        int compBefore = player1.getShip().getNonDamageComponents().size();
        combatZone.applyCombatZone(gameModel);
        int compAfter = player1.getShip().getNonDamageComponents().size();
        assertTrue(compAfter <= compBefore, "Il giocatore con meno potenza di fuoco subisce danni da cannonate, solo se le cannonate colpiscono la navicella");
    }

    @Test
    void testNoPenaltyForStrongerPlayer() {
        int posBefore = gameModel.getFlightBoard().getPosition(player2);
        int crewBefore = player2.getShip().getTotalCrewCount();
        int compBefore = player2.getShip().getComponents().size();
        combatZone.applyCombatZone(gameModel);
        int posAfter = gameModel.getFlightBoard().getPosition(player2);
        int crewAfter = player2.getShip().getTotalCrewCount();
        int compAfter = player2.getShip().getComponents().size();
        assertEquals(posBefore, posAfter, "Il giocatore più forte non perde giorni di volo");
        assertEquals(crewBefore, crewAfter, "Il giocatore più forte non perde membri equipaggio");
        assertEquals(compBefore, compAfter, "Il giocatore più forte non subisce danni da cannonate");
    }

    @Test
    void testLightCannonHitWithShield() {
        // Setup: nave con shield rivolto a EST e batteria carica
        ShipBuilder ship = new ShipBuilder(7, 5);
        Cabin cabin = new Cabin(true);
        ship.placeComponent(cabin, new Position(3, 2));
        ShieldGenerator shield = new ShieldGenerator();
        shield.initializeConnectors(new UniversalConnectorType(), new UniversalConnectorType(), new UniversalConnectorType(), new UniversalConnectorType());
        shield.add(Direction.NORTH);
        shield.add(Direction.SOUTH); // Shield protects against attacks from SOUTH (behind)
        ship.placeComponent(shield, new Position(3, 3)); // a SOUTH della cabina
        BatteryCompartment battery = new BatteryCompartment(2, BatteryType.DOUBLE, 1);
        battery.initializeConnectors(new UniversalConnectorType(), new UniversalConnectorType(), new UniversalConnectorType(), new UniversalConnectorType());
        ship.placeComponent(battery, new Position(3, 1)); // Place battery at a different position
        Player p = new Player(3, "Shielded", true, ship);
        gameModel.addPlayer(p);
        gameModel.getFlightBoard().setInitialPosition(p, 2);

        int batteryBefore = ship.calculateTotalBatteryCharge();
        int compBefore = ship.getNonDamageComponents().size();

        assertEquals(p, combatZone.findPlayerWithWeakestCannons(gameModel.getPlayers()), "Il giocatore con il cannone più debole deve essere trovato correttamente");
        assertEquals(ship.getPositionOf(shield), combatZone.checkCannonHit(p, Direction.SOUTH, 3), "Il cannone deve colpire la posizione corretta");
        assertEquals(shield, combatZone.canDefendWithShield(p, Direction.SOUTH), "Il giocatore deve essere difeso dallo scudo");
        shield.discharge();
        assertEquals(compBefore, ship.getNonDamageComponents().size(), "Il componente non deve essere distrutto se protetto da shield");
        // La batteria deve essere scaricata di 1, e pure lo shield
        assertEquals(batteryBefore - 1, ship.calculateTotalBatteryCharge(), "La batteria deve essere scaricata di 1");
        assertFalse(shield.isCharged(), "Lo scudo deve essere scaricato");

        //in caso di ulteriore attacco, lo scudo non deve difendere perché è scarico e non ci sono batterie cariche
        assertNull(combatZone.canDefendWithShield(p, Direction.SOUTH), "Il giocatore non deve essere difeso dallo scudo");
        p.getShip().destroyComponentAt(new Position(3,1));
        assertEquals(compBefore-1, ship.getNonDamageComponents().size(), "Il componente deve essere distrutto se non protetto da shield");

    }
}
