package it.polimi.ingsw.Components;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LifeSupportSystemTest {

    @Test
    void testLifeSupportCreation() {
        LifeSupportSystem lifeSupport = new LifeSupportSystem(LifeSupportSystemType.PURPLE);
        assertNotNull(lifeSupport);
        assertEquals(LifeSupportSystemType.PURPLE, lifeSupport.getType()); // Assuming a getType method
    }

     @Test
    void testInitializeConnectors() {
        LifeSupportSystem lifeSupport = new LifeSupportSystem(LifeSupportSystemType.PURPLE);
        // Verify connectors are set (requires getters or checking behavior)
        // For now, just ensure method runs without error
        assertDoesNotThrow(() -> lifeSupport.initializeConnectors(ConnectorType.UNIVERSAL, ConnectorType.NULL, ConnectorType.NULL, ConnectorType.UNIVERSAL));
    }
} 