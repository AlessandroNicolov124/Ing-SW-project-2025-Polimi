package it.polimi.ingsw;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    @Test
    void testPlayerCreation() {
        Player player = new Player(1, "TestPlayer", true);

        assertEquals(1, player.getId());
        assertEquals("TestPlayer", player.getUsername());
        assertTrue(player.isHuman());
        assertNotNull(player.getShip(), "Player ship should be initialized.");
        // Assuming default starting state for resources, hand, etc.
        // assertEquals(0, player.getResources().size()); // Need getters
        // assertTrue(player.getAdventureHand().isEmpty()); // Need getters
    }

    @Test
    void testPlayerCreation_NotHuman() {
        Player player = new Player(2, "AIPlayer", false);

        assertEquals(2, player.getId());
        assertEquals("AIPlayer", player.getUsername());
        assertFalse(player.isHuman());
        assertNotNull(player.getShip());
    }

     // Add more tests if Player class evolves, e.g., for readiness state, resource management, etc.
} 