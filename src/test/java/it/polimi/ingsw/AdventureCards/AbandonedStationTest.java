package it.polimi.ingsw.AdventureCards;

import it.polimi.ingsw.*;
import it.polimi.ingsw.Components.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AbandonedStationTest {
    private AbandonedStation station;
    private ShipBuilder ship;
    private Player player;
    private GameModel gameModel;
    private List<Cargo> goods;

    @BeforeEach
    void setUp() {
        goods = List.of(new RedCargo(), new RedCargo(), new YellowCargo(), new GreenCargo(), new BlueCargo());
        station = new AbandonedStation(1, 1, 2, 2, goods);
        ship = new ShipBuilder(7, 5);
        player = new Player(1, "Test", true, ship);

        gameModel = new GameModel();
        gameModel.addPlayer(player);
        gameModel.setCredits(player, 10);

        // Setup nave con almeno 2 crew e stive sufficienti
        ship.placeComponent(new Cabin(true), ship.getStartingPosition());
        ship.addCrewOfType(CrewType.HUMAN, 2);
        assertEquals(2, ship.getHumanCount());

        CargoHolds normalCargoHolds = new NormalCargoHolds(CargoHoldsSize.DOUBLE);
        normalCargoHolds.initializeConnectors(new UniversalConnectorType(), new UniversalConnectorType(), new UniversalConnectorType(), new UniversalConnectorType());
        CargoHolds specialCargoHolds = new SpecialCargoHolds(CargoHoldsSize.DOUBLE);
        specialCargoHolds.initializeConnectors(new UniversalConnectorType(), new UniversalConnectorType(), new UniversalConnectorType(), new UniversalConnectorType());
        ship.placeComponent(normalCargoHolds, ship.getStartingPosition().getAdjacentPosition(Direction.EAST));
        ship.placeComponent(specialCargoHolds, ship.getStartingPosition().getAdjacentPosition(Direction.WEST));

        gameModel.getFlightBoard().setInitialPosition(player, 1);
        assertEquals(1, gameModel.getFlightBoard().getPosition(player));
    }

    @Test
    @DisplayName("Non permette docking se opportunityUsed è true")
    void testDockAtStationAlreadyUsed() {
        station.dockAtStation(player, gameModel); // Prima chiamata
        assertFalse(station.dockAtStation(player, gameModel)); // Seconda chiamata fallisce
    }

    @Test
    @DisplayName("Non permette docking se crew insufficiente")
    void testDockAtStationNotEnoughCrew() {
        player.getShip().removeHumans(1); // Rimuove un membro, ora crew < minCrewRequired
        assertFalse(station.dockAtStation(player, gameModel));
    }

    @Test
    @DisplayName("Permette docking e carica le merci correttamente")
    void testDockAtStationLoadsGoods() {
        boolean result = station.dockAtStation(player, gameModel);
        assertTrue(result);
        // Verifica che le merci siano state caricate nelle stive
        int totalCargo = player.getShip().getCargoHolds().stream()
                .mapToInt(chs -> chs.getCargos().size())
                .sum();
        assertTrue(totalCargo > 0);
        assertTrue(station.isOpportunityUsed());
    }

    @Test
    @DisplayName("Applica il costo in giorni di volo con posizione finale 0")
    void testFlightDaysCostApplied() {
        gameModel.getFlightBoard().getPosition(player);
        station.dockAtStation(player, gameModel);
        int newPosition = gameModel.getFlightBoard().getPosition(player);
        assertEquals(0, newPosition);
        assertTrue(station.isOpportunityUsed());
    }

    @Test
    @DisplayName("Applica il costo in giorni di volo con posizione finale diversa da 0")
    void testFlightDaysCostApplied2() {
        gameModel.getFlightBoard().setInitialPosition(player, 4);
        int initialPosition = gameModel.getFlightBoard().getPosition(player);
        station.dockAtStation(player, gameModel);
        int newPosition = gameModel.getFlightBoard().getPosition(player);
        assertEquals(initialPosition - station.getFlightDaysCost(), newPosition);
        assertTrue(station.isOpportunityUsed());
    }
}

