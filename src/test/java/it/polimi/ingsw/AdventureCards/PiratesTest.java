package it.polimi.ingsw.AdventureCards;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import it.polimi.ingsw.*;
import it.polimi.ingsw.Components.*;

public class PiratesTest {
    GameModel gameModel;
    Pirates pirates;
    Player winner;
    Player loser;

    @BeforeEach
    void setUp() {
        gameModel = new GameModel();

        // Winner: cannon strength >= pirates
        ShipBuilder ship1 = new ShipBuilder(7, 5);
        ship1.placeComponent(new Cabin(true), new Position(3, 2));
        DoubleCannon dc1 = new DoubleCannon();
        dc1.initializeConnectors(new NullConnectorType(), new UniversalConnectorType(), new UniversalConnectorType(), new UniversalConnectorType());
        DoubleCannon dc2 = new DoubleCannon();
        dc2.initializeConnectors(new NullConnectorType(), new UniversalConnectorType(), new UniversalConnectorType(), new UniversalConnectorType());
        DoubleCannon dc3 = new DoubleCannon();
        dc3.initializeConnectors(new NullConnectorType(), new UniversalConnectorType(), new UniversalConnectorType(), new UniversalConnectorType());
        ship1.placeComponent(dc1, new Position(2, 2));
        ship1.placeComponent(dc2, new Position(4, 2));
        ship1.placeComponent(dc3, new Position(3, 1));
        BatteryCompartment batteryCompartment = new BatteryCompartment(3, BatteryType.TRIPLE, 3);
        batteryCompartment.initializeConnectors(new UniversalConnectorType(), new UniversalConnectorType(), new UniversalConnectorType(), new UniversalConnectorType());
        ship1.placeComponent(batteryCompartment, new Position(3, 3));

        winner = new Player(1, "winner1", true, ship1);

        // Loser: cannon strength < pirates
        ShipBuilder ship2 = new ShipBuilder(7, 5);
        ship2.placeComponent(new Cabin(true), new Position(3, 2));
        SingleCannon sc1 = new SingleCannon();
        sc1.initializeConnectors(new NullConnectorType(), new UniversalConnectorType(), new UniversalConnectorType(), new UniversalConnectorType());
        SingleCannon sc2 = new SingleCannon();
        sc2.initializeConnectors(new NullConnectorType(), new UniversalConnectorType(), new UniversalConnectorType(), new UniversalConnectorType());
        SingleCannon sc3 = new SingleCannon();
        sc3.initializeConnectors(new NullConnectorType(), new UniversalConnectorType(), new UniversalConnectorType(), new UniversalConnectorType());
        ship2.placeComponent(sc1, new Position(2, 2));
        ship2.placeComponent(sc2, new Position(4, 2));
        ship2.placeComponent(sc3, new Position(3, 1));

        loser = new Player(2, "loser", true, ship2);

        gameModel.addPlayer(winner);
        gameModel.addPlayer(loser);
        gameModel.setCredits(winner, 0);
        gameModel.setCredits(loser, 0);
        gameModel.getFlightBoard().setInitialPosition(winner, 1);
        gameModel.getFlightBoard().setInitialPosition(loser, 2);

        pirates = new Pirates();
    }

    @Test
    void testFightPiratesRewardAndPenalty() {
        pirates.fightPirates(gameModel);

        // Winner: ha credits e viene spostato indietro
        assertEquals(4, gameModel.getCredits(winner), "Il vincitore deve ricevere 4 crediti");
        assertEquals(0, gameModel.getFlightBoard().getPosition(winner), "Il vincitore deve essere spostato indietro di 2");
        // Loser: non riceve crediti, posizione invariata
        assertEquals(0, gameModel.getCredits(loser), "Il perdente non deve ricevere crediti");
        assertEquals(2, gameModel.getFlightBoard().getPosition(loser), "Il perdente non deve essere spostato");
    }

    @Test
    void testFightPiratesRefuseReward() {
        // Simula la vittoria ma rifiuta la ricompensa
        pirates.fightPiratesFor(winner); // pirates sconfitti
        pirates.offerReward(winner, gameModel, false); // rifiuta la ricompensa

        // Winner: non riceve crediti e non viene spostato
        assertEquals(0, gameModel.getCredits(winner), "Il vincitore che rifiuta la ricompensa non deve ricevere crediti");
        assertEquals(1, gameModel.getFlightBoard().getPosition(winner), "Il vincitore che rifiuta la ricompensa non deve essere spostato");
    }

    @Test
    void testFightPiratesRefuseRewardWith2Winner() {
        // Winner: cannon strength >= pirates
        ShipBuilder ship1 = new ShipBuilder(7, 5);
        ship1.placeComponent(new Cabin(true), new Position(3, 2));
        DoubleCannon dc1 = new DoubleCannon();
        dc1.initializeConnectors(new NullConnectorType(), new UniversalConnectorType(), new UniversalConnectorType(), new UniversalConnectorType());
        DoubleCannon dc2 = new DoubleCannon();
        dc2.initializeConnectors(new NullConnectorType(), new UniversalConnectorType(), new UniversalConnectorType(), new UniversalConnectorType());
        DoubleCannon dc3 = new DoubleCannon();
        dc3.initializeConnectors(new NullConnectorType(), new UniversalConnectorType(), new UniversalConnectorType(), new UniversalConnectorType());
        ship1.placeComponent(dc1, new Position(2, 2));
        ship1.placeComponent(dc2, new Position(4, 2));
        ship1.placeComponent(dc3, new Position(3, 1));
        BatteryCompartment batteryCompartment = new BatteryCompartment(3, BatteryType.TRIPLE, 3);
        batteryCompartment.initializeConnectors(new UniversalConnectorType(), new UniversalConnectorType(), new UniversalConnectorType(), new UniversalConnectorType());
        ship1.placeComponent(batteryCompartment, new Position(3, 3));

        Player winner2 = new Player(1, "winner2", true, ship1);
        gameModel.addPlayer(winner2);
        gameModel.setCredits(winner2, 0);
        gameModel.getFlightBoard().setInitialPosition(winner2, 4);

        pirates.fightPirates(gameModel);
        assertEquals(0, gameModel.getCredits(loser), "Il loser non deve ricevere crediti");
        assertEquals(4, gameModel.getCredits(winner2), "Il vincitore deve ricevere 4 crediti");
        assertEquals(3, gameModel.getFlightBoard().getPosition(winner2), "Il vincitore deve essere spostato indietro di 2");
        assertEquals(1, gameModel.getFlightBoard().getPosition(winner), "Il winner deve rimanere nella sua posizione");
        assertEquals(0, gameModel.getCredits(winner), "Il winner non può ricevere crediti");
    }
}
