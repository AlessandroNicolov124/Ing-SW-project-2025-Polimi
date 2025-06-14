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
        assertTrue(posAfter < posBefore, "Il giocatore con meno crew perde giorni di volo");
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
}
