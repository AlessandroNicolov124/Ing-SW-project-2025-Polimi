package it.polimi.ingsw.Components;

import it.polimi.ingsw.Components.BatteryType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for BatteryCompartment component
 */
class BatteryCompartmentTest {

    private BatteryCompartment batteryCompartment;

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Default constructor should create battery compartment with default values")
        void testDefaultConstructor() {
            BatteryCompartment battery = new BatteryCompartment();
            assertNotNull(battery);
            // I valori dovrebbero essere inizializzati a 0/null dal costruttore di default
            assertEquals(0, battery.getMaxCapacity());
            assertEquals(0, battery.getChargeLevel());
            assertNull(battery.getType());
        }

        @Test
        @DisplayName("Parameterized constructor should initialize all fields correctly")
        void testParameterizedConstructor() {
            BatteryCompartment battery2 = new BatteryCompartment(2, BatteryType.DOUBLE, 0);
            BatteryCompartment battery3 = new BatteryCompartment(3, BatteryType.TRIPLE, 0);

            assertNotNull(battery2);
            assertEquals(2, battery2.getMaxCapacity());
            assertEquals(BatteryType.DOUBLE, battery2.getType());
            assertEquals(0, battery2.getChargeLevel());

            assertNotNull(battery3);
            assertEquals(3, battery3.getMaxCapacity());
            assertEquals(BatteryType.TRIPLE, battery3.getType());
            assertEquals(0, battery3.getChargeLevel());
        }

        @Test
        @DisplayName("Constructor should handle zero values correctly")
        void testConstructorWithZeroValues() {
            BatteryCompartment battery = new BatteryCompartment(0, BatteryType.DOUBLE, 0);

            assertEquals(0, battery.getMaxCapacity());
            assertEquals(BatteryType.DOUBLE, battery.getType());
            assertEquals(0, battery.getChargeLevel());
        }

