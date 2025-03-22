package it.polimi.ingsw.Components;

import it.polimi.ingsw.*;
import it.polimi.ingsw.Visitors.Visitor;

// Classe Engine
public abstract class Cannon extends Component {
    private int firePower;
    private int energyConsumption;

    public Cannon() {
        super();
        this.firePower = 0;
        this.energyConsumption = 0;
    }
    public Cannon(int firePower, int energyConsumption) {
        super();
        this.firePower = firePower;
        this.energyConsumption = energyConsumption;
    }

    public int getFirePower() {
        return firePower;
    }

    public void setFirePower(int firePower) {
        this.firePower = firePower;
    }
    public int getEnergyConsumption() {
        return energyConsumption;
    }
    public void setEnergyConsumption(int energyConsumption) {
        this.energyConsumption = energyConsumption;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public abstract String getType();
}


