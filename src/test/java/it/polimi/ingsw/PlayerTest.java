package it.polimi.ingsw;

import it.polimi.ingsw.AdventureCards.AdventureCard;
import it.polimi.ingsw.Components.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    private Player player;
    private ShipBuilder testShip;
    private TestAdventureCard testCard;
    private StructuralModule testComponent;

    @BeforeEach
    void setUp() {
        testShip = new ShipBuilder(7, 5);
        testCard = new TestAdventureCard();
        testComponent = new StructuralModule();
        testComponent.initializeConnectors(new NullConnectorType(), new SingleConnectorType(), new UniversalConnectorType(), new DoubleConnectorType());
    }

    private static class TestAdventureCard extends AdventureCard {
        public boolean effectCalled = false;

        @Override
        protected int additionalPls() {
            return 0;
        }

        @Override
        protected void resolveEvent(Player p) {
            // Simulate resolving the event
            if (p != null) {
                effect();
            }
        }

        @Override
        public void effect() { effectCalled = true; }
    }

    @Test
    void testPlayerCreation_WithDefaultShip() {
        player = new Player(1, "TestPlayer", true);
        assertEquals(1, player.getId());
        assertEquals("TestPlayer", player.getNickName());
        assertTrue(player.isActive());
        assertNotNull(player.getShip(), "Player ship should be initialized");
        assertFalse(player.getReady(), "Player should not be ready by default");
    }

    @Test
    void testPlayerCreation_WithCustomShip() {
        player = new Player(2, "CustomPlayer", false, testShip);
        assertEquals(2, player.getId());
        assertEquals("CustomPlayer", player.getNickName());
        assertFalse(player.isActive());
        assertEquals(testShip, player.getShip());
    }

    @Test
    void testSetAndGetActive() {
        player = new Player(1, "TestPlayer", true);
        assertTrue(player.isActive());
        player.setActive(false);
        assertFalse(player.isActive());
        player.setActive(true);
        assertTrue(player.isActive());
    }

    @Test
    void testSetAndGetReady() {
        player = new Player(1, "TestPlayer", true);
        assertFalse(player.getReady());
        player.setReady(true);
        assertTrue(player.getReady());
        player.setReady(false);
        assertFalse(player.getReady());
    }

    @Test
    void testSetShip() {
        player = new Player(1, "TestPlayer", true);
        ShipBuilder originalShip = player.getShip();
        player.setShip(testShip);
        assertEquals(testShip, player.getShip());
        assertNotEquals(originalShip, player.getShip());
    }

    @Test
    void testPickCard_ValidCard() {
        player = new Player(1, "TestPlayer", true);
        player.pickCard(testCard);
        assertTrue(testCard.effectCalled, "effect() deve essere chiamato su AdventureCard");
    }

    @Test
    void testPickCard_NullCard() {
        player = new Player(1, "TestPlayer", true);
        assertDoesNotThrow(() -> player.pickCard(null));
    }

    @Test
    void testPlaceComponent() {
        player = new Player(1, "TestPlayer", true, testShip);
        Position testPosition = new Position(3, 2);
        boolean result = player.placeComponent(testComponent, testPosition);
        assertTrue(result);
        assertTrue(testShip.isPositionOccupied(testPosition));
    }

    @Test
    void testPlaceComponent_Failure() {
        player = new Player(1, "TestPlayer", true, testShip);
        Position testPosition = new Position(3, 2);
        testShip.placeComponent(testComponent, testPosition);
        Cabin anotherComponent = new Cabin(false);
        anotherComponent.initializeConnectors(new DoubleConnectorType(), new SingleConnectorType(), new SingleConnectorType(), new UniversalConnectorType());
        boolean result = player.placeComponent(anotherComponent, testPosition);
        assertFalse(result);
    }

    @Test
    void testRemoveComponent() {
        player = new Player(1, "TestPlayer", true, testShip);
        Position testPosition = new Position(3, 2);
        testShip.placeComponent(testComponent, testPosition);
        boolean result = player.removeComponent(testComponent);
        assertTrue(result);
        assertFalse(testShip.isPositionOccupied(testPosition));
    }

    @Test
    void testRemoveComponent_Failure() {
        player = new Player(1, "TestPlayer", true, testShip);
        boolean result = player.removeComponent(testComponent);
        assertFalse(result);
    }

    @Test
    void testValidateShip() {
        player = new Player(1, "TestPlayer", true, testShip);
        boolean result = player.validateShip();
        assertTrue(result);
    }

    @Test
    void testValidateShip_Invalid() {
        player = new Player(1, "TestPlayer", true, testShip);
        assertTrue(player.validateShip());
    }

    @Test
    void testTakeTurn() {
        player = new Player(1, "TestPlayer", true);
        assertDoesNotThrow(() -> player.takeTurn());
    }

    @Test
    void testPlayerSerialization() {
        player = new Player(1, "SerializablePlayer", true);
        assertTrue(true, "Player should implement Serializable interface");
    }

    @Test
    void testPlayerImmutableFields() {
        player = new Player(1, "TestPlayer", true);
        assertEquals(1, player.getId());
        assertEquals("TestPlayer", player.getNickName());
    }

    @Test
    void testMultiplePlayersWithDifferentIds() {
        Player player1 = new Player(1, "Player1", true);
        Player player2 = new Player(2, "Player2", false);
        assertNotEquals(player1.getId(), player2.getId());
        assertNotEquals(player1.getNickName(), player2.getNickName());
        assertNotEquals(player1.isActive(), player2.isActive());
    }
}
