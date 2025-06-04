package it.polimi.ingsw.Components;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

/**
 * Test class for NormalCargoHolds component
 */
class NormalCargoHoldsTest {

    private NormalCargoHolds normalCargoHolds;
    private CargoHoldsSize mockSize;
    private Cargo mockCargo;
    private RedCargo mockRedCargo;

    @BeforeEach
    void setUp() {
        mockSize = mock(CargoHoldsSize.class);
        when(mockSize.getCapacity()).thenReturn(2); // Changed from 3 to 2 (valid for normal)

        normalCargoHolds = new NormalCargoHolds(mockSize);

        mockCargo = mock(Cargo.class);
        when(mockCargo.getValue()).thenReturn(100);

        mockRedCargo = mock(RedCargo.class);
        when(mockRedCargo.getValue()).thenReturn(200);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Constructor should initialize with correct capacity")
        void testConstructor() {
            assertNotNull(normalCargoHolds);
            assertEquals(2, normalCargoHolds.getCapacity());
            assertTrue(normalCargoHolds.isEmpty());
            assertEquals(2, normalCargoHolds.getAvailableSpace());
        }

        @Test
        @DisplayName("Constructor should work with valid capacities (2 or 3)")
        void testConstructorWithValidCapacities() {
            // Test with capacity 2
            CargoHoldsSize doubleSize = mock(CargoHoldsSize.class);
            when(doubleSize.getCapacity()).thenReturn(2);
            NormalCargoHolds doubleHold = new NormalCargoHolds(doubleSize);
            assertEquals(2, doubleHold.getCapacity());

            // Test with capacity 3
            CargoHoldsSize tripleSize = mock(CargoHoldsSize.class);
            when(tripleSize.getCapacity()).thenReturn(3);
            NormalCargoHolds tripleHold = new NormalCargoHolds(tripleSize);
            assertEquals(3, tripleHold.getCapacity());
        }
    }

    @Nested
    @DisplayName("Cargo Compatibility Tests")
    class CargoCompatibilityTests {

        @Test
        @DisplayName("canContainCargo should return true for normal cargo")
        void testCanContainNormalCargo() {
            assertTrue(normalCargoHolds.canContainCargo(mockCargo));
        }

        @Test
        @DisplayName("canContainCargo should return false for RedCargo")
        void testCannotContainRedCargo() {
            assertFalse(normalCargoHolds.canContainCargo(mockRedCargo));
        }

        @Test
        @DisplayName("canContainCargo should handle different cargo types")
        void testCanContainDifferentCargoTypes() {
            // Test with various mock cargo types (non-Red)
            Cargo blueCargo = mock(Cargo.class);
            Cargo greenCargo = mock(Cargo.class);

            assertTrue(normalCargoHolds.canContainCargo(blueCargo));
            assertTrue(normalCargoHolds.canContainCargo(greenCargo));
        }
    }

    @Nested
    @DisplayName("Load Cargo Tests")
    class LoadCargoTests {

        @Test
        @DisplayName("loadCargo should successfully load compatible cargo")
        void testLoadCompatibleCargo() {
            normalCargoHolds.loadCargo(mockCargo);

            assertFalse(normalCargoHolds.isEmpty());
            assertEquals(1, normalCargoHolds.getCargos().size());
            assertEquals(1, normalCargoHolds.getAvailableSpace()); // Changed from 2 to 1
            assertTrue(normalCargoHolds.getCargos().contains(mockCargo));
        }

        @Test
        @DisplayName("loadCargo should throw exception for incompatible cargo")
        void testLoadIncompatibleCargo() {
            assertThrows(IllegalArgumentException.class, () -> {
                normalCargoHolds.loadCargo(mockRedCargo);
            });

            assertTrue(normalCargoHolds.isEmpty());
        }

