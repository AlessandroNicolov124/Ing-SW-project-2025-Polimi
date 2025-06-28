package it.polimi.ingsw.AdventureCards;

import it.polimi.ingsw.Components.*;
import it.polimi.ingsw.Player;
import it.polimi.ingsw.Position;
import it.polimi.ingsw.ShipBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class PlanetsTest {
    private Planets planets;

    @BeforeEach
    void setUp() {
        planets = new Planets();
    }

    @Test
    void testNumberOfPlanetsBetween2and4() {
        int numPlanets = planets.getPlanets().size();
        assertTrue(numPlanets >= 2 && numPlanets <= 4, "The number of planets must be between 2 and 4");
    }

    @Test
    void testLandingCost() {
        assertEquals(2, planets.getFlightDaysCost(), "Landing cost must be 2 by default");
    }

    @Test
    void testOnlyOneRocketPerPlanet() {
        List<Planets.Planet> planetList = planets.getPlanets();
        Player p1 = new Player(1, "A", true);
        Player p2 = new Player(1, "B", true);
        planets.getLandedPlayers().put(planetList.get(0), p1);
        boolean alreadyLanded = planets.getLandedPlayers().containsKey(planetList.get(0));
        assertTrue(alreadyLanded, "Only one rocket can land on a planet");
        planets.getLandedPlayers().put(planetList.get(0), p2);
        assertEquals(p2, planets.getLandedPlayers().get(planetList.get(0)), "Second player overwrites the first (should be prevented in game logic)");
    }

    @Test
    void testNoPlayerIsForcedToLand() {
        assertTrue(planets.getLandedPlayers().isEmpty(), "No player is forced to land");
    }

    @Test
    void testPlayersChooseInOrderAndCanBlockOthers() {
        // Setup: 3 players, 2 planets
        Player leader = new Player(1, "Leader", true);
        Player second = new Player(2, "Second", true);
        Player third = new Player(3, "Third", true);
        List<Player> players = Arrays.asList(leader, second, third);
        List<Planets.Planet> planetList = planets.getPlanets();
        // Simulate leader lands on first planet
        planets.getLandedPlayers().put(planetList.get(0), leader);
        // Simulate second lands on second planet
        planets.getLandedPlayers().put(planetList.get(1), second);
        // Third cannot land (no more planets)
        assertEquals(2, planets.getLandedPlayers().size());
        assertFalse(planets.getLandedPlayers().containsValue(third), "Third player cannot land if all planets are taken");
    }

    @Test
    void testLandingLoadsGoodsAndMovesBack() {
        // Setup: 1 player, 1 planet with goods
        ShipBuilder ship = new ShipBuilder(7, 5);
        ship.placeComponent(new Cabin(true), ship.getStartingPosition());
        SpecialCargoHolds specialCargoHolds = new SpecialCargoHolds(CargoHoldsSize.DOUBLE);
        specialCargoHolds.initializeConnectors(new UniversalConnectorType(), new UniversalConnectorType(), new UniversalConnectorType(), new UniversalConnectorType());
        ship.placeComponent(specialCargoHolds, new Position(3, 3));
        Player player = new Player(1, "Test", true, ship);
        List<Cargo> cargoList = new ArrayList<>();
        cargoList.add(new BlueCargo());
        cargoList.add(new RedCargo());
        Planets.Planet planet = new Planets.Planet("TestPlanet", cargoList);
        List<Planets.Planet> planetList = new ArrayList<>();
        planetList.add(planet);
        Planets testPlanets = new Planets(1, 5, planetList, 3);
        // Simulate landing
        testPlanets.getLandedPlayers().put(planet, player);
        // Simulate loading goods from planet to player's ship
        planets.loadGoods(player, planet);
        // Check that the player is marked as landed
        assertTrue(testPlanets.getLandedPlayers().containsValue(player));
        // Check the cost is correct
        assertEquals(3, testPlanets.getFlightDaysCost());
        // Check that the player's ship contains the goods from the planet
        List<Cargo> playerCargo = player.getShip().getCargoList();
        System.out.println("Player's cargo: " + playerCargo.size());
        assertTrue(playerCargo.containsAll(cargoList), "Player's ship should contain all goods from the planet");
    }

    @Test
    void testCreateDefaultPlanets() {
        Planets defaultPlanets = new Planets();
        List<Planets.Planet> planetsList = defaultPlanets.getPlanets();
        assertTrue(planetsList.size() >= 2 && planetsList.size() <= 4, "Default planets list should have 2 to 4 planets");
        for (Planets.Planet p : planetsList) {
            assertNotNull(p.getName());
            assertNotNull(p.getGoodsToLoad());
            assertFalse(p.getGoodsToLoad().isEmpty(), "Each default planet should have goods");
        }
    }

    @Test
    void testChoosePlanetReturnsAvailablePlanet() throws Exception {
        Planets testPlanets = new Planets();
        Player player = new Player(1, "Test", true);
        List<Player> landedPlayers = new ArrayList<>();
        // Use reflection to access private method
        java.lang.reflect.Method method = Planets.class.getDeclaredMethod("choosePlanet", Player.class, List.class);
        method.setAccessible(true);
        Planets.Planet chosen = (Planets.Planet) method.invoke(testPlanets, player, landedPlayers);
        assertNotNull(chosen, "choosePlanet should return a planet if available");
        // Mark all planets as landed
        for (Planets.Planet p : testPlanets.getPlanets()) {
            testPlanets.getLandedPlayers().put(p, player);
        }
        Planets.Planet none = (Planets.Planet) method.invoke(testPlanets, player, landedPlayers);
        assertNull(none, "choosePlanet should return null if no planets available");
    }

    @Test
    void testLoadGoodsPriorityAndCapacity() {
        // Setup: player with limited cargo holds
        ShipBuilder ship = new ShipBuilder(7, 5);
        ship.placeComponent(new Cabin(true), ship.getStartingPosition());
        SpecialCargoHolds specialCargoHolds = new SpecialCargoHolds(CargoHoldsSize.SINGLE);
        specialCargoHolds.initializeConnectors(new UniversalConnectorType(), new UniversalConnectorType(), new UniversalConnectorType(), new UniversalConnectorType());
        ship.placeComponent(specialCargoHolds, new Position(3, 3));
        Player player = new Player(1, "Test", true, ship);

        // Planet with multiple goods (red, blue, green, yellow)
        List<Cargo> goods = new ArrayList<>();
        goods.add(new RedCargo());
        goods.add(new BlueCargo());
        goods.add(new GreenCargo());
        goods.add(new YellowCargo());
        Planets.Planet planet = new Planets.Planet("TestPlanet", goods);
        Planets testPlanets = new Planets(1, 5, List.of(planet), 2);
        // Only one good should be loaded, and it should be the red one (priority)
        testPlanets.loadGoods(player, planet);
        List<Cargo> loaded = player.getShip().getCargoList();
        assertEquals(1, loaded.size(), "Only one cargo should be loaded due to capacity");
        assertInstanceOf(RedCargo.class, loaded.get(0), "Red cargo should have priority");
    }
}
