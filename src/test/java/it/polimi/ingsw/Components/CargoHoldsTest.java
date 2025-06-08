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
        blueCargo = new BlueCargo();
        greenCargo = new GreenCargo();
        yellowCargo = new YellowCargo();
        redCargo = new RedCargo();
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Constructor should initialize with correct capacity")
        void testConstructor() {
            assertNotNull(normalCargoHolds);
            assertEquals(3, normalCargoHolds.getCapacity());
            assertTrue(normalCargoHolds.isEmpty());
            assertEquals(3, normalCargoHolds.getAvailableSpace());
        }

        @Test
        @DisplayName("Constructor should work with different sizes")
        void testConstructorWithDifferentSizes() {
            CargoHoldsSize largeSize = CargoHoldsSize.TRIPLE;
            NormalCargoHolds largeHold = new NormalCargoHolds(largeSize);
            assertEquals(3, largeHold.getCapacity());
            CargoHoldsSize doubleSize = CargoHoldsSize.DOUBLE;
            NormalCargoHolds doubleHold = new NormalCargoHolds(doubleSize);
            assertEquals(2, doubleHold.getCapacity());
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
        assertTrue(result.startsWith("normalcargoholds_"+normalCargoHolds.getCapacity()+"_"+normalCargoHolds.getConnectionsString()));
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
        size = new CargoHoldsSize(2);
        specialCargoHolds = new SpecialCargoHolds(CargoHoldsSize.DOUBLE);

        cargo = new Cargo(150);
        redCargo = new RedCargo(300);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Constructor should initialize with correct capacity")
        void testConstructor() {
            assertNotNull(specialCargoHolds);
            assertEquals(2, specialCargoHolds.getCapacity());
            assertTrue(specialCargoHolds.isEmpty());
            assertEquals(2, specialCargoHolds.getAvailableSpace());
        }
    }

    @Nested
    @DisplayName("Cargo Compatibility Tests")
    class CargoCompatibilityTests {

        @Test
        @DisplayName("canContainCargo should return true for normal cargo")
        void testCanContainNormalCargo() {
            assertTrue(specialCargoHolds.canContainCargo(cargo));
        }

        @Test
        @DisplayName("canContainCargo should return true for RedCargo")
        void testCanContainRedCargo() {
            assertTrue(specialCargoHolds.canContainCargo(redCargo));
        }

        @Test
        @DisplayName("canContainCargo should return true for all cargo types")
        void testCanContainAllCargoTypes() {
            Cargo blueCargo = new Cargo(50);
            Cargo greenCargo = new Cargo(60);

            assertTrue(specialCargoHolds.canContainCargo(blueCargo));
            assertTrue(specialCargoHolds.canContainCargo(greenCargo));
            assertTrue(specialCargoHolds.canContainCargo(redCargo));
        }
    }

    @Nested
    @DisplayName("Load Cargo Tests")
    class LoadCargoTests {

        @Test
        @DisplayName("loadCargo should successfully load normal cargo")
        void testLoadNormalCargo() {
            specialCargoHolds.loadCargo(cargo);

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
            specialCargoHolds.loadCargo(cargo);
            specialCargoHolds.loadCargo(redCargo);

            assertTrue(specialCargoHolds.isFull());
            assertEquals(2, specialCargoHolds.getCargos().size());
            assertTrue(specialCargoHolds.getCargos().contains(cargo));
            assertTrue(specialCargoHolds.getCargos().contains(redCargo));
        }
    }

    @Test
    @DisplayName("getComponentString should return correct format")
    void testGetComponentString() {
        String result = specialCargoHolds.getComponentString();
        assertTrue(result.startsWith("specialcargoholds_"+size.getCapacity()+"_"+specialCargoHolds.getConnectionsString()));
    }
}

/**
 * Abstract test class for common CargoHolds functionality
 * This tests the base functionality that should work for both Normal and Special cargo holds
 */
abstract class CargoHoldsTestBase {

    protected CargoHolds cargoHolds;
    protected Cargo cargo1;
    protected Cargo cargo2;

    @BeforeEach
    void setUpBase() {
        cargo1 = new Cargo(100);
        cargo2 = new Cargo(200);
    }

    @Nested
    @DisplayName("Common Functionality Tests")
    class CommonFunctionalityTests {

        @Test
        @DisplayName("getCargos should return defensive copy")
        void testGetCargosReturnsDefensiveCopy() {
            cargoHolds.loadCargo(cargo1);

            List<Cargo> cargos = cargoHolds.getCargos();
            int originalSize = cargos.size();

            // Modifying returned list should not affect internal state
            cargos.clear();

            assertEquals(originalSize, cargoHolds.getCargos().size());
            assertFalse(cargoHolds.isEmpty());
        }

        @Test
        @DisplayName("isEmpty should work correctly")
        void testIsEmpty() {
            assertTrue(cargoHolds.isEmpty());

            cargoHolds.loadCargo(cargo1);
            assertFalse(cargoHolds.isEmpty());

            cargoHolds.unloadCargo(cargo1);
            assertTrue(cargoHolds.isEmpty());
        }

        @Test
        @DisplayName("isFull should work correctly")
        void testIsFull() {
            assertFalse(cargoHolds.isFull());

            // Load to capacity (assuming capacity > 0)
            while (!cargoHolds.isFull()) {
                Cargo cargo = new Cargo(10);
                if (cargoHolds.canContainCargo(cargo)) {
                    cargoHolds.loadCargo(cargo);
                } else {
                    break; // Can't load this type
                }
            }

            if (cargoHolds.getCapacity() > 0) {
                assertTrue(cargoHolds.isFull() || !cargoHolds.canContainCargo(new Cargo(10)));
            }
        }