        @Test
        @DisplayName("Constructor should handle maximum charge equal to capacity")
        void testConstructorWithFullCharge() {
            BatteryCompartment battery = new BatteryCompartment(3, BatteryType.TRIPLE, 3);

            assertEquals(3, battery.getMaxCapacity());
            assertEquals(3, battery.getChargeLevel());
        }
    }

    @Nested
    @DisplayName("Getter Tests")
    class GetterTests {

        @BeforeEach
        void setUp() {
            batteryCompartment = new BatteryCompartment(3, BatteryType.TRIPLE, 2);
        }

        @Test
        @DisplayName("getMaxCapacity should return correct capacity")
        void testGetMaxCapacity() {
            assertEquals(3, batteryCompartment.getMaxCapacity());
        }

        @Test
        @DisplayName("getType should return correct battery type")
        void testGetType() {
            assertEquals(BatteryType.TRIPLE, batteryCompartment.getType());
        }

        @Test
        @DisplayName("getChargeLevel should return correct charge level")
        void testGetChargeLevel() {
            assertEquals(2, batteryCompartment.getChargeLevel());
        }
    }

    @Nested
    @DisplayName("Component String Tests")
    class ComponentStringTests {

        @Test
        @DisplayName("getComponentString should return correct format with DOUBLE battery")
        void testGetComponentStringWithDoubleBattery() {
            BatteryCompartment battery = new BatteryCompartment(2, BatteryType.DOUBLE, 1);
            String result = battery.getComponentString();

            assertTrue(result.startsWith("batterycompartment_double_"));
            assertTrue(result.contains("double"));
        }

        @Test
        @DisplayName("getComponentString should return correct format with TRIPLE battery")
        void testGetComponentStringWithTripleBattery() {
            BatteryCompartment battery = new BatteryCompartment(3, BatteryType.TRIPLE, 0);
            String result = battery.getComponentString();

            assertTrue(result.startsWith("batterycompartment_triple_"));
            assertTrue(result.contains("triple"));
        }

        @Test
        @DisplayName("getComponentString should be all lowercase")
        void testGetComponentStringLowercase() {
            BatteryCompartment battery = new BatteryCompartment(3, BatteryType.TRIPLE, 0);
            String result = battery.getComponentString();

            assertEquals(result, result.toLowerCase());
        }
    }

    @Nested
    @DisplayName("Recharge Tests")
    class RechargeTests {

        @BeforeEach
        void setUp() {
            batteryCompartment = new BatteryCompartment(2, BatteryType.DOUBLE, 1);
        }

        @Test
        @DisplayName("recharge() should fully charge the battery")
        void testFullRecharge() {
            batteryCompartment.recharge();
            assertEquals(2, batteryCompartment.getChargeLevel());
            assertEquals(batteryCompartment.getMaxCapacity(), batteryCompartment.getChargeLevel());
        }

        @Test
        @DisplayName("recharge() should work when battery is empty")
        void testRechargeFromEmpty() {
            BatteryCompartment emptyBattery = new BatteryCompartment(2, BatteryType.DOUBLE, 0);
            emptyBattery.recharge();
            assertEquals(0, emptyBattery.getChargeLevel());
        }

        @Test
        @DisplayName("recharge() should work when battery is already full")
        void testRechargeWhenFull() {
            BatteryCompartment fullBattery = new BatteryCompartment(3, BatteryType.TRIPLE, 3);
            fullBattery.recharge();
            assertEquals(3, fullBattery.getChargeLevel());
        }

        @Test
        @DisplayName("recharge(amount) should recharge by specified amount")
        void testPartialRecharge() {
            int recharged = batteryCompartment.recharge(3);

            assertEquals(3, recharged);
            assertEquals(8, batteryCompartment.getChargeLevel());
        }

        @Test
        @DisplayName("recharge(amount) should not exceed maximum capacity")
        void testRechargeExceedsCapacity() {
            int recharged = batteryCompartment.recharge(10); // Try to recharge more than space available

            assertEquals(2, recharged); // Should only recharge what's possible
            assertEquals(2, batteryCompartment.getChargeLevel()); // Should be at max capacity
        }

        @Test
        @DisplayName("recharge(amount) should return 0 for zero amount")
        void testRechargeZeroAmount() {
            int originalCharge = batteryCompartment.getChargeLevel();
            int recharged = batteryCompartment.recharge(0);

            assertEquals(0, recharged);
            assertEquals(originalCharge, batteryCompartment.getChargeLevel());
        }

        @Test
        @DisplayName("recharge(amount) should work when battery is full")
        void testRechargeAmountWhenFull() {
            batteryCompartment.recharge(); // Fill to capacity
            int recharged = batteryCompartment.recharge(2);

            assertEquals(0, recharged);
            assertEquals(2, batteryCompartment.getChargeLevel());
        }
    }

    @Nested
    @DisplayName("Discharge Tests")
    class DischargeTests {

        @BeforeEach
        void setUp() {
            batteryCompartment = new BatteryCompartment(3, BatteryType.TRIPLE, 2);
        }

        @Test
        @DisplayName("discharge should remove specified amount of charge")
        void testDischarge() {
            int discharged = batteryCompartment.discharge(2);

            assertEquals(2, discharged);
            assertEquals(0, batteryCompartment.getChargeLevel());
        }

        @Test
        @DisplayName("discharge should not go below zero")
        void testDischargeExceedsCapacity() {
            int discharged = batteryCompartment.discharge(10); // Try to discharge more than available

            assertEquals(2, discharged); // Should only discharge what's available
            assertEquals(0, batteryCompartment.getChargeLevel());
        }

        @Test
        @DisplayName("discharge should return 0 for zero amount")
        void testDischargeZeroAmount() {
            int originalCharge = batteryCompartment.getChargeLevel();
            int discharged = batteryCompartment.discharge(0);

            assertEquals(0, discharged);
            assertEquals(originalCharge, batteryCompartment.getChargeLevel());
        }

        @Test
        @DisplayName("discharge should work when battery is empty")
        void testDischargeWhenEmpty() {
            BatteryCompartment emptyBattery = new BatteryCompartment(2, BatteryType.DOUBLE, 0);
            int discharged = emptyBattery.discharge(5);

            assertEquals(0, discharged);
            assertEquals(0, emptyBattery.getChargeLevel());
        }

        @Test
        @DisplayName("discharge should completely drain battery")
        void testDischargeCompletely() {
            int discharged = batteryCompartment.discharge(2);

            assertEquals(2, discharged);
            assertEquals(0, batteryCompartment.getChargeLevel());
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @BeforeEach
        void setUp() {
            batteryCompartment = new BatteryCompartment(3, BatteryType.TRIPLE, 0);
        }

        @Test
        @DisplayName("Multiple recharge and discharge operations should work correctly")
        void testMultipleOperations() {
            // Start empty
            assertEquals(0, batteryCompartment.getChargeLevel());

            // Recharge partially
            int recharged1 = batteryCompartment.recharge(2);
            assertEquals(2, recharged1);
            assertEquals(2, batteryCompartment.getChargeLevel());

            // Discharge partially
            int discharged1 = batteryCompartment.discharge(1);
            assertEquals(1, discharged1);
            assertEquals(1, batteryCompartment.getChargeLevel());

            // Recharge to full
            batteryCompartment.recharge();
            assertEquals(3, batteryCompartment.getChargeLevel());

            // Discharge completely
            int discharged2 = batteryCompartment.discharge(3);
            assertEquals(3, discharged2);
            assertEquals(0, batteryCompartment.getChargeLevel());
        }

        @Test
        @DisplayName("Recharge and discharge should handle edge cases together")
        void testEdgeCases() {
            // Try to discharge from empty
            assertEquals(0, batteryCompartment.discharge(5));
            assertEquals(0, batteryCompartment.getChargeLevel());

            // Recharge to full
            batteryCompartment.recharge();
            assertEquals(10, batteryCompartment.getChargeLevel());

            // Try to recharge when full
            assertEquals(0, batteryCompartment.recharge(5));
            assertEquals(3, batteryCompartment.getChargeLevel());

            // Try to discharge more than available
            assertEquals(3, batteryCompartment.discharge(15));
            assertEquals(0, batteryCompartment.getChargeLevel());
        }

        @Test
        @DisplayName("Battery should maintain consistency across operations")
        void testConsistency() {
            int initialCapacity = batteryCompartment.getMaxCapacity();

            // Perform various operations
            batteryCompartment.recharge(3);
            batteryCompartment.discharge(1);
            batteryCompartment.recharge();
            batteryCompartment.discharge(5);

            // Capacity should never change
            assertEquals(initialCapacity, batteryCompartment.getMaxCapacity());

            // Charge should never exceed capacity or go below 0
            assertTrue(batteryCompartment.getChargeLevel() >= 0);
            assertTrue(batteryCompartment.getChargeLevel() <= batteryCompartment.getMaxCapacity());
        }

        @Test
        @DisplayName("Different battery types should work the same way")
        void testDifferentBatteryTypes() {
            BatteryCompartment smallBattery = new BatteryCompartment(2, BatteryType.DOUBLE, 1);
            BatteryCompartment largeBattery = new BatteryCompartment(3, BatteryType.TRIPLE, 1);

            // Both should recharge correctly
            assertEquals(1, smallBattery.recharge(5));
            assertEquals(2, smallBattery.getChargeLevel());

            assertEquals(2, largeBattery.recharge(15));
            assertEquals(3, largeBattery.getChargeLevel());

            // Both should discharge correctly
            assertEquals(2, smallBattery.discharge(2));
            assertEquals(3, largeBattery.discharge(15));
        }
    }
}