        @Test
        @DisplayName("loadCargo should throw exception when at full capacity")
        void testLoadCargoWhenFull() {
            // Fill to capacity (2)
            Cargo cargo1 = mock(Cargo.class);
            Cargo cargo2 = mock(Cargo.class);
            Cargo cargo3 = mock(Cargo.class);

            normalCargoHolds.loadCargo(cargo1);
            normalCargoHolds.loadCargo(cargo2);

            assertTrue(normalCargoHolds.isFull());

            assertThrows(IllegalStateException.class, () -> {
                normalCargoHolds.loadCargo(cargo3);
            });
        }

        @Test
        @DisplayName("loadCargo should work up to capacity")
        void testLoadCargoToCapacity() {
            Cargo cargo1 = mock(Cargo.class);
            Cargo cargo2 = mock(Cargo.class);

            normalCargoHolds.loadCargo(cargo1);
            normalCargoHolds.loadCargo(cargo2);

            assertTrue(normalCargoHolds.isFull());
            assertEquals(0, normalCargoHolds.getAvailableSpace());
            assertEquals(2, normalCargoHolds.getCargos().size()); // Changed from 3 to 2
        }

        @Test
        @DisplayName("loadCargo should work with triple capacity")
        void testLoadCargoWithTripleCapacity() {
            // Test with capacity 3
            CargoHoldsSize tripleSize = mock(CargoHoldsSize.class);
            when(tripleSize.getCapacity()).thenReturn(3);
            NormalCargoHolds tripleHold = new NormalCargoHolds(tripleSize);

            Cargo cargo1 = mock(Cargo.class);
            Cargo cargo2 = mock(Cargo.class);
            Cargo cargo3 = mock(Cargo.class);

            tripleHold.loadCargo(cargo1);
            tripleHold.loadCargo(cargo2);
            tripleHold.loadCargo(cargo3);

            assertTrue(tripleHold.isFull());
            assertEquals(0, tripleHold.getAvailableSpace());
            assertEquals(3, tripleHold.getCargos().size());
        }
    }

    @Test
    @DisplayName("getComponentString should return correct format")
    void testGetComponentString() {
        String result = normalCargoHolds.getComponentString();
        assertTrue(result.startsWith("normalcargoholds_"+mockSize.getCapacity()+"_"+normalCargoHolds.getConnectionsString()));
    }
}

/**
 * Test class for SpecialCargoHolds component
 */
class SpecialCargoHoldsTest {

    private SpecialCargoHolds specialCargoHolds;
    private CargoHoldsSize mockSize;
    private Cargo mockCargo;
    private RedCargo mockRedCargo;

    @BeforeEach
    void setUp() {
        mockSize = mock(CargoHoldsSize.class);
        when(mockSize.getCapacity()).thenReturn(1); // Changed from 2 to 1 (valid for special)

        specialCargoHolds = new SpecialCargoHolds(mockSize);

        mockCargo = mock(Cargo.class);
        when(mockCargo.getValue()).thenReturn(150);

        mockRedCargo = mock(RedCargo.class);
        when(mockRedCargo.getValue()).thenReturn(300);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Constructor should initialize with correct capacity")
        void testConstructor() {
            assertNotNull(specialCargoHolds);
            assertEquals(1, specialCargoHolds.getCapacity());
            assertTrue(specialCargoHolds.isEmpty());
            assertEquals(1, specialCargoHolds.getAvailableSpace());
        }

        @Test
        @DisplayName("Constructor should work with valid capacities (1 or 2)")
        void testConstructorWithValidCapacities() {
            // Test with capacity 1
            CargoHoldsSize singleSize = mock(CargoHoldsSize.class);
            when(singleSize.getCapacity()).thenReturn(1);
            SpecialCargoHolds singleHold = new SpecialCargoHolds(singleSize);
            assertEquals(1, singleHold.getCapacity());

            // Test with capacity 2
            CargoHoldsSize doubleSize = mock(CargoHoldsSize.class);
            when(doubleSize.getCapacity()).thenReturn(2);
            SpecialCargoHolds doubleHold = new SpecialCargoHolds(doubleSize);
            assertEquals(2, doubleHold.getCapacity());
        }
    }

