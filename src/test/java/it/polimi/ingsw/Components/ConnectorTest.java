package it.polimi.ingsw.Components;

import it.polimi.ingsw.Direction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import static org.junit.jupiter.api.Assertions.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class ConnectorTest {

    private NullConnectorType nullType;
    private SingleConnectorType singleType;
    private DoubleConnectorType doubleType;
    private UniversalConnectorType universalType;

    @BeforeEach
    public void setUp() {
        nullType = new NullConnectorType();
        singleType = new SingleConnectorType();
        doubleType = new DoubleConnectorType();
        universalType = new UniversalConnectorType();
    }

    // ===== CONNECTOR CONSTRUCTION TESTS =====

    @Test
    @DisplayName("Connector constructor should set direction and type correctly")
    public void testConnectorConstruction() {
        Connector connector = new Connector(Direction.NORTH, singleType);

        assertEquals(Direction.NORTH, connector.getDirection());
        assertEquals(singleType, connector.getConnectorType());
    }

    @ParameterizedTest
    @EnumSource(Direction.class)
    @DisplayName("Connector should accept all Direction values")
    public void testConnectorWithAllDirections(Direction direction) {
        Connector connector = new Connector(direction, universalType);
        assertEquals(direction, connector.getDirection());
    }

    // ===== CONNECTOR GETTER/SETTER TESTS =====

    @Test
    @DisplayName("setDirection should update direction correctly")
    public void testSetDirection() {
        Connector connector = new Connector(Direction.NORTH, singleType);
        connector.setDirection(Direction.SOUTH);
        assertEquals(Direction.SOUTH, connector.getDirection());
    }

    @Test
    @DisplayName("setConnectorType should update connector type correctly")
    public void testSetConnectorType() {
        Connector connector = new Connector(Direction.NORTH, singleType);
        connector.setConnectorType(doubleType);
        assertEquals(doubleType, connector.getConnectorType());
    }

    // ===== NULL CONNECTOR TYPE COMPATIBILITY TESTS =====

    @Test
    @DisplayName("NullConnectorType should be compatible only with NullConnectorType")
    public void testNullConnectorTypeCompatibility() {
        assertTrue(nullType.isCompatibleWith(nullType));
        assertFalse(nullType.isCompatibleWith(singleType));
        assertFalse(nullType.isCompatibleWith(doubleType));
        assertFalse(nullType.isCompatibleWith(universalType));
    }

    // ===== SINGLE CONNECTOR TYPE COMPATIBILITY TESTS =====

    @Test
    @DisplayName("SingleConnectorType should be compatible with Single and Universal")
    public void testSingleConnectorTypeCompatibility() {
        assertFalse(singleType.isCompatibleWith(nullType));
        assertTrue(singleType.isCompatibleWith(singleType));
        assertFalse(singleType.isCompatibleWith(doubleType));
        assertTrue(singleType.isCompatibleWith(universalType));
    }

    // ===== DOUBLE CONNECTOR TYPE COMPATIBILITY TESTS =====

    @Test
    @DisplayName("DoubleConnectorType should be compatible with Double and Universal")
    public void testDoubleConnectorTypeCompatibility() {
        assertFalse(doubleType.isCompatibleWith(nullType));
        assertFalse(doubleType.isCompatibleWith(singleType));
        assertTrue(doubleType.isCompatibleWith(doubleType));
        assertTrue(doubleType.isCompatibleWith(universalType));
    }

    // ===== UNIVERSAL CONNECTOR TYPE COMPATIBILITY TESTS =====

    @Test
    @DisplayName("UniversalConnectorType should be compatible with all except Null")
    public void testUniversalConnectorTypeCompatibility() {
        assertFalse(universalType.isCompatibleWith(nullType));
        assertTrue(universalType.isCompatibleWith(singleType));
        assertTrue(universalType.isCompatibleWith(doubleType));
        assertTrue(universalType.isCompatibleWith(universalType));
    }

    // ===== CONNECTOR COMPATIBILITY TESTS =====

    @Test
    @DisplayName("Connector isCompatibleWith should delegate to ConnectorType")
    public void testConnectorCompatibility() {
        Connector singleConnector = new Connector(Direction.NORTH, singleType);
        Connector universalConnector = new Connector(Direction.SOUTH, universalType);
        Connector nullConnector = new Connector(Direction.EAST, nullType);

        assertTrue(singleConnector.isCompatibleWith(universalConnector));
        assertTrue(universalConnector.isCompatibleWith(singleConnector));
        assertFalse(singleConnector.isCompatibleWith(nullConnector));
        assertFalse(nullConnector.isCompatibleWith(singleConnector));
    }

    @Test
    @DisplayName("Connector isCompatibleWith null should return false and print message")
    public void testConnectorCompatibilityWithNull() {
        Connector connector = new Connector(Direction.NORTH, singleType);

        // Capture System.out to verify the print statement
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        try {
            assertFalse(connector.isCompatibleWith(null));
            String output = outputStream.toString();
            assertTrue(output.contains("other connector is null"));
        } finally {
            System.setOut(originalOut);
        }
    }

    // ===== SYMMETRY TESTS =====

    @Test
    @DisplayName("Compatibility should be symmetric for identical types")
    public void testCompatibilitySymmetryIdenticalTypes() {
        Connector connector1 = new Connector(Direction.NORTH, singleType);
        Connector connector2 = new Connector(Direction.SOUTH, singleType);

        assertTrue(connector1.isCompatibleWith(connector2));
        assertTrue(connector2.isCompatibleWith(connector1));
    }

    @Test
    @DisplayName("Compatibility should be symmetric for Universal and other types")
    public void testCompatibilitySymmetryWithUniversal() {
        Connector universalConnector = new Connector(Direction.NORTH, universalType);
        Connector singleConnector = new Connector(Direction.SOUTH, singleType);
        Connector doubleConnector = new Connector(Direction.EAST, doubleType);

        assertTrue(universalConnector.isCompatibleWith(singleConnector));
        assertTrue(singleConnector.isCompatibleWith(universalConnector));

        assertTrue(universalConnector.isCompatibleWith(doubleConnector));
        assertTrue(doubleConnector.isCompatibleWith(universalConnector));
    }

    @Test
    @DisplayName("Incompatible types should be symmetric")
    public void testIncompatibilitySymmetry() {
        Connector singleConnector = new Connector(Direction.NORTH, singleType);
        Connector doubleConnector = new Connector(Direction.SOUTH, doubleType);
        Connector nullConnector = new Connector(Direction.EAST, nullType);

        // Single and Double are not compatible
        assertFalse(singleConnector.isCompatibleWith(doubleConnector));
        assertFalse(doubleConnector.isCompatibleWith(singleConnector));

        // Null is only compatible with Null
        assertFalse(nullConnector.isCompatibleWith(singleConnector));
        assertFalse(singleConnector.isCompatibleWith(nullConnector));

        assertFalse(nullConnector.isCompatibleWith(doubleConnector));
        assertFalse(doubleConnector.isCompatibleWith(nullConnector));
    }

    // ===== COMPREHENSIVE COMPATIBILITY MATRIX TEST =====

    @Test
    @DisplayName("Complete compatibility matrix test")
    public void testCompleteCompatibilityMatrix() {
        ConnectorType[] types = {nullType, singleType, doubleType, universalType};
        String[] typeNames = {"Null", "Single", "Double", "Universal"};

        // Expected compatibility matrix (row compatible with column)
        boolean[][] expectedMatrix = {
                {true,  false, false, false}, // Null with [Null, Single, Double, Universal]
                {false, true,  false, true},  // Single with [Null, Single, Double, Universal]
                {false, false, true,  true},  // Double with [Null, Single, Double, Universal]
                {false, true,  true,  true}   // Universal with [Null, Single, Double, Universal]
        };

        for (int i = 0; i < types.length; i++) {
            for (int j = 0; j < types.length; j++) {
                boolean actual = types[i].isCompatibleWith(types[j]);
                boolean expected = expectedMatrix[i][j];
                assertEquals(expected, actual,
                        String.format("%s should %sbe compatible with %s",
                                typeNames[i], expected ? "" : "not ", typeNames[j]));
            }
        }
    }

    // ===== SERIALIZATION TEST =====

    @Test
    @DisplayName("Connector should implement Serializable")
    public void testConnectorSerializable() {
        Connector connector = new Connector(Direction.NORTH, singleType);
        assertTrue(connector instanceof java.io.Serializable);
    }

    @Test
    @DisplayName("ConnectorType should implement Serializable")
    public void testConnectorTypeSerializable() {
        assertTrue(singleType instanceof java.io.Serializable);
        assertTrue(doubleType instanceof java.io.Serializable);
        assertTrue(universalType instanceof java.io.Serializable);
        assertTrue(nullType instanceof java.io.Serializable);
    }

    // ===== EDGE CASES =====

    @Test
    @DisplayName("Multiple connectors with same type should be compatible")
    public void testMultipleConnectorsSameType() {
        Connector connector1 = new Connector(Direction.NORTH, singleType);
        Connector connector2 = new Connector(Direction.SOUTH, singleType);
        Connector connector3 = new Connector(Direction.EAST, singleType);

        assertTrue(connector1.isCompatibleWith(connector2));
        assertTrue(connector2.isCompatibleWith(connector3));
        assertTrue(connector1.isCompatibleWith(connector3));
    }

    @Test
    @DisplayName("Direction should not affect compatibility")
    public void testDirectionDoesNotAffectCompatibility() {
        Connector northSingle = new Connector(Direction.NORTH, singleType);
        Connector southSingle = new Connector(Direction.SOUTH, singleType);
        Connector eastUniversal = new Connector(Direction.EAST, universalType);
        Connector westUniversal = new Connector(Direction.WEST, universalType);

        assertTrue(northSingle.isCompatibleWith(southSingle));
        assertTrue(eastUniversal.isCompatibleWith(westUniversal));
        assertTrue(northSingle.isCompatibleWith(eastUniversal));
        assertTrue(southSingle.isCompatibleWith(westUniversal));
    }
}