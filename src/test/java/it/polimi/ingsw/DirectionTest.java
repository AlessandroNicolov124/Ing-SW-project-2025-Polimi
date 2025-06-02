package it.polimi.ingsw;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import static org.junit.jupiter.api.Assertions.*;

public class DirectionTest {

    @Test
    @DisplayName("Test getOpposite() for all directions")
    public void testGetOpposite() {
        assertEquals(Direction.SOUTH, Direction.NORTH.getOpposite());
        assertEquals(Direction.NORTH, Direction.SOUTH.getOpposite());
        assertEquals(Direction.WEST, Direction.EAST.getOpposite());
        assertEquals(Direction.EAST, Direction.WEST.getOpposite());
    }

    @Test
    @DisplayName("Test getClockwise() for all directions")
    public void testGetClockwise() {
        assertEquals(Direction.EAST, Direction.NORTH.getClockwise());
        assertEquals(Direction.SOUTH, Direction.EAST.getClockwise());
        assertEquals(Direction.WEST, Direction.SOUTH.getClockwise());
        assertEquals(Direction.NORTH, Direction.WEST.getClockwise());
    }

    @Test
    @DisplayName("Test getCounterClockwise() for all directions")
    public void testGetCounterClockwise() {
        assertEquals(Direction.WEST, Direction.NORTH.getCounterClockwise());
        assertEquals(Direction.SOUTH, Direction.WEST.getCounterClockwise());
        assertEquals(Direction.EAST, Direction.SOUTH.getCounterClockwise());
        assertEquals(Direction.NORTH, Direction.EAST.getCounterClockwise());
    }

    @Test
    @DisplayName("Test clockwise and counter-clockwise are opposite operations")
    public void testClockwiseCounterClockwiseSymmetry() {
        for (Direction direction : Direction.values()) {
            assertEquals(direction, direction.getClockwise().getCounterClockwise());
            assertEquals(direction, direction.getCounterClockwise().getClockwise());
        }
    }

    @Test
    @DisplayName("Test opposite is symmetric")
    public void testOppositeSymmetry() {
        for (Direction direction : Direction.values()) {
            assertEquals(direction, direction.getOpposite().getOpposite());
        }
    }

    @Test
    @DisplayName("Test four clockwise rotations return to original")
    public void testFourClockwiseRotations() {
        for (Direction direction : Direction.values()) {
            Direction result = direction
                    .getClockwise()
                    .getClockwise()
                    .getClockwise()
                    .getClockwise();
            assertEquals(direction, result);
        }
    }

    @Test
    @DisplayName("Test four counter-clockwise rotations return to original")
    public void testFourCounterClockwiseRotations() {
        for (Direction direction : Direction.values()) {
            Direction result = direction
                    .getCounterClockwise()
                    .getCounterClockwise()
                    .getCounterClockwise()
                    .getCounterClockwise();
            assertEquals(direction, result);
        }
    }

    @Test
    @DisplayName("Test getAdjacentPosition() for NORTH direction")
    public void testGetAdjacentPositionNorth() {
        Position origin = new Position(5, 5);
        Position adjacent = Direction.NORTH.getAdjacentPosition(origin);

        assertEquals(5, adjacent.getX());
        assertEquals(4, adjacent.getY()); // Y decreases going north
    }

    @Test
    @DisplayName("Test getAdjacentPosition() for SOUTH direction")
    public void testGetAdjacentPositionSouth() {
        Position origin = new Position(5, 5);
        Position adjacent = Direction.SOUTH.getAdjacentPosition(origin);

        assertEquals(5, adjacent.getX());
        assertEquals(6, adjacent.getY()); // Y increases going south
    }

    @Test
    @DisplayName("Test getAdjacentPosition() for EAST direction")
    public void testGetAdjacentPositionEast() {
        Position origin = new Position(5, 5);
        Position adjacent = Direction.EAST.getAdjacentPosition(origin);

        assertEquals(6, adjacent.getX()); // X increases going east
        assertEquals(5, adjacent.getY());
    }

