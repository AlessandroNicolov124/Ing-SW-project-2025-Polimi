package it.polimi.ingsw.Components;

public class SingleEngine extends Engine {

    public SingleEngine(int power, int energyConsumption) {
        super(power, energyConsumption);
    }

    public SingleEngine() {
        super(0,0);
    }
    @Override
    public String getType() {
        return "SingleEngine";
    }

}
