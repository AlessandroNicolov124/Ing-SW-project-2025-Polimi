package it.polimi.ingsw.Components;

import it.polimi.ingsw.Direction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

class CannonTest {

    /**
     * Test class for SingleCannon component
     */
    @Nested
    class SingleCannonTest {

        private SingleCannon singleCannon;

        @BeforeEach
        void setUp() {
            singleCannon = new SingleCannon();
            singleCannon.initializeConnectors(new NullConnectorType(), new DoubleConnectorType(), new SingleConnectorType(), new UniversalConnectorType());
        }

        @Test
        @DisplayName("Constructor should initialize SingleCannon with correct values")
        void testConstructor() {
            assertNotNull(singleCannon);
            // Assumendo che la classe base abbia getFirePower() e getEnergyConsumption()
            assertEquals(1, singleCannon.getFirePower());
            assertEquals(0, singleCannon.getEnergyConsumption());
        }

        @Test
        @DisplayName("getComponentString should return correct format")
        void testGetComponentString() {
            String result = singleCannon.getComponentString();
            assertTrue(result.startsWith("singlecannon_"));
            // Il test completo dipende dall'implementazione di getConnectionsString()
        }

        @Test
        @DisplayName("fire should return true when not damaged")
        void testFireWhenNotDamaged() {
            // Assumendo che damaged sia inizialmente false
            assertTrue(singleCannon.fire());
        }

        @Test
        @DisplayName("fire should return false when damaged")
        void testFireWhenDamaged() {
            // Assumendo che esista un metodo per impostare il danno
            singleCannon.setDamaged(true);
            assertFalse(singleCannon.fire());
        }

        @Test
        @DisplayName("getEffectivePower should return 0 when damaged")
        void testGetEffectivePowerWhenDamaged() {
            singleCannon.setDamaged(true);
            assertEquals(0.0, singleCannon.getEffectivePower());
        }

        @Test
        @DisplayName("getEffectivePower should return full power when facing north")
        void testGetEffectivePowerFacingNorth() {
            // Assumendo che esista un metodo per impostare la direzione
            singleCannon.setComponentDirection(Direction.NORTH);
            double expectedPower = singleCannon.getFirePower();
            assertEquals(expectedPower, singleCannon.getEffectivePower());
        }

        @Test
        @DisplayName("getEffectivePower should return half power when not facing north")
        void testGetEffectivePowerNotFacingNorth() {
            singleCannon.setComponentDirection(Direction.SOUTH);
            double expectedPower = singleCannon.getFirePower() / 2.0;
            assertEquals(expectedPower, singleCannon.getEffectivePower());
        }
    }

    /**
     * Test class for DoubleCannon component
     */
    @Nested
    class DoubleCannonTest {

        private DoubleCannon doubleCannon;
        private BatteryCompartment testBattery;

        @BeforeEach
        void setUp() {
            doubleCannon = new DoubleCannon();
            doubleCannon.initializeConnectors(new NullConnectorType(), new DoubleConnectorType(), new SingleConnectorType(), new UniversalConnectorType());
            testBattery = new BatteryCompartment(2, BatteryType.DOUBLE, 2);
        }

        @Test
        @DisplayName("Constructor should initialize DoubleCannon with correct values")
        void testConstructor() {
            assertNotNull(doubleCannon);
            assertEquals(2, doubleCannon.getFirePower());
            assertEquals(1, doubleCannon.getEnergyConsumption());
            assertFalse(doubleCannon.isCharge());
        }

        @Test
        @DisplayName("getComponentString should return correct format")
        void testGetComponentString() {
            String result = doubleCannon.getComponentString();
            assertTrue(result.startsWith("doublecannon_"));
        }

        @Test
        @DisplayName("fire should return false when not charged")
        void testFireWhenNotCharged() {
            assertFalse(doubleCannon.fire());
        }

        @Test
        @DisplayName("fire should return false when damaged")
        void testFireWhenDamaged() {
            doubleCannon.setDamaged(true);
            assertFalse(doubleCannon.fire());
        }

        @Test
        @DisplayName("fire should return true when charged and not damaged")
        void testFireWhenChargedAndNotDamaged() {
            // Prima carichiamo il cannone
            testBattery = new BatteryCompartment(2, BatteryType.DOUBLE, 2);
            assertTrue(doubleCannon.charge(testBattery));
            assertTrue(doubleCannon.isCharge());
            // Ora dovrebbe sparare con successo
            assertTrue(doubleCannon.fire());
            // Dopo aver sparato, non dovrebbe essere più carico
            assertFalse(doubleCannon.isCharge());
        }

