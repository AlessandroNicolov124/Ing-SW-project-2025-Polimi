package it.polimi.ingsw.Components;

import it.polimi.ingsw.Direction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

class EngineTest {

    /**
     * Test class for SingleEngine component
     */
    @Nested
    class SingleEngineTest {

        private SingleEngine singleEngine;

        @BeforeEach
        void setUp() {
            singleEngine = new SingleEngine();
        }

        @Test
        @DisplayName("Constructor should initialize SingleEngine with correct values")
        void testConstructor() {
            assertNotNull(singleEngine);
            // Assumendo che la classe base abbia getPower() e getEnergyConsumption()
            assertEquals(1, singleEngine.getPower());
            assertEquals(0, singleEngine.getEnergyConsumption());
        }

        @Test
        @DisplayName("getComponentString should return correct format")
        void testGetComponentString() {
            String result = singleEngine.getComponentString();
            assertTrue(result.startsWith("singleengine_"));
            // Il test completo dipende dall'implementazione di getConnectionsString()
        }

        @Test
        @DisplayName("getEffectivePower should return full power when not damaged")
        void testGetEffectivePowerWhenNotDamaged() {
            int expectedPower = singleEngine.getPower();
            assertEquals(expectedPower, singleEngine.getEffectivePower());
        }

        @Test
        @DisplayName("getEffectivePower should return 0 when damaged")
        void testGetEffectivePowerWhenDamaged() {
            // Assumendo che esista un metodo per impostare il danno
            singleEngine.setDamaged(true);
            assertEquals(0, singleEngine.getEffectivePower());
        }

        @Test
        @DisplayName("getEffectivePower should consistently return same value when not damaged")
        void testGetEffectivePowerConsistency() {
            int power1 = singleEngine.getEffectivePower();
            int power2 = singleEngine.getEffectivePower();
            assertEquals(power1, power2);
            assertEquals(singleEngine.getPower(), power1);
        }
    }

    /**
     * Test class for DoubleEngine component
     */
    @Nested
    class DoubleEngineTest {

        private DoubleEngine doubleEngine;
        private BatteryCompartment mockBattery;

        @BeforeEach
        void setUp() {
            doubleEngine = new DoubleEngine();
            mockBattery = mock(BatteryCompartment.class);
        }

        @Test
        @DisplayName("Constructor should initialize DoubleEngine with correct values")
        void testConstructor() {
            assertNotNull(doubleEngine);
            assertEquals(2, doubleEngine.getPower());
            assertEquals(1, doubleEngine.getEnergyConsumption());
            assertFalse(doubleEngine.isCharge());
        }

        @Test
        @DisplayName("getComponentString should return correct format in lowercase")
        void testGetComponentString() {
            String result = doubleEngine.getComponentString();
            assertTrue(result.startsWith("doubleengine_"));
            // Verifica che sia tutto in minuscolo
            assertEquals(result, result.toLowerCase());
        }

        @Test
        @DisplayName("getEffectivePower should return 0 when damaged")
        void testGetEffectivePowerWhenDamaged() {
            doubleEngine.setDamaged(true);
            assertEquals(0, doubleEngine.getEffectivePower());
        }

        @Test
        @DisplayName("getEffectivePower should return full power when charged")
        void testGetEffectivePowerWhenCharged() {
            // Carichiamo il motore
            when(mockBattery.getChargeLevel()).thenReturn(2);
            when(mockBattery.discharge(1)).thenReturn(1);

            assertTrue(doubleEngine.charge(mockBattery));

            int expectedPower = doubleEngine.getPower();
            assertEquals(expectedPower, doubleEngine.getEffectivePower());
        }

        @Test
        @DisplayName("getEffectivePower should return half power when not charged")
        void testGetEffectivePowerWhenNotCharged() {
            int expectedPower = doubleEngine.getPower() / 2;
            assertEquals(expectedPower, doubleEngine.getEffectivePower());
        }

        @Test
        @DisplayName("charge should return false with null battery")
        void testChargeWithNullBattery() {
            assertFalse(doubleEngine.charge(null));
            assertFalse(doubleEngine.isCharge());
        }

        @Test
        @DisplayName("charge should return false when damaged")
        void testChargeWhenDamaged() {
            doubleEngine.setDamaged(true);
            when(mockBattery.getChargeLevel()).thenReturn(2);

            assertFalse(doubleEngine.charge(mockBattery));
            assertFalse(doubleEngine.isCharge());
        }

        @Test
        @DisplayName("charge should return false when already charged")
        void testChargeWhenAlreadyCharged() {
            // Prima carica
            when(mockBattery.getChargeLevel()).thenReturn(2);
            when(mockBattery.discharge(1)).thenReturn(1);

            assertTrue(doubleEngine.charge(mockBattery));
            assertTrue(doubleEngine.isCharge());

            // Seconda carica dovrebbe fallire
            assertFalse(doubleEngine.charge(mockBattery));
        }

