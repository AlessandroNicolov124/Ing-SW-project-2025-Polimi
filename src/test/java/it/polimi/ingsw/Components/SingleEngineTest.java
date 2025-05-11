package it.polimi.ingsw.Components;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SingleEngineTest {

    @Test
    void testEngineCreation() {
        SingleEngine engine = new SingleEngine();
        assertNotNull(engine);
        // Test default state/properties if applicable
    }

    @Test
    void testInitializeConnectors() {
        SingleEngine engine = new SingleEngine();
        // Verify connectors are set (requires getters or checking behavior)
        // For now, just ensure method runs without error
        assertDoesNotThrow(() -> engine.initializeConnectors(ConnectorType.SINGLE, ConnectorType.NULL, ConnectorType.NULL, ConnectorType.NULL));
    }
} 