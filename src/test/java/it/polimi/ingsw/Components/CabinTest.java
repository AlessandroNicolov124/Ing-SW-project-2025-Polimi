package it.polimi.ingsw.Components;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CabinTest {

    @Test
    void testCabinCreation_Starting() {
        Cabin cabin = new Cabin(true);
        assertNotNull(cabin);
        assertTrue(cabin.isStartingCabin());
        // Test default connectors if applicable or needed
    }

    @Test
    void testCabinCreation_NotStarting() {
        Cabin cabin = new Cabin(false);
        assertNotNull(cabin);
        assertFalse(cabin.isStartingCabin());
    }

    @Test
    void testInitializeConnectors() {
        Cabin cabin = new Cabin(true);
        // Verify connectors are set (requires getters or checking behavior)
        // For now, just ensure method runs without error
        assertDoesNotThrow(() -> cabin.initializeConnectors(ConnectorType.SINGLE, ConnectorType.TRIPLE, ConnectorType.NULL, ConnectorType.UNIVERSAL));
    }
} 