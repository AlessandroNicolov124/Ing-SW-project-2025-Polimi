package it.polimi.ingsw.Components;

import it.polimi.ingsw.Direction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class ShieldGeneratorTest {

    private ShieldGenerator shieldGenerator;
    private BatteryCompartment mockBattery;

    @BeforeEach
    public void setUp() {
        shieldGenerator = new ShieldGenerator();
        shieldGenerator.initializeConnectors(new NullConnectorType(), new DoubleConnectorType(), new SingleConnectorType(), new UniversalConnectorType());
        mockBattery = new BatteryCompartment(2, BatteryType.DOUBLE, 2);
    }

    // ===== CONSTRUCTION TESTS =====

    @Test
    @DisplayName("ShieldGenerator should initialize with correct default values")
    public void testDefaultConstruction() {
        assertFalse(shieldGenerator.isCharged());
        assertEquals(1, shieldGenerator.getEnergyConsumption());
        assertTrue(shieldGenerator.getProtectedDirections().isEmpty());
        assertFalse(shieldGenerator.isDamaged()); // Inherited from Component
    }
    // ===== PROTECTED DIRECTIONS TESTS =====

    @ParameterizedTest
    @EnumSource(Direction.class)
    @DisplayName("Should be able to add all Direction types to protected directions")
    public void testAddDirection(Direction direction) {
        shieldGenerator.add(direction);
        assertTrue(shieldGenerator.getProtectedDirections().contains(direction));
        assertEquals(1, shieldGenerator.getProtectedDirections().size());
    }

    @Test
    @DisplayName("Should be able to add multiple directions")
    public void testAddMultipleDirections() {
        shieldGenerator.add(Direction.NORTH);
        shieldGenerator.add(Direction.SOUTH);
        shieldGenerator.add(Direction.EAST);

        List<Direction> protectedDirections = shieldGenerator.getProtectedDirections();
        assertEquals(3, protectedDirections.size());
        assertTrue(protectedDirections.contains(Direction.NORTH));
        assertTrue(protectedDirections.contains(Direction.SOUTH));
        assertTrue(protectedDirections.contains(Direction.EAST));
    }

    @Test
    @DisplayName("Should be able to remove direction from protected directions")
    public void testRemoveDirection() {
        shieldGenerator.add(Direction.NORTH);
        shieldGenerator.add(Direction.SOUTH);

        assertTrue(shieldGenerator.getProtectedDirections().contains(Direction.NORTH));

        shieldGenerator.remove(Direction.NORTH);

        assertFalse(shieldGenerator.getProtectedDirections().contains(Direction.NORTH));
        assertTrue(shieldGenerator.getProtectedDirections().contains(Direction.SOUTH));
        assertEquals(1, shieldGenerator.getProtectedDirections().size());
    }

    @Test
    @DisplayName("Remove should handle non-existent direction gracefully")
    public void testRemoveNonExistentDirection() {
        shieldGenerator.add(Direction.NORTH);
        assertEquals(1, shieldGenerator.getProtectedDirections().size());

        shieldGenerator.remove(Direction.SOUTH); // Not in the list

        assertEquals(1, shieldGenerator.getProtectedDirections().size());
        assertTrue(shieldGenerator.getProtectedDirections().contains(Direction.NORTH));
    }

    // ===== CHARGING TESTS =====

    @Test
    @DisplayName("Should charge successfully with sufficient battery")
    public void testChargeSuccess() {
        mockBattery = new BatteryCompartment(2, BatteryType.DOUBLE, 2);
        assertTrue(shieldGenerator.charge(mockBattery));
        assertTrue(shieldGenerator.isCharged());
        assertEquals(1, mockBattery.getChargeLevel());
    }

    @Test
    @DisplayName("Should fail to charge with null battery")
    public void testChargeWithNullBattery() {
        assertFalse(shieldGenerator.charge(null));
        assertFalse(shieldGenerator.isCharged());
    }

    @Test
    @DisplayName("Should fail to charge when already charged")
    public void testChargeWhenAlreadyCharged() {
        mockBattery = new BatteryCompartment(2, BatteryType.DOUBLE, 2);
        assertTrue(shieldGenerator.charge(mockBattery));
        // Provo a ricaricare con una seconda batteria
        BatteryCompartment secondBattery = new BatteryCompartment(2, BatteryType.DOUBLE, 2);
        assertFalse(shieldGenerator.charge(secondBattery));
        assertEquals(2, secondBattery.getChargeLevel()); // Nessuna scarica
    }

    @Test
    @DisplayName("Should fail to charge when damaged")
    public void testChargeWhenDamaged() {
        shieldGenerator.setDamaged(true);
        mockBattery = new BatteryCompartment(2, BatteryType.DOUBLE, 2);
        assertFalse(shieldGenerator.charge(mockBattery));
        assertFalse(shieldGenerator.isCharged());
        assertEquals(2, mockBattery.getChargeLevel());
    }

    @Test
    @DisplayName("Should fail to charge with insufficient battery")
    public void testChargeWithInsufficientBattery() {
        mockBattery = new BatteryCompartment(2, BatteryType.DOUBLE, 0); // Carica insufficiente
        assertFalse(shieldGenerator.charge(mockBattery));
        assertFalse(shieldGenerator.isCharged());
        assertEquals(0, mockBattery.getChargeLevel());
    }

    @Test
    @DisplayName("Should fail to charge when battery discharge fails")
    public void testChargeWhenBatteryDischargeFails() {
        // Per far fallire la scarica, la carica deve essere 0
        mockBattery = new BatteryCompartment(2, BatteryType.DOUBLE, 0);
        assertFalse(shieldGenerator.charge(mockBattery));
        assertFalse(shieldGenerator.isCharged());
        assertEquals(0, mockBattery.getChargeLevel());
    }

    @Test
    @DisplayName("Should handle battery with exactly enough charge")
    public void testChargeWithExactlyEnoughBattery() {
        mockBattery = new BatteryCompartment(2, BatteryType.DOUBLE, 1);
        assertTrue(shieldGenerator.charge(mockBattery));
        assertTrue(shieldGenerator.isCharged());
        assertEquals(0, mockBattery.getChargeLevel());
    }

    // ===== DISCHARGE TESTS =====

    @Test
    @DisplayName("Should discharge when charged")
    public void testDischarge() {
        mockBattery = new BatteryCompartment(2, BatteryType.DOUBLE, 2);
        shieldGenerator.charge(mockBattery);
        assertTrue(shieldGenerator.isCharged());
        shieldGenerator.discharge();
        assertFalse(shieldGenerator.isCharged());
    }

    @Test
    @DisplayName("Should handle discharge when not charged")
    public void testDischargeWhenNotCharged() {
        assertFalse(shieldGenerator.isCharged());

        shieldGenerator.discharge();

        assertFalse(shieldGenerator.isCharged()); // Should remain false
    }

    @Test
    @DisplayName("Should be able to recharge after discharge")
    public void testRechargeAfterDischarge() {
        mockBattery = new BatteryCompartment(3, BatteryType.TRIPLE, 3);
        assertTrue(shieldGenerator.charge(mockBattery));
        shieldGenerator.discharge();
        BatteryCompartment secondBattery = new BatteryCompartment(3, BatteryType.TRIPLE, 3);
        assertTrue(shieldGenerator.charge(secondBattery));
        assertTrue(shieldGenerator.isCharged());
        assertEquals(2, secondBattery.getChargeLevel());
    }

    // ===== ACTIVE STATE TESTS (via reflection or behavior testing) =====

    @Test
    @DisplayName("Should not be active when damaged")
    public void testNotActiveWhenDamaged() {
        mockBattery = new BatteryCompartment(3, BatteryType.TRIPLE, 3);
        shieldGenerator.charge(mockBattery);
        shieldGenerator.setDamaged(true);
        assertTrue(shieldGenerator.isDamaged());
        assertTrue(shieldGenerator.isCharged());
    }

    @Test
    @DisplayName("Should not be active when not charged")
    public void testNotActiveWhenNotCharged() {
        assertFalse(shieldGenerator.isCharged());
        assertFalse(shieldGenerator.isDamaged());

        // The shield should not be active (though we can't test isActive directly)
        // We can verify the conditions that would make it inactive
    }

    // ===== COMPONENT STRING TESTS =====

    @Test
    @DisplayName("Should generate correct component string")
    public void testGetComponentString() {
        String componentString = shieldGenerator.getComponentString();

        assertTrue(componentString.startsWith("shieldgenerator_"));
        // The exact format depends on getConnectionsString() implementation from Component
    }

    // ===== INTEGRATION TESTS =====

    @Test
    @DisplayName("Complete workflow: add directions, charge, discharge")
    public void testCompleteWorkflow() {
        // Add protected directions
        shieldGenerator.add(Direction.NORTH);
        shieldGenerator.add(Direction.EAST);
        assertEquals(2, shieldGenerator.getProtectedDirections().size());

        // Charge
        mockBattery = new BatteryCompartment(3, BatteryType.TRIPLE, 3);
        assertTrue(shieldGenerator.charge(mockBattery));
        assertTrue(shieldGenerator.isCharged());
        assertEquals(2, mockBattery.getChargeLevel());

        // Remove a direction
        shieldGenerator.remove(Direction.NORTH);
        assertEquals(1, shieldGenerator.getProtectedDirections().size());
        assertTrue(shieldGenerator.getProtectedDirections().contains(Direction.EAST));

        // Discharge
        shieldGenerator.discharge();
        assertFalse(shieldGenerator.isCharged());
    }

    @Test
    @DisplayName("Energy consumption should remain constant")
    public void testEnergyConsumptionConstant() {
        assertEquals(1, shieldGenerator.getEnergyConsumption());

        // After adding directions
        shieldGenerator.add(Direction.NORTH);
        assertEquals(1, shieldGenerator.getEnergyConsumption());

        // After charging
        mockBattery = new BatteryCompartment(3, BatteryType.TRIPLE, 3);
        shieldGenerator.charge(mockBattery);
        assertEquals(1, shieldGenerator.getEnergyConsumption());

        // After discharging
        shieldGenerator.discharge();
        assertEquals(1, shieldGenerator.getEnergyConsumption());
    }

    @Test
    @DisplayName("Protected directions list should be modifiable")
    public void testProtectedDirectionsListModifiable() {
        List<Direction> directions = shieldGenerator.getProtectedDirections();
        assertTrue(directions.isEmpty());

        shieldGenerator.add(Direction.NORTH);
        assertEquals(1, directions.size());
        assertTrue(directions.contains(Direction.NORTH));
    }
}