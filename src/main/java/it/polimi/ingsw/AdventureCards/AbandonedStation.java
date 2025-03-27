package it.polimi.ingsw.AdventureCards;

import it.polimi.ingsw.Player;

public class AbandonedStation extends AdventureCard {

    public AbandonedStation(){

    }

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
    public void effect(){

    }
}