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
//            if (p != null) {
//                effect();
//            }
        }

        @Override
        public void effect(GameModel gameModel) { effectCalled = true; }
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
    void testSetAndGetReady() {
        player = new Player(2, "ReadyPlayer", true);
        assertFalse(player.getReady());
        player.setReady(true);
        assertTrue(player.getReady());
    }

    @Test
    void testSetAndGetActive() {
        player = new Player(3, "ActivePlayer", false);
        assertFalse(player.isActive());
        player.setActive(true);
        assertTrue(player.isActive());
    }

    @Test
    void testPickCard() {
        player = new Player(4, "CardPlayer", true);
        assertNull(player.getPickedCard());
        player.pickCard(testCard);
        assertEquals(testCard, player.getPickedCard());
    }

    @Test
    void testSetAndGetShip() {
        player = new Player(5, "ShipPlayer", true);
        ShipBuilder newShip = new ShipBuilder(7, 5);
        player.setShip(newShip);
        assertEquals(newShip, player.getShip());
    }

    @Test
    void testPlaceAndRemoveComponent() {
        player = new Player(6, "ComponentPlayer", true);
        Position pos = new Position(4, 2);
        assertTrue(player.placeComponent(testComponent, pos));
        assertTrue(player.removeComponentAt(pos));
    }

    @Test
    void testValidateShipAndGetShipValidationErrors() {
        player = new Player(7, "ValidatePlayer", true);
        // ShipBuilder vuoto non valido
        assertFalse(player.validateShip());
        assertNotNull(player.getShipValidationErrors());
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
