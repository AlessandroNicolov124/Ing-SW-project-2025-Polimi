package it.polimi.ingsw.Components;

public class DoubleEngine extends Engine {

    public DoubleEngine(int power, int energyConsumption) {
        super(power, energyConsumption);
    }

    @Override
    public String getType() {
        return "DoubleEngine";
    }

}