package it.polimi.ingsw.Components;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

/**
 * Test class for NormalCargoHolds component
 */
class NormalCargoHoldsTest {

    private NormalCargoHolds normalCargoHolds;
    private YellowCargo yellowCargo;
    private GreenCargo greenCargo;
    private BlueCargo blueCargo;
    private RedCargo redCargo;

    @BeforeEach
    void setUp() {
        normalCargoHolds = new NormalCargoHolds(CargoHoldsSize.TRIPLE);
        normalCargoHolds.initializeConnectors(new UniversalConnectorType(),new UniversalConnectorType(), new SingleConnectorType(), new NullConnectorType());
        blueCargo = new BlueCargo();
        greenCargo = new GreenCargo();
        yellowCargo = new YellowCargo();
        redCargo = new RedCargo();
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Constructor should initialize with correct capacity and only allow DOUBLE or TRIPLE size")
        void testConstructorAllowedSizes() {
            // DOUBLE
            NormalCargoHolds doubleHold = new NormalCargoHolds(CargoHoldsSize.DOUBLE);
            assertEquals(2, doubleHold.getCapacity());
            // TRIPLE
            NormalCargoHolds tripleHold = new NormalCargoHolds(CargoHoldsSize.TRIPLE);
            assertEquals(3, tripleHold.getCapacity());
        }

        @Test
        @DisplayName("Constructor should throw for SINGLE size")
        void testConstructorSingleNotAllowed() {
            assertThrows(IllegalArgumentException.class, () -> new NormalCargoHolds(CargoHoldsSize.SINGLE));
        }
    }

    @Nested
    @DisplayName("Cargo Compatibility Tests")
    class CargoCompatibilityTests {

        @Test
        @DisplayName("canContainCargo should return true for normal cargo")
        void testCanContainYellowGreenBlueCargo() {
            assertTrue(normalCargoHolds.canContainCargo(yellowCargo));
            assertTrue(normalCargoHolds.canContainCargo(blueCargo));
            assertTrue(normalCargoHolds.canContainCargo(greenCargo));
        }

        @Test
        @DisplayName("canContainCargo should return false for RedCargo")
        void testCannotContainRedCargo() {
            assertFalse(normalCargoHolds.canContainCargo(redCargo));
        }
    }

    @Nested
    @DisplayName("Load Cargo Tests")
    class LoadCargoTests {

        @Test
        @DisplayName("loadCargo should successfully load compatible cargo")
        void testLoadCompatibleCargo() {
            normalCargoHolds.loadCargo(yellowCargo);

            assertFalse(normalCargoHolds.isEmpty());
            assertEquals(1, normalCargoHolds.getCargos().size());
            assertEquals(2, normalCargoHolds.getAvailableSpace());
            assertTrue(normalCargoHolds.getCargos().contains(yellowCargo));
        }

        @Test
        @DisplayName("loadCargo should throw exception for incompatible cargo")
        void testLoadIncompatibleCargo() {
            assertThrows(IllegalArgumentException.class, () -> {
                normalCargoHolds.loadCargo(redCargo);
            });

            assertTrue(normalCargoHolds.isEmpty());
        }

        @Test
        @DisplayName("loadCargo should throw exception when at full capacity")
        void testLoadCargoWhenFull() {
            Cargo cargo1 = new BlueCargo();
            Cargo cargo2 = new BlueCargo();
            Cargo cargo3 = new BlueCargo();
            Cargo cargo4 = new BlueCargo();

            normalCargoHolds.loadCargo(cargo1);
            normalCargoHolds.loadCargo(cargo2);
            normalCargoHolds.loadCargo(cargo3);

            assertTrue(normalCargoHolds.isFull());

            assertThrows(IllegalStateException.class, () -> {
                normalCargoHolds.loadCargo(cargo4);
            });
        }

        @Test
        @DisplayName("loadCargo should work up to capacity")
        void testLoadCargoToCapacity() {
            Cargo cargo1 = new BlueCargo();
            Cargo cargo2 = new BlueCargo();
            Cargo cargo3 = new BlueCargo();

            normalCargoHolds.loadCargo(cargo1);
            normalCargoHolds.loadCargo(cargo2);
            normalCargoHolds.loadCargo(cargo3);

            assertTrue(normalCargoHolds.isFull());
            assertEquals(0, normalCargoHolds.getAvailableSpace());
            assertEquals(3, normalCargoHolds.getCargos().size());
        }
    }