        @Test
        @DisplayName("charge should return false with null battery")
        void testChargeWithNullBattery() {
            assertFalse(doubleCannon.charge(null));
        }

        @Test
        @DisplayName("charge should return false when damaged")
        void testChargeWhenDamaged() {
            doubleCannon.setDamaged(true);
            testBattery = new BatteryCompartment(2, BatteryType.DOUBLE, 2);
            assertFalse(doubleCannon.charge(testBattery));
        }

        @Test
        @DisplayName("charge should return false when already charged")
        void testChargeWhenAlreadyCharged() {
            // Prima carica
            testBattery = new BatteryCompartment(2, BatteryType.DOUBLE, 2);
            assertTrue(doubleCannon.charge(testBattery));
            // Seconda carica dovrebbe fallire
            assertFalse(doubleCannon.charge(testBattery));
        }

        @Test
        @DisplayName("charge should return false when battery has insufficient charge")
        void testChargeWithInsufficientBattery() {
            testBattery = new BatteryCompartment(2, BatteryType.DOUBLE, 0);
            assertFalse(doubleCannon.charge(testBattery));
        }

        @Test
        @DisplayName("charge should return true with sufficient battery")
        void testChargeWithSufficientBattery() {
            testBattery = new BatteryCompartment(2, BatteryType.DOUBLE, 2);
            assertTrue(doubleCannon.charge(testBattery));
            assertTrue(doubleCannon.isCharge());
        }

        @Test
        @DisplayName("discharge should set isCharged to false")
        void testDischarge() {
            // Prima carichiamo
            testBattery = new BatteryCompartment(2, BatteryType.DOUBLE, 2);
            doubleCannon.charge(testBattery);
            assertTrue(doubleCannon.isCharge());
            // Poi scarichiamo
            doubleCannon.discharge();
            assertFalse(doubleCannon.isCharge());
        }

        @Test
        @DisplayName("getEffectivePower should return 0 when damaged")
        void testGetEffectivePowerWhenDamaged() {
            doubleCannon.setDamaged(true);
            assertEquals(0.0, doubleCannon.getEffectivePower());
        }

        @Test
        @DisplayName("getEffectivePower should return full power when charged and facing north")
        void testGetEffectivePowerChargedFacingNorth() {
            // Carichiamo il cannone
            testBattery = new BatteryCompartment(2, BatteryType.DOUBLE, 2);
            doubleCannon.charge(testBattery);

            doubleCannon.setComponentDirection(Direction.NORTH);

            double expectedPower = doubleCannon.getFirePower();
            assertEquals(expectedPower, doubleCannon.getEffectivePower());
        }

        @Test
        @DisplayName("getEffectivePower should return half power when not charged")
        void testGetEffectivePowerNotCharged() {
            doubleCannon.setComponentDirection(Direction.NORTH);

            double expectedPower = doubleCannon.getFirePower() / 2.0;
            assertEquals(expectedPower, doubleCannon.getEffectivePower());
        }

        @Test
        @DisplayName("calculatePowerUsingBattery should work correctly with battery")
        void testCalculatePowerUsingBatteryWithBattery() {
            doubleCannon.setComponentDirection(Direction.NORTH);

            double powerWithBattery = doubleCannon.calculatePowerUsingBattery(true);
            double expectedPower = doubleCannon.getFirePower();
            assertEquals(expectedPower, powerWithBattery);
        }

        @Test
        @DisplayName("calculatePowerUsingBattery should work correctly without battery")
        void testCalculatePowerUsingBatteryWithoutBattery() {
            doubleCannon.setComponentDirection(Direction.NORTH);

            double powerWithoutBattery = doubleCannon.calculatePowerUsingBattery(false);
            double expectedPower = doubleCannon.getFirePower() / 2.0;
            assertEquals(expectedPower, powerWithoutBattery);
        }

        @Test
        @DisplayName("calculatePowerUsingBattery should return 0 when damaged")
        void testCalculatePowerUsingBatteryWhenDamaged() {
            doubleCannon.setDamaged(true);

            assertEquals(0.0, doubleCannon.calculatePowerUsingBattery(true));
            assertEquals(0.0, doubleCannon.calculatePowerUsingBattery(false));
        }
    }
}