    @Nested
    @DisplayName("Cargo Compatibility Tests")
    class CargoCompatibilityTests {

        @Test
        @DisplayName("canContainCargo should return true for normal cargo")
        void testCanContainNormalCargo() {
            assertTrue(specialCargoHolds.canContainCargo(mockCargo));
        }

        @Test
        @DisplayName("canContainCargo should return true for RedCargo")
        void testCanContainRedCargo() {
            assertTrue(specialCargoHolds.canContainCargo(mockRedCargo));
        }

        @Test
        @DisplayName("canContainCargo should return true for all cargo types")
        void testCanContainAllCargoTypes() {
            // Test with various mock cargo types
            Cargo blueCargo = mock(Cargo.class);
            Cargo greenCargo = mock(Cargo.class);

            assertTrue(specialCargoHolds.canContainCargo(blueCargo));
            assertTrue(specialCargoHolds.canContainCargo(greenCargo));
            assertTrue(specialCargoHolds.canContainCargo(mockRedCargo));
        }
    }

    @Nested
    @DisplayName("Load Cargo Tests")
    class LoadCargoTests {

        @Test
        @DisplayName("loadCargo should successfully load normal cargo")
        void testLoadNormalCargo() {
            specialCargoHolds.loadCargo(mockCargo);

            assertFalse(specialCargoHolds.isEmpty());
            assertEquals(1, specialCargoHolds.getCargos().size());
            assertEquals(0, specialCargoHolds.getAvailableSpace()); // Changed from 1 to 0
            assertTrue(specialCargoHolds.isFull()); // Now full with single capacity
        }

        @Test
        @DisplayName("loadCargo should successfully load RedCargo")
        void testLoadRedCargo() {
            specialCargoHolds.loadCargo(mockRedCargo);

            assertFalse(specialCargoHolds.isEmpty());
            assertEquals(1, specialCargoHolds.getCargos().size());
            assertTrue(specialCargoHolds.getCargos().contains(mockRedCargo));
            assertTrue(specialCargoHolds.isFull());
        }

        @Test
        @DisplayName("loadCargo should load mixed cargo types with double capacity")
        void testLoadMixedCargoTypesWithDoubleCapacity() {
            // Test with capacity 2
            CargoHoldsSize doubleSize = mock(CargoHoldsSize.class);
            when(doubleSize.getCapacity()).thenReturn(2);
            SpecialCargoHolds doubleHold = new SpecialCargoHolds(doubleSize);

            doubleHold.loadCargo(mockCargo);
            doubleHold.loadCargo(mockRedCargo);

            assertTrue(doubleHold.isFull());
            assertEquals(2, doubleHold.getCargos().size());
            assertTrue(doubleHold.getCargos().contains(mockCargo));
            assertTrue(doubleHold.getCargos().contains(mockRedCargo));
        }

        @Test
        @DisplayName("loadCargo should throw exception when single capacity is exceeded")
        void testLoadCargoExceedsSingleCapacity() {
            specialCargoHolds.loadCargo(mockCargo);
            assertTrue(specialCargoHolds.isFull());

            assertThrows(IllegalStateException.class, () -> {
                specialCargoHolds.loadCargo(mockRedCargo);
            });
        }
    }

    @Test
    @DisplayName("getComponentString should return correct format")
    void testGetComponentString() {
        String result = specialCargoHolds.getComponentString();
        assertTrue(result.startsWith("specialcargoholds_"+mockSize.getCapacity()+"_"+specialCargoHolds.getConnectionsString()));
    }
}

/**
 * Abstract test class for common CargoHolds functionality
 * This tests the base functionality that should work for both Normal and Special cargo holds
 */
abstract class CargoHoldsTestBase {

    protected CargoHolds cargoHolds;
    protected Cargo mockCargo1;
    protected Cargo mockCargo2;

