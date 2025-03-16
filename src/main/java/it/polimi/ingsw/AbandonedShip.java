package it.polimi.ingsw;

public class AbandonedShip extends AdventureCard{

    public AbandonedShip(){

    }
    public AbandonedShip(int copies, int duration) {
        super(copies, duration);
    }

    @Override
    int additionalPls(){
        return 0;
    }

    @Override
    void resolveEvent(Player p) {

    }

    @Override
    void effect(){

    }


}