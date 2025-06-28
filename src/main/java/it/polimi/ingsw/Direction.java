package it.polimi.ingsw;

/**
 * This enum represents the four cardinal directions.
 * It provides methods to get the opposite, clockwise, and counter-clockwise directions,
 * as well as to get the adjacent position based on a given position.
 */
public enum Direction {
    NORTH, EAST, SOUTH, WEST;

    /**
     * Returns the opposite direction.
     * The opposite direction is the one that points in the exact opposite way.
     *
     * @return the opposite direction
     */
    public Direction getOpposite() {
        switch (this) {
            case NORTH: return SOUTH;
            case SOUTH: return NORTH;
            case EAST: return WEST;
            case WEST: return EAST;
            default: throw new IllegalStateException("Unknown direction");
        }
    }

    /**
     * Returns the clockwise direction.
     * The clockwise direction is the one that comes after this direction in a clockwise rotation.
     *
     * @return the clockwise direction
     */
    public Direction getClockwise() {
        switch (this) {
            case NORTH: return EAST;
            case EAST: return SOUTH;
            case SOUTH: return WEST;
            case WEST: return NORTH;
            default: throw new IllegalStateException("Unknown direction");
        }
    }

    /**
     * Returns the counter-clockwise direction.
     * The counter-clockwise direction is the one that comes before this direction in a clockwise rotation.
     *
     * @return the counter-clockwise direction
     */
    public Direction getCounterClockwise() {
        switch (this) {
            case NORTH: return WEST;
            case WEST: return SOUTH;
            case SOUTH: return EAST;
            case EAST: return NORTH;
            default: throw new IllegalStateException("Unknown direction");
        }
    }


    /**
     * Returns the position adjacent to the given position in this direction.
     *
     * @param position the position from which to get the adjacent position
     * @return the adjacent position in this direction
     */
    public Position getAdjacentPosition(Position position) {
        switch (this) {
            case NORTH: return new Position(position.getX(), position.getY() - 1);
            case SOUTH: return new Position(position.getX(), position.getY() + 1);
            case EAST: return new Position(position.getX() + 1, position.getY());
            case WEST: return new Position(position.getX() - 1, position.getY());
            default: throw new IllegalStateException("Unknown direction");
        }
    }

    /**
     * Checks if this direction is adjacent to another direction.
     * Two directions are considered adjacent if they are next to each other in the clockwise or counter-clockwise order.
     *
     * @param other the other direction to check adjacency with
     * @return true if this direction is adjacent to the other, false otherwise
     */
    public boolean isAdjacentTo(Direction other) {
        if (this == other) return false; // Same direction is not adjacent
        return this.getClockwise() == other || this.getCounterClockwise() == other;
    }
}