        @Test
        @DisplayName("charge should return false when battery has insufficient charge")
        void testChargeWithInsufficientBattery() {
            when(mockBattery.getChargeLevel()).thenReturn(0);

            assertFalse(doubleEngine.charge(mockBattery));
            assertFalse(doubleEngine.isCharge());
        }

        @Test
        @DisplayName("charge should return true with sufficient battery")
        void testChargeWithSufficientBattery() {
            when(mockBattery.getChargeLevel()).thenReturn(2);
            when(mockBattery.discharge(1)).thenReturn(1);

            assertTrue(doubleEngine.charge(mockBattery));
            assertTrue(doubleEngine.isCharge());

            verify(mockBattery).discharge(1);
        }

        @Test
        @DisplayName("charge should return false when battery discharge fails")
        void testChargeWhenBatteryDischargeFails() {
            when(mockBattery.getChargeLevel()).thenReturn(2);
            when(mockBattery.discharge(1)).thenReturn(0); // Simulate discharge failure

            assertFalse(doubleEngine.charge(mockBattery));
            assertFalse(doubleEngine.isCharge());
        }

        @Test
        @DisplayName("discharge should set isCharged to false")
        void testDischarge() {
            // Prima carichiamo
            when(mockBattery.getChargeLevel()).thenReturn(2);
            when(mockBattery.discharge(1)).thenReturn(1);
            doubleEngine.charge(mockBattery);

            assertTrue(doubleEngine.isCharge());

            // Poi scarichiamo
            doubleEngine.discharge();
            assertFalse(doubleEngine.isCharge());
        }

        @Test
        @DisplayName("discharge should work even when not charged")
        void testDischargeWhenNotCharged() {
            assertFalse(doubleEngine.isCharge());

            doubleEngine.discharge();
            assertFalse(doubleEngine.isCharge()); // Should remain false
        }

        @Test
        @DisplayName("isCharge should return correct charge state")
        void testIsCharge() {
            // Initially not charged
            assertFalse(doubleEngine.isCharge());

            // After charging
            when(mockBattery.getChargeLevel()).thenReturn(2);
            when(mockBattery.discharge(1)).thenReturn(1);
            doubleEngine.charge(mockBattery);
            assertTrue(doubleEngine.isCharge());

            // After discharging
            doubleEngine.discharge();
            assertFalse(doubleEngine.isCharge());
        }

        @Test
        @DisplayName("calculatePowerUsingBattery should return full power when using battery")
        void testCalculatePowerUsingBatteryWithBattery() {
            int powerWithBattery = doubleEngine.calculatePowerUsingBattery(true);
            int expectedPower = doubleEngine.getPower();
            assertEquals(expectedPower, powerWithBattery);
        }

        @Test
        @DisplayName("calculatePowerUsingBattery should return half power when not using battery")
        void testCalculatePowerUsingBatteryWithoutBattery() {
            int powerWithoutBattery = doubleEngine.calculatePowerUsingBattery(false);
            int expectedPower = doubleEngine.getPower() / 2;
            assertEquals(expectedPower, powerWithoutBattery);
        }

        @Test
        @DisplayName("calculatePowerUsingBattery should return 0 when damaged")
        void testCalculatePowerUsingBatteryWhenDamaged() {
            doubleEngine.setDamaged(true);

            assertEquals(0, doubleEngine.calculatePowerUsingBattery(true));
            assertEquals(0, doubleEngine.calculatePowerUsingBattery(false));
        }

        @Test
        @DisplayName("Power calculations should be consistent between methods")
        void testPowerCalculationConsistency() {
            // Test when not charged
            int effectivePower = doubleEngine.getEffectivePower();
            int calculatedPowerNoBattery = doubleEngine.calculatePowerUsingBattery(false);
            assertEquals(effectivePower, calculatedPowerNoBattery);

            // Test when charged
            when(mockBattery.getChargeLevel()).thenReturn(2);
            when(mockBattery.discharge(1)).thenReturn(1);
            doubleEngine.charge(mockBattery);

            int effectivePowerCharged = doubleEngine.getEffectivePower();
            int calculatedPowerWithBattery = doubleEngine.calculatePowerUsingBattery(true);
            assertEquals(effectivePowerCharged, calculatedPowerWithBattery);
        }

        @Test
        @DisplayName("Charge state should affect power calculations correctly")
        void testChargeStateEffectOnPower() {
            int basePower = doubleEngine.getPower();

            // Not charged - should be half power
            assertEquals(basePower / 2, doubleEngine.getEffectivePower());

            // Charge the engine
            when(mockBattery.getChargeLevel()).thenReturn(2);
            when(mockBattery.discharge(1)).thenReturn(1);
            doubleEngine.charge(mockBattery);

            // Charged - should be full power
            assertEquals(basePower, doubleEngine.getEffectivePower());

            // Discharge
            doubleEngine.discharge();

            // Not charged again - should be half power
            assertEquals(basePower / 2, doubleEngine.getEffectivePower());
        }
    }
}