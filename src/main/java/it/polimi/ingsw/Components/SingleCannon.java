package it.polimi.ingsw.Components;

public class SingleCannon extends Cannon {
    public SingleCannon() {
        super();
    }
    public SingleCannon(int firePower, int energyConsumption) {
        super(firePower, energyConsumption);
    }

    @Override
    public String getType() {
        return "SingleCannon";
    }
}
