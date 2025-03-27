package it.polimi.ingsw.Components;

import it.polimi.ingsw.Visitors.Visitor;

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

    public int getEnergyConsumption() {
        return energyConsumption;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }


    public abstract String getType();
}
