package it.polimi.ingsw.AdventureCards;

import it.polimi.ingsw.Components.*;
import it.polimi.ingsw.GameModel;
import it.polimi.ingsw.Player;
import it.polimi.ingsw.ShipBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

/**
 * Test class for Planets adventure card with Level 1 and Level 2 variations
 */
class PlanetsTest {

    private GameModel gameModel;
    private Player testPlayer;
    private ShipBuilder testShip;

    @BeforeEach
    void setUp() {
        gameModel = new GameModel();
        testShip = new ShipBuilder(7, 5);
        testPlayer = new Player(1, "TestPlayer", true, testShip);
        gameModel.addPlayer(testPlayer);
    }

    @Test
    @DisplayName("Default constructor should create Level 1 Variation 1")
    void testDefaultConstructor() {
        Planets planets = new Planets();
        
        assertEquals(Planets.PlanetVariation.LEVEL1_VARIATION_1, planets.getVariation());
        assertEquals(2, planets.getFlightDaysCost());
        assertEquals(3, planets.getPlanets().size());
    }

    @Test
    @DisplayName("Level 1 Variation 1 should have correct configuration")
    void testLevel1Variation1() {
        Planets planets = new Planets(1, 1, Planets.PlanetVariation.LEVEL1_VARIATION_1);
        
        assertEquals(Planets.PlanetVariation.LEVEL1_VARIATION_1, planets.getVariation());
        assertEquals(2, planets.getFlightDaysCost());
        assertEquals(3, planets.getPlanets().size());
        
        List<Planets.Planet> planetList = planets.getPlanets();
        assertTrue(planetList.get(0).getCargoCount() > 0);
        assertTrue(planetList.get(1).getCargoCount() > 0);
        assertTrue(planetList.get(2).getCargoCount() > 0);
    }

    @Test
    @DisplayName("Level 1 Variation 2 should have 4 planets")
    void testLevel1Variation2() {
        Planets planets = new Planets(1, 1, Planets.PlanetVariation.LEVEL1_VARIATION_2);
        
        assertEquals(Planets.PlanetVariation.LEVEL1_VARIATION_2, planets.getVariation());
        assertEquals(3, planets.getFlightDaysCost());
        assertEquals(4, planets.getPlanets().size()); // Only variation with 4 planets
    }

    @Test
    @DisplayName("Level 1 Variation 4 should have lowest flight day cost")
    void testLevel1Variation4() {
        Planets planets = new Planets(1, 1, Planets.PlanetVariation.LEVEL1_VARIATION_4);
        
        assertEquals(Planets.PlanetVariation.LEVEL1_VARIATION_4, planets.getVariation());
        assertEquals(1, planets.getFlightDaysCost()); // Lowest cost
        assertEquals(3, planets.getPlanets().size());
    }

    @Test
    @DisplayName("Level 2 Variation 1 should have correct configuration")
    void testLevel2Variation1() {
        Planets planets = new Planets(1, 1, Planets.PlanetVariation.LEVEL2_VARIATION_1);
        
        assertEquals(Planets.PlanetVariation.LEVEL2_VARIATION_1, planets.getVariation());
        assertEquals(3, planets.getFlightDaysCost());
        assertEquals(2, planets.getPlanets().size());
    }

    @Test
    @DisplayName("Level 2 Variation 4 should have 4 planets")
    void testLevel2Variation4() {
        Planets planets = new Planets(1, 1, Planets.PlanetVariation.LEVEL2_VARIATION_4);
        
        assertEquals(Planets.PlanetVariation.LEVEL2_VARIATION_4, planets.getVariation());
        assertEquals(2, planets.getFlightDaysCost()); 
        assertEquals(4, planets.getPlanets().size()); // Only Level 2 variation with 4 planets
    }

    @Test
    @DisplayName("Planet should calculate total cargo value correctly")
    void testPlanetCargoValue() {
        List<Cargo> cargo = List.of(new RedCargo(), new YellowCargo(), new BlueCargo());
        Planets.Planet planet = new Planets.Planet("TestPlanet", cargo);
        
        assertEquals("TestPlanet", planet.getName());
        assertEquals(3, planet.getCargoCount());
        assertEquals(8, planet.getTotalCargoValue()); // Red(4) + Yellow(3) + Blue(1) = 8
    }

    @Test
    @DisplayName("Planet should return defensive copies of cargo")
    void testPlanetDefensiveCopies() {
        List<Cargo> originalCargo = List.of(new RedCargo(), new BlueCargo());
        Planets.Planet planet = new Planets.Planet("TestPlanet", originalCargo);
        
        List<Cargo> retrievedCargo = planet.getGoodsToLoad();
        
        // Should be different instances (defensive copy)
        assertNotSame(originalCargo, retrievedCargo);
        assertEquals(originalCargo.size(), retrievedCargo.size());
    }

    @Test
    @DisplayName("createRandomVariation should create valid planets")
    void testCreateRandomVariation() {
        Planets planets = Planets.createRandomVariation();
        
        assertNotNull(planets);
        assertNotNull(planets.getVariation());
        assertTrue(planets.getFlightDaysCost() >= 1 && planets.getFlightDaysCost() <= 4);
        assertTrue(planets.getPlanets().size() >= 2 && planets.getPlanets().size() <= 4);
    }

    @Test
    @DisplayName("getDescription should include variation information")
    void testGetDescription() {
        Planets planets = new Planets(1, 1, Planets.PlanetVariation.LEVEL2_VARIATION_2);
        String description = planets.getDescription();
        
        assertTrue(description.contains("4 flight days")); // Level 2 Variation 2 cost
        assertTrue(description.contains("LEVEL2_VARIATION_2"));
        assertTrue(description.contains("3 planets")); // Level 2 Variation 2 has 3 planets
    }

    @Test
    @DisplayName("effect method should be callable")
    void testEffectMethod() {
        Planets planets = new Planets();
        
        // Should not throw exception when calling effect
        assertDoesNotThrow(() -> planets.effect(gameModel));
    }

    @Test
    @DisplayName("Level variations should have different characteristics")
    void testVariationDifferences() {
        Planets level1Var1 = new Planets(1, 1, Planets.PlanetVariation.LEVEL1_VARIATION_1);
        Planets level1Var4 = new Planets(1, 1, Planets.PlanetVariation.LEVEL1_VARIATION_4);
        Planets level2Var2 = new Planets(1, 1, Planets.PlanetVariation.LEVEL2_VARIATION_2);
        
        // Different flight day costs
        assertEquals(2, level1Var1.getFlightDaysCost());
        assertEquals(1, level1Var4.getFlightDaysCost()); // Lowest
        assertEquals(4, level2Var2.getFlightDaysCost()); // Highest
        
        // All should have planets
        assertTrue(level1Var1.getPlanets().size() >= 2);
        assertTrue(level1Var4.getPlanets().size() >= 2);
        assertTrue(level2Var2.getPlanets().size() >= 2);
    }

    @Test
    @DisplayName("Landed players map should track landings correctly")
    void testLandedPlayersTracking() {
        Planets planets = new Planets(1, 1, Planets.PlanetVariation.LEVEL1_VARIATION_1);
        
        // Initially empty
        assertTrue(planets.getLandedPlayers().isEmpty());
        
        // The map should be accessible for tracking player landings
        assertNotNull(planets.getLandedPlayers());
    }
}
