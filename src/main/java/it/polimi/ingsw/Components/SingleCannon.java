package it.polimi.ingsw.Components;

public class SingleCannon extends Cannon {
    public SingleCannon() {
        super(1,1);
    }

    @Override
    public String getType() {
        return "SingleCannon";
    }

}
