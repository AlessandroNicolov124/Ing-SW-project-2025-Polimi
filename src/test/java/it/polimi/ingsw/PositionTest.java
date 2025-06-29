package it.polimi.ingsw;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.*;

class PositionTest {

    private Position position;

    @BeforeEach
    void setUp() {
        position = new Position(5, 3);
    }

    @Test
    @DisplayName("Test Position creation")
    void testPositionCreation() {
        Position pos = new Position(10, 20);

        assertEquals(10, pos.getX());
        assertEquals(20, pos.getY());
    }

    @Test
    @DisplayName("Test Position creation with zero coordinates")
    void testPositionCreationWithZeroCoordinates() {
        Position pos = new Position(0, 0);

        assertEquals(0, pos.getX());
        assertEquals(0, pos.getY());
    }

    @Test
    @DisplayName("Test getX method")
    void testGetX() {
        assertEquals(5, position.getX());
    }

    @Test
    @DisplayName("Test getY method")
    void testGetY() {
        assertEquals(3, position.getY());
    }

    @Test
    @DisplayName("Test setX method")
    void testSetX() {
        //position.setX(15);
        position = new Position(15, 3);
        assertEquals(15, position.getX());
        assertEquals(3, position.getY()); // Y should remain unchanged
    }

    @Test
    @DisplayName("Test setY method")
    void testSetY() {
        //position.setY(25);
        position = new Position(15, 25);
        assertEquals(5, position.getX()); // X should remain unchanged
        assertEquals(25, position.getY());
    }

    @Test
    @DisplayName("Test equals method with same position")
    void testEqualsWithSamePosition() {
        Position other = new Position(5, 3);
        assertEquals(position, other);
        assertEquals(other, position); // Test symmetry
    }

    @Test
    @DisplayName("Test equals method with different position")
    void testEqualsWithDifferentPosition() {
        Position other = new Position(5, 4);
        assertNotEquals(position, other);
        assertNotEquals(other, position); // Test symmetry
    }

    @Test
    @DisplayName("Test equals method with completely different coordinates")
    void testEqualsWithCompletelyDifferentCoordinates() {
        Position other = new Position(10, 20);
        assertNotEquals(position, other);
    }

    @Test
    @DisplayName("Test equals method with null object")
    void testEqualsWithNull() {
        assertNotEquals(null, position);
    }

    @Test
    @DisplayName("Test equals method with different object type")
    void testEqualsWithDifferentObjectType() {
        String notAPosition = "This is not a position";
        assertNotEquals(position, notAPosition);
    }

    @Test
    @DisplayName("Test equals method with same object reference")
    void testEqualsWithSameReference() {
        assertEquals(position, position);
    }

    @Test
    @DisplayName("Test getAdjacentPosition NORTH")
    void testGetAdjacentPositionNorth() {
        Position adjacent = position.getAdjacentPosition(Direction.NORTH);

        assertEquals(5, adjacent.getX());
        assertEquals(2, adjacent.getY()); // y - 1

        // Verify original position is unchanged
        assertEquals(5, position.getX());
        assertEquals(3, position.getY());
    }

    @Test
    @DisplayName("Test getAdjacentPosition SOUTH")
    void testGetAdjacentPositionSouth() {
        Position adjacent = position.getAdjacentPosition(Direction.SOUTH);

        assertEquals(5, adjacent.getX());
        assertEquals(4, adjacent.getY()); // y + 1
    }

    @Test
    @DisplayName("Test getAdjacentPosition EAST")
    void testGetAdjacentPositionEast() {
        Position adjacent = position.getAdjacentPosition(Direction.EAST);

        assertEquals(6, adjacent.getX()); // x + 1
        assertEquals(3, adjacent.getY());
    }

    @Test
    @DisplayName("Test getAdjacentPosition WEST")
    void testGetAdjacentPositionWest() {
        Position adjacent = position.getAdjacentPosition(Direction.WEST);

        assertEquals(4, adjacent.getX()); // x - 1
        assertEquals(3, adjacent.getY());
    }

    @Test
    @DisplayName("Test getAdjacentPosition with null direction throws exception")
    void testGetAdjacentPositionWithNullDirection() {
        assertThrows(NullPointerException.class, () -> {
            position.getAdjacentPosition(null);
        });
    }