    @Test
    @DisplayName("Test getAdjacentPosition() for WEST direction")
    public void testGetAdjacentPositionWest() {
        Position origin = new Position(5, 5);
        Position adjacent = Direction.WEST.getAdjacentPosition(origin);

        assertEquals(4, adjacent.getX()); // X decreases going west
        assertEquals(5, adjacent.getY());
    }

    @Test
    @DisplayName("Test getAdjacentPosition() with origin at (0,0)")
    public void testGetAdjacentPositionAtOrigin() {
        Position origin = new Position(0, 0);

        assertEquals(new Position(0, -1), Direction.NORTH.getAdjacentPosition(origin));
        assertEquals(new Position(0, 1), Direction.SOUTH.getAdjacentPosition(origin));
        assertEquals(new Position(1, 0), Direction.EAST.getAdjacentPosition(origin));
        assertEquals(new Position(-1, 0), Direction.WEST.getAdjacentPosition(origin));
    }

    @Test
    @DisplayName("Test getAdjacentPosition() with negative coordinates")
    public void testGetAdjacentPositionNegativeCoordinates() {
        Position origin = new Position(-3, -7);

        assertEquals(new Position(-3, -8), Direction.NORTH.getAdjacentPosition(origin));
        assertEquals(new Position(-3, -6), Direction.SOUTH.getAdjacentPosition(origin));
        assertEquals(new Position(-2, -7), Direction.EAST.getAdjacentPosition(origin));
        assertEquals(new Position(-4, -7), Direction.WEST.getAdjacentPosition(origin));
    }

    @Test
    @DisplayName("Test opposite directions produce opposite movements")
    public void testOppositeDirectionsOppositeMovements() {
        Position origin = new Position(10, 10);

        for (Direction direction : Direction.values()) {
            Position forward = direction.getAdjacentPosition(origin);
            Position backward = direction.getOpposite().getAdjacentPosition(origin);

            // The sum of movements should bring us back to origin
            int deltaX = forward.getX() + backward.getX() - 2 * origin.getX();
            int deltaY = forward.getY() + backward.getY() - 2 * origin.getY();

            assertEquals(0, deltaX);
            assertEquals(0, deltaY);
        }
    }

    @ParameterizedTest
    @EnumSource(Direction.class)
    @DisplayName("Test that all directions have valid adjacent positions")
    public void testAllDirectionsProduceValidAdjacentPositions(Direction direction) {
        Position origin = new Position(100, 100);
        Position adjacent = direction.getAdjacentPosition(origin);

        // Adjacent position should be exactly 1 unit away in Manhattan distance
        int manhattanDistance = Math.abs(adjacent.getX() - origin.getX()) +
                Math.abs(adjacent.getY() - origin.getY());
        assertEquals(1, manhattanDistance);
    }

    @Test
    @DisplayName("Test circular movement - moving in a square")
    public void testCircularMovement() {
        Position start = new Position(0, 0);
        Position current = start;

        // Move in a clockwise square: East -> South -> West -> North
        current = Direction.EAST.getAdjacentPosition(current);   // (1, 0)
        current = Direction.SOUTH.getAdjacentPosition(current);  // (1, 1)
        current = Direction.WEST.getAdjacentPosition(current);   // (0, 1)
        current = Direction.NORTH.getAdjacentPosition(current);  // (0, 0)

        assertEquals(start, current);
    }

    @Test
    @DisplayName("Test enum values count")
    public void testEnumValuesCount() {
        assertEquals(4, Direction.values().length);
    }

    @Test
    @DisplayName("Test enum contains expected values")
    public void testEnumContainsExpectedValues() {
        Direction[] directions = Direction.values();

        assertTrue(java.util.Arrays.asList(directions).contains(Direction.NORTH));
        assertTrue(java.util.Arrays.asList(directions).contains(Direction.SOUTH));
        assertTrue(java.util.Arrays.asList(directions).contains(Direction.EAST));
        assertTrue(java.util.Arrays.asList(directions).contains(Direction.WEST));
    }
}