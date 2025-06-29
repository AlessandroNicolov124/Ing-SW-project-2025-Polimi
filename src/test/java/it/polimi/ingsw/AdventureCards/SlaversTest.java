package it.polimi.ingsw.AdventureCards;

import it.polimi.ingsw.*;
import it.polimi.ingsw.Components.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class SlaversTest {
    private Player winner, loser;
    private GameModel gameModel;
    private Slavers slavers;

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

        winner.getShip().addCrewOfType(CrewType.HUMAN, 2);
        loser.getShip().addCrewOfType(CrewType.HUMAN, 2);

        gameModel.setCredits(winner, 0);
        gameModel.setCredits(loser, 0);
        gameModel.getFlightBoard().setInitialPosition(winner, 1);
        gameModel.getFlightBoard().setInitialPosition(loser, 2);

        slavers = new Slavers(  1, 1, 4, 5, 2, 2);
        // forza 4, 5 crediti, 2 spazi indietro, 2 crew persa
    }

    @Test
    void testFightSlaversRewardAndPenalty() {
        int initialCreditsWinner = gameModel.getCredits(winner);
        int initialPositionWinner = gameModel.getFlightBoard().getPosition(winner);
        int initialCreditsLoser = gameModel.getCredits(loser);
        int initialPositionLoser = gameModel.getFlightBoard().getPosition(loser);
        int initialCrewLoser = loser.getShip().getTotalCrewCount();

        slavers.fightSlavers(gameModel);

        int expectedCreditsWinner = initialCreditsWinner + slavers.getRewardCredits();
        int expectedPositionWinner = initialPositionWinner - slavers.getFlightDaysCost();
        if(expectedPositionWinner < 0) {
            expectedPositionWinner +=24;
        }
        int expectedCrewLoser = Math.max(0, initialCrewLoser - slavers.getCrewLoss());

        // Winner: ha credits e viene spostato indietro
        assertEquals(expectedCreditsWinner, gameModel.getCredits(winner), "Il vincitore deve ricevere 5 crediti");
        assertEquals(expectedPositionWinner, gameModel.getFlightBoard().getPosition(winner), "Il vincitore deve essere spostato indietro di 2");
        // Loser: non riceve crediti, posizione invariata
        assertEquals(initialCreditsLoser, gameModel.getCredits(loser), "Il perdente non deve ricevere crediti");
        assertEquals(initialPositionLoser, gameModel.getFlightBoard().getPosition(loser), "Il perdente non deve essere spostato");
        assertEquals(expectedCrewLoser, loser.getShip().getTotalCrewCount(), "Il perdente deve perdere equipaggio");
    }

    @Test
    void testFightSlaversRefuseReward() {
        int initialCreditsWinner = gameModel.getCredits(winner);
        int initialPositionWinner = gameModel.getFlightBoard().getPosition(winner);

        // Simula la vittoria ma rifiuta la ricompensa
        slavers.fightSlaversFor(winner); // pirates sconfitti
        slavers.offerReward(winner, gameModel, false); // rifiuta la ricompensa

        // Winner: non riceve crediti e non viene spostato
        assertEquals(initialCreditsWinner, gameModel.getCredits(winner), "Il vincitore che rifiuta la ricompensa non deve ricevere crediti");
        assertEquals(initialPositionWinner, gameModel.getFlightBoard().getPosition(winner), "Il vincitore che rifiuta la ricompensa non deve essere spostato");
    }

    @Test
    void testFightSlaversWith2Winner() {
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
        winner2.getShip().addCrewOfType(CrewType.HUMAN, 2);
        gameModel.getFlightBoard().setInitialPosition(winner2, 3);

        int initialPositionWinner = gameModel.getFlightBoard().getPosition(winner);
        int initialPositionWinner2 = gameModel.getFlightBoard().getPosition(winner2);
        int initialCreditsWinner = gameModel.getCredits(winner);
        int initialCreditsWinner2 = gameModel.getCredits(winner2);
        int initialPositionLoser = gameModel.getFlightBoard().getPosition(loser);
        int initialCreditsLoser = gameModel.getCredits(loser);

        slavers.fightSlavers(gameModel);

        int expectedPositionWinner2 = initialPositionWinner2 - slavers.getFlightDaysCost();
        if(expectedPositionWinner2 < 0) {
            expectedPositionWinner2 +=24;
        }
        int expectedCreditsWinner2 = initialCreditsWinner2 + slavers.getRewardCredits();

        assertEquals(initialCreditsLoser, gameModel.getCredits(loser), "Il loser non deve ricevere crediti");
        assertEquals(expectedCreditsWinner2, gameModel.getCredits(winner2), "Il vincitore deve ricevere 5 crediti");
        assertEquals(expectedPositionWinner2, gameModel.getFlightBoard().getPosition(winner2), "Il vincitore deve essere spostato indietro di 2, ma la prima posizione libera è la 3");
        assertEquals(initialPositionWinner, gameModel.getFlightBoard().getPosition(winner), "Il winner deve rimanere nella sua posizione");
        assertEquals(initialPositionLoser, gameModel.getFlightBoard().getPosition(loser), "Il loser deve rimanere nella sua posizione");
        assertEquals(initialCreditsWinner, gameModel.getCredits(winner), "Il winner non può ricevere crediti");
    }

    @Test
    void testDefeatSlaversGainCreditsAndMoveBack() {
        int initialCredits = gameModel.getCredits(winner);
        int initialPosition = gameModel.getFlightBoard().getPosition(winner);
        Slavers slavers = new Slavers(3, 2, 4, 5, 2, 1); // forza 4, 5 crediti, 2 spazi indietro, 1 crew persa
        // Simula vittoria e accetta la ricompensa
        slavers.fightSlaversFor(winner);
        slavers.offerReward(winner, gameModel, true); // accetta la ricompensa
        assertEquals(initialCredits + 5, gameModel.getCredits(winner));
        assertTrue(initialPosition > gameModel.getFlightBoard().getPosition(winner));
    }

    @Test
    void testDefeatSlaversRefuseRewardAndAccept() {
        int initialCredits = gameModel.getCredits(winner);
        int initialPosition = gameModel.getFlightBoard().getPosition(winner);
        Slavers slavers = new Slavers(3, 2, 4, 5, 2, 1);
        slavers.fightSlaversFor(winner);
        slavers.offerReward(winner, gameModel, false); // rifiuta la ricompensa
        assertEquals(initialCredits, gameModel.getCredits(winner));
        assertEquals(initialPosition, gameModel.getFlightBoard().getPosition(winner));

        slavers.fightSlaversFor(winner);
        slavers.offerReward(winner, gameModel, true);
        assertEquals(initialCredits + 5, gameModel.getCredits(winner));
        assertTrue(initialPosition > gameModel.getFlightBoard().getPosition(winner));
    }

    @Test
    void testSlaversDefeatPlayerLosesCrew() {
        int initialCrew = loser.getShip().getTotalCrewCount();
        slavers.fightSlaversFor((loser)); // il player perde
        assertFalse(slavers.isDefeated(), "Slavers should not be defeated if the player loses");
        slavers.surrenderCrew(Collections.singletonList(loser)); // il player perde 2 crew
        assertEquals(initialCrew - Math.min(2, initialCrew), loser.getShip().getTotalCrewCount());
    }
}