    @BeforeEach
    void setUpBase() {
        mockCargo1 = mock(Cargo.class);
        when(mockCargo1.getValue()).thenReturn(100);

        mockCargo2 = mock(Cargo.class);
        when(mockCargo2.getValue()).thenReturn(200);
    }

    @Nested
    @DisplayName("Common Functionality Tests")
    class CommonFunctionalityTests {

        @Test
        @DisplayName("getCargos should return defensive copy")
        void testGetCargosReturnsDefensiveCopy() {
            if (cargoHolds.canContainCargo(mockCargo1)) {
                cargoHolds.loadCargo(mockCargo1);

                List<Cargo> cargos = cargoHolds.getCargos();
                int originalSize = cargos.size();

                // Modifying returned list should not affect internal state
                cargos.clear();

                assertEquals(originalSize, cargoHolds.getCargos().size());
                assertFalse(cargoHolds.isEmpty());
            }
        }

        @Test
        @DisplayName("isEmpty should work correctly")
        void testIsEmpty() {
            assertTrue(cargoHolds.isEmpty());

            if (cargoHolds.canContainCargo(mockCargo1)) {
                cargoHolds.loadCargo(mockCargo1);
                assertFalse(cargoHolds.isEmpty());

                cargoHolds.unloadCargo(mockCargo1);
                assertTrue(cargoHolds.isEmpty());
            }
        }

        @Test
        @DisplayName("isFull should work correctly")
        void testIsFull() {
            assertFalse(cargoHolds.isFull());

            // Load to capacity (assuming capacity > 0)
            while (!cargoHolds.isFull()) {
                Cargo cargo = mock(Cargo.class);
                if (cargoHolds.canContainCargo(cargo)) {
                    cargoHolds.loadCargo(cargo);
                } else {
                    break; // Can't load this type
                }
            }

            if (cargoHolds.getCapacity() > 0) {
                assertTrue(cargoHolds.isFull() || !cargoHolds.canContainCargo(mock(Cargo.class)));
            }
        }

        @Test
        @DisplayName("getAvailableSpace should return correct value")
        void testGetAvailableSpace() {
            int initialSpace = cargoHolds.getAvailableSpace();
            assertEquals(cargoHolds.getCapacity(), initialSpace);

            if (cargoHolds.canContainCargo(mockCargo1)) {
                cargoHolds.loadCargo(mockCargo1);
                assertEquals(initialSpace - 1, cargoHolds.getAvailableSpace());
            }
        }

        @Test
        @DisplayName("unloadCargo should remove cargo")
        void testUnloadCargo() {
            if (cargoHolds.canContainCargo(mockCargo1)) {
                cargoHolds.loadCargo(mockCargo1);
                assertTrue(cargoHolds.getCargos().contains(mockCargo1));

                cargoHolds.unloadCargo(mockCargo1);
                assertFalse(cargoHolds.getCargos().contains(mockCargo1));
            }
        }

        @Test
        @DisplayName("unloadCargo should handle non-existent cargo gracefully")
        void testUnloadNonExistentCargo() {
            // Should not throw exception
            assertDoesNotThrow(() -> cargoHolds.unloadCargo(mockCargo1));
        }

