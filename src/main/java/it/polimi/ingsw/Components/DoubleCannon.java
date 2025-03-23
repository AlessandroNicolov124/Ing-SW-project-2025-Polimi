package it.polimi.ingsw.Components;

public class DoubleCannon extends Cannon {
    public DoubleCannon() {
        super(2,2);
    }

    @Override
    public String getType() {
        return "DoubleCannon";
    }

}


