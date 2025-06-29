package it.polimi.ingsw.AdventureCards;

import it.polimi.ingsw.*;
import it.polimi.ingsw.Components.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SmugglersTest {
    private GameModel gameModel;
    private Player winner;
    private Player loser;
    private Smugglers smugglers;

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
        SpecialCargoHolds specialCargoHolds = new SpecialCargoHolds(CargoHoldsSize.DOUBLE);
        specialCargoHolds.initializeConnectors(new UniversalConnectorType(), new UniversalConnectorType(), new UniversalConnectorType(), new UniversalConnectorType());
        ship1.placeComponent(specialCargoHolds, new Position(4, 3));
        winner = new Player(1, "winner", true, ship1);
        System.out.println("components of winner " + ship1.getComponents().size());

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
        SpecialCargoHolds specialCargoHolds2 = new SpecialCargoHolds(CargoHoldsSize.DOUBLE);
        specialCargoHolds2.initializeConnectors(new UniversalConnectorType(), new UniversalConnectorType(), new UniversalConnectorType(), new UniversalConnectorType());
        specialCargoHolds2.loadCargo(new BlueCargo());
        specialCargoHolds2.loadCargo(new RedCargo());
        ship2.placeComponent(specialCargoHolds2, new Position(3, 3));
        BatteryCompartment batteryCompartment2 = new BatteryCompartment(3, BatteryType.TRIPLE, 3);
        batteryCompartment2.initializeConnectors(new UniversalConnectorType(), new UniversalConnectorType(), new UniversalConnectorType(), new UniversalConnectorType());
        ship2.placeComponent(batteryCompartment2, new Position(4, 3));


        loser = new Player(2, "loser", true, ship2);
        System.out.println("components of loser " + ship1.getComponents().size());


        gameModel.addPlayer(winner);
        gameModel.addPlayer(loser);
        gameModel.setCredits(winner, 0);
        gameModel.setCredits(loser, 0);
        gameModel.getFlightBoard().setInitialPosition(winner, 2);
        gameModel.getFlightBoard().setInitialPosition(loser, 1);

        List<Cargo> cargoList = new ArrayList<>();
        cargoList.add(new RedCargo());
        cargoList.add(new BlueCargo());
        smugglers= new Smugglers(3, 2, 5, 2, cargoList, 1); // forza 4, 2 crediti, 2 spazi indietro, 2 merce
    }

    @Test
    void testFightSmugglersRewardAndPenalty() {
        int positionWinner = gameModel.getFlightBoard().getPosition(winner);
        int positionLoser = gameModel.getFlightBoard().getPosition(loser);

        smugglers.fightSmugglers(gameModel);

        // Winner: ha cargo e viene spostato indietro
        assertEquals(smugglers.getGoodsGainedOnVictory().size(), winner.getShip().getCargoList().size(), "Il vincitore deve ricevere le merci vinte");
        assertTrue(winner.getShip().getCargoList().containsAll(smugglers.getGoodsGainedOnVictory()), "Il vincitore deve ricevere le merci vinte");
        assertEquals(positionWinner - smugglers.getFlightDaysCost(), gameModel.getFlightBoard().getPosition(winner), "Il vincitore deve essere spostato indietro di 1");

        // Loser: non riceve cargo, ma non li deve nemmeno perdere
        assertEquals(positionWinner-smugglers.getFlightDaysCost(), loser.getShip().getCargoList().size(), "Il perdente non deve ricevere /perdere merci");
        assertEquals(positionLoser, gameModel.getFlightBoard().getPosition(loser), "La posizione del perdente deve rimanere invariata");
    }

    @Test
    void testFightSmugglersRefuseReward() {
        int positionWinner = gameModel.getFlightBoard().getPosition(winner);
        int cargosize = winner.getShip().getCargoList().size();
        // Simula la vittoria ma rifiuta la ricompensa
        smugglers.fightSmugglersFor(winner); //smugglers sconfitti
        smugglers.offerReward(winner, gameModel, false); // rifiuta la ricompensa

        // Winner: non riceve cargo, ma non li deve nemmeno perdere e non viene spostato
        assertEquals(cargosize, winner.getShip().getCargoList().size(), "Il winner non deve ricevere /perdere merci");
        assertEquals(positionWinner, gameModel.getFlightBoard().getPosition(winner), "Il vincitore che rifiuta la ricompensa non deve essere spostato");
    }

    @Test
    void testDefeatSmugglersGainGoodsAndMoveBack() {
        int initialCredits = winner.getShip().getCargoList().size(); // Inizialmente il vincitore ha 0 merci
        int initialPosition = gameModel.getFlightBoard().getPosition(winner);
        // Simula vittoria e accetta la ricompensa in crediti
        smugglers.fightSmugglersFor(winner); // smugglers sconfitti
        smugglers.offerReward(winner, gameModel, true); // accetta la ricompensa

        assertEquals(initialCredits + smugglers.getGoodsGainedOnVictory().size(), winner.getShip().getCargoList().size(), "Il vincitore deve avere le merci vinte");
        assertTrue(winner.getShip().getCargoList().containsAll(smugglers.getGoodsGainedOnVictory()), "Il vincitore deve ricevere le merci vinte");
        assertEquals(initialPosition - smugglers.getFlightDaysCost(), gameModel.getFlightBoard().getPosition(winner));
    }

    @Test
    void testSmugglersDefeatPlayerLosesGoods() {
        // Simula che il giocatore abbia 2 merci
        int initialGoods = loser.getShip().getCargoList().size();
        int initialBatteries = loser.getShip().calculateTotalBatteryCharge();

        smugglers = new Smugglers(3, 2, 5, 3, null, 1);

        assertFalse(smugglers.fightSmugglersFor(loser));  // il player perde
        smugglers.removeMostValuableGoodsOrBatteries(loser); // il giocatore perde le merci più preziose
        assertEquals(initialGoods - 2, loser.getShip().getCargoList().size(), "Il giocatore deve perdere tutte le merci");
        assertEquals(initialBatteries - 1, loser.getShip().calculateTotalBatteryCharge(), "Il giocatore deve perdere una batteria");
    }
}
