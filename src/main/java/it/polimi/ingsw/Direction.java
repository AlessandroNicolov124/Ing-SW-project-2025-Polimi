package it.polimi.ingsw;

public enum Direction {
    NORTH, EAST, SOUTH, WEST;

    public Direction getOpposite() {
        switch (this) {
            case NORTH: return SOUTH;
            case SOUTH: return NORTH;
            case EAST: return WEST;
            case WEST: return EAST;
            default: throw new IllegalStateException("Unknown direction");
        }
    }

    //restituisce la posizione adiacente a quella specificata, in base alla direzione
    public Position getAdjacentPosition(Position position) {
        switch (this) {
            case NORTH: return new Position(position.getX(), position.getY() - 1);
            case SOUTH: return new Position(position.getX(), position.getY() + 1);
            case EAST: return new Position(position.getX() + 1, position.getY());
            case WEST: return new Position(position.getX() - 1, position.getY());
            default: throw new IllegalStateException("Unknown direction");
        }
    }
}
