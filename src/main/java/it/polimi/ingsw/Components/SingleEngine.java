package it.polimi.ingsw.Components;

public class SingleEngine extends Engine {

    public SingleEngine(int power, int energyConsumption) {
        super(power, energyConsumption);
    }

    @Override
    public String getType() {
        return "SingleEngine";
    }

}
