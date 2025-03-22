package it.polimi.ingsw.Components;

public class DoubleCannon extends Cannon {
    public DoubleCannon() {
        super();
    }
    public DoubleCannon(int firePower, int energyConsumption) {
        super(firePower, energyConsumption);
    }

    @Override
    public String getType() {
        return "DoubleCannon";
    }

}