        @Test
        @DisplayName("sumCargoValue should calculate total value correctly")
        void testSumCargoValue() {
            assertEquals(0, cargoHolds.sumCargoValue());

            if (cargoHolds.canContainCargo(mockCargo1)) {
                cargoHolds.loadCargo(mockCargo1);
                assertEquals(100, cargoHolds.sumCargoValue());

                if (cargoHolds.getAvailableSpace() > 0 && cargoHolds.canContainCargo(mockCargo2)) {
                    cargoHolds.loadCargo(mockCargo2);
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
            if (cargoHolds.canContainCargo(mockCargo1) &&
                    cargoHolds.canContainCargo(mockCargo2) &&
                    cargoHolds.getCapacity() >= 2) {

                // Load both
                cargoHolds.loadCargo(mockCargo1);
                cargoHolds.loadCargo(mockCargo2);
                assertEquals(2, cargoHolds.getCargos().size());

                // Unload one
                cargoHolds.unloadCargo(mockCargo1);
                assertEquals(1, cargoHolds.getCargos().size());
                assertTrue(cargoHolds.getCargos().contains(mockCargo2));

                // Load it back
                cargoHolds.loadCargo(mockCargo1);
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
        CargoHoldsSize mockSize = mock(CargoHoldsSize.class);
        when(mockSize.getCapacity()).thenReturn(3); // Valid capacity for normal cargo holds
        cargoHolds = new NormalCargoHolds(mockSize);
        setUpBase();
    }
}

/**
 * Concrete test class for SpecialCargoHolds extending the base test
 */
class SpecialCargoHoldsCommonTest extends CargoHoldsTestBase {

    @BeforeEach
    void setUp() {
        CargoHoldsSize mockSize = mock(CargoHoldsSize.class);
        when(mockSize.getCapacity()).thenReturn(2); // Valid capacity for special cargo holds
        cargoHolds = new SpecialCargoHolds(mockSize);
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
        // Use valid capacities for each type
        CargoHoldsSize normalSize = mock(CargoHoldsSize.class);
        when(normalSize.getCapacity()).thenReturn(2); // Valid for normal (2 or 3)

        CargoHoldsSize specialSize = mock(CargoHoldsSize.class);
        when(specialSize.getCapacity()).thenReturn(2); // Valid for special (1 or 2)

        normalHolds = new NormalCargoHolds(normalSize);
        specialHolds = new SpecialCargoHolds(specialSize);

        normalCargo = mock(Cargo.class);
        when(normalCargo.getValue()).thenReturn(100);

        redCargo = mock(RedCargo.class);
        when(redCargo.getValue()).thenReturn(200);
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
    @DisplayName("Both hold types should have same capacity management when using same capacity")
    void testCapacityManagement() {
        assertEquals(normalHolds.getCapacity(), specialHolds.getCapacity());
        assertEquals(normalHolds.getAvailableSpace(), specialHolds.getAvailableSpace());

        // Load normal cargo in both
        normalHolds.loadCargo(normalCargo);
        specialHolds.loadCargo(normalCargo);

        assertEquals(normalHolds.getAvailableSpace(), specialHolds.getAvailableSpace());
        assertEquals(normalHolds.getCargos().size(), specialHolds.getCargos().size());
    }

    @Test
    @DisplayName("Test capacity constraints are respected")
    void testCapacityConstraints() {
        // Test that normal holds work with capacity 2 and 3
        CargoHoldsSize normalSize2 = mock(CargoHoldsSize.class);
        when(normalSize2.getCapacity()).thenReturn(2);
        NormalCargoHolds normalHolds2 = new NormalCargoHolds(normalSize2);
        assertEquals(2, normalHolds2.getCapacity());

        CargoHoldsSize normalSize3 = mock(CargoHoldsSize.class);
        when(normalSize3.getCapacity()).thenReturn(3);
        NormalCargoHolds normalHolds3 = new NormalCargoHolds(normalSize3);
        assertEquals(3, normalHolds3.getCapacity());

        // Test that special holds work with capacity 1 and 2
        CargoHoldsSize specialSize1 = mock(CargoHoldsSize.class);
        when(specialSize1.getCapacity()).thenReturn(1);
        SpecialCargoHolds specialHolds1 = new SpecialCargoHolds(specialSize1);
        assertEquals(1, specialHolds1.getCapacity());

        CargoHoldsSize specialSize2 = mock(CargoHoldsSize.class);
        when(specialSize2.getCapacity()).thenReturn(2);
        SpecialCargoHolds specialHolds2 = new SpecialCargoHolds(specialSize2);
        assertEquals(2, specialHolds2.getCapacity());
    }
}