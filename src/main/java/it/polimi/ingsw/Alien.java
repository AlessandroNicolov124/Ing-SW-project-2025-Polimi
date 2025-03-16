package it.polimi.ingsw;

public abstract class Alien extends Crew{
    public Alien() {
        super();
    }
    public Alien(String type, int count) {
        super(type, count);
    }

    public abstract void applyAbility(ShipBuilder ship);
}