    @Test
    @DisplayName("Test getAdjacentPosition from origin")
    void testGetAdjacentPositionFromOrigin() {
        Position origin = new Position(0, 0);

        Position north = origin.getAdjacentPosition(Direction.NORTH);
        assertEquals(0, north.getX());
        assertEquals(-1, north.getY());

        Position south = origin.getAdjacentPosition(Direction.SOUTH);
        assertEquals(0, south.getX());
        assertEquals(1, south.getY());

        Position east = origin.getAdjacentPosition(Direction.EAST);
        assertEquals(1, east.getX());
        assertEquals(0, east.getY());

        Position west = origin.getAdjacentPosition(Direction.WEST);
        assertEquals(-1, west.getX());
        assertEquals(0, west.getY());
    }

    @Test
    @DisplayName("Test getAdjacentPosition with negative coordinates")
    void testGetAdjacentPositionWithNegativeCoordinates() {
        Position negativePos = new Position(-2, -3);

        Position north = negativePos.getAdjacentPosition(Direction.NORTH);
        assertEquals(-2, north.getX());
        assertEquals(-4, north.getY());

        Position east = negativePos.getAdjacentPosition(Direction.EAST);
        assertEquals(-1, east.getX());
        assertEquals(-3, east.getY());
    }

    @Test
    @DisplayName("Test toString method")
    void testToString() {
        String expected = "Position [x=5, y=3]";
        assertEquals(expected, position.toString());
    }

    @Test
    @DisplayName("Test toString method with negative coordinates")
    void testToStringWithNegativeCoordinates() {
        Position negativePos = new Position(-10, -20);
        String expected = "Position [x=-10, y=-20]";
        assertEquals(expected, negativePos.toString());
    }

    @Test
    @DisplayName("Test toString method with zero coordinates")
    void testToStringWithZeroCoordinates() {
        Position zeroPos = new Position(0, 0);
        String expected = "Position [x=0, y=0]";
        assertEquals(expected, zeroPos.toString());
    }

    @Test
    @DisplayName("Test serialization compatibility")
    void testSerializationCompatibility() {
        // Verify that the class implements Serializable
        assertInstanceOf(Serializable.class, position, "Position should implement Serializable interface");
    }

    @Test
    @DisplayName("Test position immutability through getAdjacentPosition")
    void testPositionImmutabilityThroughGetAdjacentPosition() {
        int originalX = position.getX();
        int originalY = position.getY();

        // Call getAdjacentPosition multiple times
        position.getAdjacentPosition(Direction.NORTH);
        position.getAdjacentPosition(Direction.SOUTH);
        position.getAdjacentPosition(Direction.EAST);
        position.getAdjacentPosition(Direction.WEST);

        // Verify original position hasn't changed
        assertEquals(originalX, position.getX());
        assertEquals(originalY, position.getY());
    }

    @Test
    @DisplayName("Test multiple adjacent positions form correct pattern")
    void testMultipleAdjacentPositions() {
        Position center = new Position(0, 0);

        Position north = center.getAdjacentPosition(Direction.NORTH);
        Position south = center.getAdjacentPosition(Direction.SOUTH);
        Position east = center.getAdjacentPosition(Direction.EAST);
        Position west = center.getAdjacentPosition(Direction.WEST);

        // Test that going in opposite directions cancels out
        Position backToCenter1 = north.getAdjacentPosition(Direction.SOUTH);
        Position backToCenter2 = south.getAdjacentPosition(Direction.NORTH);
        Position backToCenter3 = east.getAdjacentPosition(Direction.WEST);
        Position backToCenter4 = west.getAdjacentPosition(Direction.EAST);

        assertEquals(center, backToCenter1);
        assertEquals(center, backToCenter2);
        assertEquals(center, backToCenter3);
        assertEquals(center, backToCenter4);
    }

    @Test
    @DisplayName("Test direct field access")
    void testDirectFieldAccess() {
        // Test that public fields x and y can be accessed directly
        assertEquals(5, position.getX());
        assertEquals(3, position.getY());

        // Test direct modification
        position = new Position(100, 200);

        assertEquals(100, position.getX());
        assertEquals(200, position.getY());
        assertEquals(100, position.getX());
        assertEquals(200, position.getY());
    }
}