    @Test
    @DisplayName("getComponentString should return correct format")
    void testGetComponentString() {
        String result = normalCargoHolds.getComponentString();
        assertTrue(result.startsWith("normalcargoholds_"+CargoHoldsSize.fromInt(normalCargoHolds.getCapacity()).toString().toLowerCase()+"_"+normalCargoHolds.getConnectionsString()));
    }
}

/**
 * Test class for SpecialCargoHolds component
 */
class SpecialCargoHoldsTest {

    private SpecialCargoHolds specialCargoHolds;
    private YellowCargo yellowCargo;
    private GreenCargo greenCargo;
    private BlueCargo blueCargo;
    private RedCargo redCargo;

    @BeforeEach
    void setUp() {
        specialCargoHolds = new SpecialCargoHolds(CargoHoldsSize.DOUBLE);
        specialCargoHolds.initializeConnectors(new UniversalConnectorType(),new UniversalConnectorType(), new SingleConnectorType(), new NullConnectorType());

        blueCargo = new BlueCargo();
        greenCargo = new GreenCargo();
        yellowCargo = new YellowCargo();
        redCargo = new RedCargo();
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Constructor should initialize with correct capacity and only allow SINGLE or DOUBLE size")
        void testConstructorAllowedSizes() {
            // SINGLE
            SpecialCargoHolds single = new SpecialCargoHolds(CargoHoldsSize.SINGLE);
            assertEquals(1, single.getCapacity());
            // DOUBLE
            SpecialCargoHolds doubleHold = new SpecialCargoHolds(CargoHoldsSize.DOUBLE);
            assertEquals(2, doubleHold.getCapacity());
        }

        @Test
        @DisplayName("Constructor should throw for TRIPLE size")
        void testConstructorTripleNotAllowed() {
            assertThrows(IllegalArgumentException.class, () -> new SpecialCargoHolds(CargoHoldsSize.TRIPLE));
        }
    }

    @Nested
    @DisplayName("Cargo Compatibility Tests")
    class CargoCompatibilityTests {

        @Test
        @DisplayName("canContainCargo should return true for normal cargo")
        void testCanContainNormalCargo() {
            assertTrue(specialCargoHolds.canContainCargo(blueCargo));
            assertTrue(specialCargoHolds.canContainCargo(greenCargo));
            assertTrue(specialCargoHolds.canContainCargo(yellowCargo));
        }

        @Test
        @DisplayName("canContainCargo should return true for RedCargo")
        void testCanContainRedCargo() {
            assertTrue(specialCargoHolds.canContainCargo(redCargo));
        }

        @Test
        @DisplayName("canContainCargo should return true for all cargo types")
        void testCanContainAllCargoTypes() {
            assertTrue(specialCargoHolds.canContainCargo(blueCargo));
            assertTrue(specialCargoHolds.canContainCargo(greenCargo));
            assertTrue(specialCargoHolds.canContainCargo(yellowCargo));
            assertTrue(specialCargoHolds.canContainCargo(redCargo));
        }
    }

    @Nested
    @DisplayName("Load Cargo Tests")
    class LoadCargoTests {

        @Test
        @DisplayName("loadCargo should successfully load normal cargo")
        void testLoadNormalCargo() {
            specialCargoHolds.loadCargo(blueCargo);

            assertFalse(specialCargoHolds.isEmpty());
            assertEquals(1, specialCargoHolds.getCargos().size());
            assertEquals(1, specialCargoHolds.getAvailableSpace());
        }

        @Test
        @DisplayName("loadCargo should successfully load RedCargo")
        void testLoadRedCargo() {
            specialCargoHolds.loadCargo(redCargo);

            assertFalse(specialCargoHolds.isEmpty());
            assertEquals(1, specialCargoHolds.getCargos().size());
            assertTrue(specialCargoHolds.getCargos().contains(redCargo));
        }

        @Test
        @DisplayName("loadCargo should load mixed cargo types")
        void testLoadMixedCargoTypes() {
            specialCargoHolds.loadCargo(blueCargo);
            specialCargoHolds.loadCargo(redCargo);

            assertTrue(specialCargoHolds.isFull());
            assertEquals(2, specialCargoHolds.getCargos().size());
            assertTrue(specialCargoHolds.getCargos().contains(blueCargo));
            assertTrue(specialCargoHolds.getCargos().contains(redCargo));
        }
    }

    @Test
    @DisplayName("getComponentString should return correct format")
    void testGetComponentString() {
        String result = specialCargoHolds.getComponentString();
        assertTrue(result.startsWith("specialcargoholds_"+CargoHoldsSize.fromInt(specialCargoHolds.getCapacity()).toString().toLowerCase()+"_"+specialCargoHolds.getConnectionsString()));
    }
}
