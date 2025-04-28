package it.polimi.ingsw.Components;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BatteryCompartmentTest {

    @Test
    void testBatteryCreation() {
        BatteryCompartment battery = new BatteryCompartment();
        assertNotNull(battery);
        // Test default state/properties if applicable (e.g., charge level)
    }

    @Test
    void testInitializeConnectors() {
        BatteryCompartment battery = new BatteryCompartment();
        // Verify connectors are set (requires getters or checking behavior)
        // For now, just ensure method runs without error
        assertDoesNotThrow(() -> battery.initializeConnectors(ConnectorType.NULL, ConnectorType.UNIVERSAL, ConnectorType.UNIVERSAL, ConnectorType.NULL));
    }
} 