        @Test
        @DisplayName("getAvailableSpace should return correct value")
        void testGetAvailableSpace() {
            int initialSpace = cargoHolds.getAvailableSpace();
            assertEquals(cargoHolds.getCapacity(), initialSpace);

            if (cargoHolds.canContainCargo(cargo1)) {
                cargoHolds.loadCargo(cargo1);
                assertEquals(initialSpace - 1, cargoHolds.getAvailableSpace());
            }
        }

        @Test
        @DisplayName("unloadCargo should remove cargo")
        void testUnloadCargo() {
            if (cargoHolds.canContainCargo(cargo1)) {
                cargoHolds.loadCargo(cargo1);
                assertTrue(cargoHolds.getCargos().contains(cargo1));

                cargoHolds.unloadCargo(cargo1);
                assertFalse(cargoHolds.getCargos().contains(cargo1));
            }
        }

        @Test
        @DisplayName("unloadCargo should handle non-existent cargo gracefully")
        void testUnloadNonExistentCargo() {
            // Should not throw exception
            assertDoesNotThrow(() -> cargoHolds.unloadCargo(cargo1));
        }

        @Test
        @DisplayName("sumCargoValue should calculate total value correctly")
        void testSumCargoValue() {
            assertEquals(0, cargoHolds.sumCargoValue());

            if (cargoHolds.canContainCargo(cargo1) && cargoHolds.canContainCargo(cargo2)) {
                cargoHolds.loadCargo(cargo1);
                assertEquals(100, cargoHolds.sumCargoValue());

                if (cargoHolds.getAvailableSpace() > 0) {
                    cargoHolds.loadCargo(cargo2);
                    assertEquals(300, cargoHolds.sumCargoValue());
                }
            }
        }

        @Test
        @DisplayName("sumCargoValue should be 0 when empty")
        void testSumCargoValueWhenEmpty() {
            assertEquals(0, cargoHolds.sumCargoValue());
        }

        @Test
        @DisplayName("Multiple load and unload operations should work correctly")
        void testMultipleOperations() {
            if (cargoHolds.canContainCargo(cargo1) &&
                    cargoHolds.canContainCargo(cargo2) &&
                    cargoHolds.getCapacity() >= 2) {

                // Load both
                cargoHolds.loadCargo(cargo1);
                cargoHolds.loadCargo(cargo2);
                assertEquals(2, cargoHolds.getCargos().size());

                // Unload one
                cargoHolds.unloadCargo(cargo1);
                assertEquals(1, cargoHolds.getCargos().size());
                assertTrue(cargoHolds.getCargos().contains(cargo2));

                // Load it back
                cargoHolds.loadCargo(cargo1);
                assertEquals(2, cargoHolds.getCargos().size());
            }
        }
    }
}

/**
 * Concrete test class for NormalCargoHolds extending the base test
 */
class NormalCargoHoldsCommonTest extends CargoHoldsTestBase {

    @BeforeEach
    void setUp() {
        CargoHoldsSize size = new CargoHoldsSize(5);
        cargoHolds = new NormalCargoHolds(size);
        setUpBase();
    }
}

/**
 * Concrete test class for SpecialCargoHolds extending the base test
 */
class SpecialCargoHoldsCommonTest extends CargoHoldsTestBase {

    @BeforeEach
    void setUp() {
        CargoHoldsSize size = new CargoHoldsSize(5);
        cargoHolds = new SpecialCargoHolds(size);
        setUpBase();
    }
}

/**
 * Integration tests comparing behavior between Normal and Special cargo holds
 */
class CargoHoldsIntegrationTest {

    private NormalCargoHolds normalHolds;
    private SpecialCargoHolds specialHolds;
    private Cargo normalCargo;
    private RedCargo redCargo;

    @BeforeEach
    void setUp() {
        CargoHoldsSize size = new CargoHoldsSize(3);

        normalHolds = new NormalCargoHolds(size);
        specialHolds = new SpecialCargoHolds(size);

        normalCargo = new Cargo(100);
        redCargo = new RedCargo(200);
    }

    @Test
    @DisplayName("Normal holds should reject RedCargo while Special holds accept it")
    void testCargoTypeRestrictions() {
        // Normal cargo should work in both
        assertTrue(normalHolds.canContainCargo(normalCargo));
        assertTrue(specialHolds.canContainCargo(normalCargo));

        // RedCargo should only work in special holds
        assertFalse(normalHolds.canContainCargo(redCargo));
        assertTrue(specialHolds.canContainCargo(redCargo));

        // Test actual loading
        assertDoesNotThrow(() -> normalHolds.loadCargo(normalCargo));
        assertThrows(IllegalArgumentException.class, () -> normalHolds.loadCargo(redCargo));

        assertDoesNotThrow(() -> specialHolds.loadCargo(normalCargo));
        assertDoesNotThrow(() -> specialHolds.loadCargo(redCargo));
    }

    @Test
    @DisplayName("Both hold types should have same capacity management")
    void testCapacityManagement() {
        assertEquals(normalHolds.getCapacity(), specialHolds.getCapacity());
        assertEquals(normalHolds.getAvailableSpace(), specialHolds.getAvailableSpace());

        // Load normal cargo in both
        normalHolds.loadCargo(normalCargo);
        specialHolds.loadCargo(normalCargo);

        assertEquals(normalHolds.getAvailableSpace(), specialHolds.getAvailableSpace());
        assertEquals(normalHolds.getCargos().size(), specialHolds.getCargos().size());
    }
}