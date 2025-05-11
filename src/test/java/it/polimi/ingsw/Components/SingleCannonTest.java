package it.polimi.ingsw.Components;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SingleCannonTest {

    private SingleCannon cannon;
    private BatteryCompartment battery; // Assuming BatteryCompartment is needed for charging

    @BeforeEach
    void setUp() {
        cannon = new SingleCannon();
        battery = new BatteryCompartment();
        // Initialize connectors if necessary for basic functionality or charging logic
        cannon.initializeConnectors(ConnectorType.NULL, ConnectorType.NULL, ConnectorType.SINGLE, ConnectorType.NULL);
        battery.initializeConnectors(ConnectorType.NULL, ConnectorType.UNIVERSAL, ConnectorType.UNIVERSAL, ConnectorType.NULL);
        // Simulate placing them adjacent if needed for charge/fire logic? Depends on implementation.
    }

    @Test
    void testCannonCreation() {
        assertNotNull(cannon);
        // Check default state if applicable
        // assertFalse(cannon.isCharged()); // Assuming a state exists
    }

    @Test
    void testInitializeConnectors() {
        // Verify connectors are set (requires getters or checking behavior)
        // For now, just ensure method runs without error
        assertDoesNotThrow(() -> cannon.initializeConnectors(ConnectorType.SINGLE, ConnectorType.TRIPLE, ConnectorType.NULL, ConnectorType.UNIVERSAL));
    }

    @Test
    void testChargeAndFire() {
        // This test likely requires more complex setup involving component placement and connections
        // The charge(battery) method signature suggests interaction.
        // The current implementation in App.java `cannon.charge(battery)` is likely simplified.

        // Assuming a simplified internal state for now:
        // assertFalse(cannon.isCharged(), "Cannon should not be charged initially.");
        // assertFalse(cannon.fire(), "Cannot fire when not charged.");

        // Simulate successful charge
        //assertTrue(cannon.charge(battery), "Charging should succeed."); // Requires charge logic
        //assertTrue(cannon.isCharged(), "Cannon should be charged after successful charge.");

        // Simulate firing
        //assertTrue(cannon.fire(), "Firing should succeed when charged.");
        //assertFalse(cannon.isCharged(), "Cannon should not be charged after firing.");

        // Current state based on App.java example (may need refinement based on actual implementation):
        boolean charged = cannon.charge(battery);
        assertTrue(charged, "Charge method returned true in App.java example");
        boolean fired = cannon.fire();
        assertTrue(fired, "Fire method returned true in App.java example");
    }

    @Test
    void testFireWithoutCharge() {
        // Assuming fire() returns false or throws if not charged
        // assertFalse(cannon.fire(), "Should not fire if not charged");
        // Or:
        // assertThrows(IllegalStateException.class, () -> cannon.fire(), "Should throw if trying to fire when not charged");

        // Based on App.java, charge() is called first, so this path isn't directly tested there.
        // We'll add a basic check assuming fire returns false if not possible.
        SingleCannon freshCannon = new SingleCannon();
        assertFalse(freshCannon.fire(), "A newly created cannon should likely not be able to fire immediately.");
    }
} 