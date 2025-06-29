package it.polimi.ingsw.AdventureCards;

import it.polimi.ingsw.*;
import it.polimi.ingsw.Components.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class OpenSpaceTest {
    @Test
    void testApplyOpenSpaceUpdatesPlayerPositions() {
        // Setup GameModel e giocatori
        GameModel gameModel = new GameModel();
        Player player1 = new Player(1, "p1", true);
        Player player2 = new Player(2, "p2", true);

        // Aggiungi un motore a ciascuna nave
        Cabin cabin1 = new Cabin(true);
        Cabin cabin2 = new Cabin(true);
        Engine engine1 = new SingleEngine();
        engine1.initializeConnectors(new UniversalConnectorType(), new UniversalConnectorType(), new NullConnectorType(), new UniversalConnectorType());
        Engine engine2 = new DoubleEngine();
        engine2.initializeConnectors(new UniversalConnectorType(), new UniversalConnectorType(), new NullConnectorType(), new UniversalConnectorType());
        player1.placeComponent(cabin1,player1.getShip().getStartingPosition());
        player1.placeComponent(engine1, player1.getShip().getStartingPosition().getAdjacentPosition(Direction.SOUTH));
        player2.placeComponent(cabin2,player2.getShip().getStartingPosition());
        player2.placeComponent(engine2, player2.getShip().getStartingPosition().getAdjacentPosition(Direction.SOUTH));

        // Forza motrice base = 1
        gameModel.addPlayer(player1);
        gameModel.addPlayer(player2);
        gameModel.getFlightBoard().setInitialPosition(player1, 1);
        gameModel.getFlightBoard().setInitialPosition(player2, 2);

        // Posizione iniziale
        int pos1Before = gameModel.getFlightBoard().getPosition(player1);
        int pos2Before = gameModel.getFlightBoard().getPosition(player2);

        OpenSpace openSpace = new OpenSpace();

        int expectedPos1 = pos1Before + player1.getShip().calculateEnginePower();
        int expectedPos2 = pos2Before + player2.getShip().calculateEnginePower();

        openSpace.applyOpenSpace(gameModel);

        // Dopo l'effetto ogni giocatore deve anzare di 1
        int pos1After = gameModel.getFlightBoard().getPosition(player1);
        int pos2After = gameModel.getFlightBoard().getPosition(player2);
        assertEquals(expectedPos1, pos1After, "p1 deve avanzare di 1 (single engine)");
        assertEquals(expectedPos2, pos2After, "p2 deve avanzare di 2 (double engine attivato)");
    }

    @Test
    void testApplyOpenSpaceWithDoubleEngineAndBatteries() {
        GameModel gameModel = new GameModel();
        Player player1 = new Player(1, "p1", true);
        Player player2 = new Player(2, "p2", true);

        // Single engine per Alice
        Cabin cabin1 = new Cabin(true);
        Engine engine1 = new SingleEngine();
        engine1.initializeConnectors(new UniversalConnectorType(), new UniversalConnectorType(), new NullConnectorType(), new UniversalConnectorType());
        player1.placeComponent(cabin1, player1.getShip().getStartingPosition());
        player1.placeComponent(engine1, player1.getShip().getStartingPosition().getAdjacentPosition(Direction.SOUTH));

        // Double engine e batteria per Bob
        Cabin cabin2 = new Cabin(true);
        Engine engine2 = new DoubleEngine();
        engine2.initializeConnectors(new UniversalConnectorType(), new UniversalConnectorType(), new NullConnectorType(), new UniversalConnectorType());
        BatteryCompartment battery = new BatteryCompartment(3, BatteryType.TRIPLE, 0);
        battery.initializeConnectors(new UniversalConnectorType(), new UniversalConnectorType(), new NullConnectorType(), new UniversalConnectorType());
        player2.placeComponent(cabin2, player2.getShip().getStartingPosition());
        player2.placeComponent(engine2, player2.getShip().getStartingPosition().getAdjacentPosition(Direction.SOUTH));
        player2.placeComponent(battery, player2.getShip().getStartingPosition().getAdjacentPosition(Direction.EAST));

        battery.recharge();
        assertEquals(3, battery.getChargeLevel(), "La batteria di p2 deve essere completamente carica");

        gameModel.addPlayer(player1);
        gameModel.addPlayer(player2);
        gameModel.getFlightBoard().setInitialPosition(player1, 1);
        gameModel.getFlightBoard().setInitialPosition(player2, 2);

        int pos1Before = gameModel.getFlightBoard().getPosition(player1);
        int pos2Before = gameModel.getFlightBoard().getPosition(player2);
        int batteryChargeBefore = player2.getShip().calculateTotalBatteryCharge();

        OpenSpace openSpace = new OpenSpace();

        int expectedPos1 = pos1Before + player1.getShip().calculateEnginePowerWithBatteries(player1.getShip().calculateTotalBatteryCharge() > 0);
        int expectedPos2 = pos2Before + player2.getShip().calculateEnginePowerWithBatteries(player2.getShip().calculateTotalBatteryCharge() > 0);

        openSpace.applyOpenSpace(gameModel);

        int pos1After = gameModel.getFlightBoard().getPosition(player1);
        int pos2After = gameModel.getFlightBoard().getPosition(player2);

        int batteryChargeAfter = player2.getShip().calculateTotalBatteryCharge();

        assertEquals(expectedPos1, pos1After, "p1 deve avanzare di 1 (single engine)");
        assertEquals(expectedPos2, pos2After, "p2 deve avanzare di 2 (double engine attivato)");
        assertTrue(batteryChargeAfter<batteryChargeBefore, "Le batterie di p2 devono essere scaricate dopo l'uso");
    }

    @Test
    void testApplyOpenSpaceWithDoubleEngineAndNOBatteries() {
        GameModel gameModel = new GameModel();
        Player player1 = new Player(1, "p1", true);
        Player player2 = new Player(2, "p2", true);

        // Single engine per Alice
        Cabin cabin1 = new Cabin(true);
        Engine engine1 = new SingleEngine();
        engine1.initializeConnectors(new UniversalConnectorType(), new UniversalConnectorType(), new NullConnectorType(), new UniversalConnectorType());
        player1.placeComponent(cabin1, player1.getShip().getStartingPosition());
        player1.placeComponent(engine1, player1.getShip().getStartingPosition().getAdjacentPosition(Direction.SOUTH));

        // Double engine e batteria per Bob
        Cabin cabin2 = new Cabin(true);
        Engine engine2 = new DoubleEngine();
        engine2.initializeConnectors(new UniversalConnectorType(), new UniversalConnectorType(), new NullConnectorType(), new UniversalConnectorType());
        BatteryCompartment battery = new BatteryCompartment(3, BatteryType.TRIPLE, 0);
        battery.initializeConnectors(new UniversalConnectorType(), new UniversalConnectorType(), new NullConnectorType(), new UniversalConnectorType());
        player2.placeComponent(cabin2, player2.getShip().getStartingPosition());
        player2.placeComponent(engine2, player2.getShip().getStartingPosition().getAdjacentPosition(Direction.SOUTH));
        player2.placeComponent(battery, player2.getShip().getStartingPosition().getAdjacentPosition(Direction.EAST));

        gameModel.addPlayer(player1);
        gameModel.addPlayer(player2);
        gameModel.getFlightBoard().setInitialPosition(player1, 1);
        gameModel.getFlightBoard().setInitialPosition(player2, 2);

        int pos1Before = gameModel.getFlightBoard().getPosition(player1);
        int pos2Before = gameModel.getFlightBoard().getPosition(player2);
        int batteryChargeBefore = player2.getShip().calculateTotalBatteryCharge();

        OpenSpace openSpace = new OpenSpace();

        int expectedPos1 = pos1Before + player1.getShip().calculateEnginePowerWithBatteries(player1.getShip().calculateTotalBatteryCharge() > 0);
        int expectedPos2 = pos2Before + player2.getShip().calculateEnginePowerWithBatteries(player2.getShip().calculateTotalBatteryCharge() > 0);

        openSpace.applyOpenSpace(gameModel);

        int pos1After = gameModel.getFlightBoard().getPosition(player1);
        int pos2After = gameModel.getFlightBoard().getPosition(player2);
        int batteryChargeAfter = player2.getShip().calculateTotalBatteryCharge();

        assertEquals(expectedPos1, pos1After, "p1 deve avanzare di 1 (single engine)");
        assertEquals(expectedPos2, pos2After, "p2 deve avanzare di 1 (double engine non attivato)");
        assertEquals(batteryChargeAfter, batteryChargeBefore, "Le batterie di p2 non devono essere state usate");
    }
}
