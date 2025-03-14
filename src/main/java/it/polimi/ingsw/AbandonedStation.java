package it.polimi.ingsw;

public class AbandonedStation extends AdventureCard{

    public AbandonedStation(int copies, int duration) {
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