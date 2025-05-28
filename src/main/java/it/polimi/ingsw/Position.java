package it.polimi.ingsw;

import java.io.Serializable;

public class Position implements Serializable {

    public int x;
    public int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Position)) return false;
        Position other = (Position) obj;
        return x == other.x && y == other.y;
    }

    //restituisce la posizione adiacente a quella specificata, in base alla direzione
    public Position getAdjacentPosition(Direction direction) {
        switch (direction) {
            case NORTH: return new Position(this.getX(), this.getY() - 1);
            case SOUTH: return new Position(this.getX(), this.getY() + 1);
            case EAST: return new Position(this.getX() + 1, this.getY());
            case WEST: return new Position(this.getX() - 1, this.getY());
            default: throw new IllegalStateException("Unknown direction");
        }
    }

    @Override
    public String toString() {
        return "Position [x=" + x + ", y=" + y + "]";
    }
}
