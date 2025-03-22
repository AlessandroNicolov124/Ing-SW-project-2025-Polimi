package it.polimi.ingsw;

public abstract class Alien extends Crew{
    public Alien() {
        super();
    }
    public Alien(int count) {
        super(count);
    }

    public abstract void applyAbility(ShipBuilder ship);
}

