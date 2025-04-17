package it.polimi.ingsw.Components;

// Classe Engine
public abstract class Engine extends Component {
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
