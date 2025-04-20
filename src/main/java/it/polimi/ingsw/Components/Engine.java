package it.polimi.ingsw.Components;

public abstract sealed class Engine extends Component permits SingleEngine, DoubleEngine {
    private int power;
    private int energyConsumption;

    public Engine(int power, int energyConsumption) {
        super();
        this.power = power;
        this.energyConsumption = energyConsumption;
    }

    public int getPower() {
        return power;
    }

    public boolean isDoubleEngine(){return false;}

    public int getEnergyConsumption() {
        return energyConsumption;
    }

}
