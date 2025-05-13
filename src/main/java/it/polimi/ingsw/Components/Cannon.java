package it.polimi.ingsw.Components;

import it.polimi.ingsw.*;

public sealed abstract class Cannon extends Component permits SingleCannon, DoubleCannon {

    //la direzione di fuoco del cannone corrisponde alla direzione del componente
    protected int firePower;
    protected int energyConsumption;

    public Cannon(int power, int energyConsumption) {
        super();
        this.firePower = power;
        this.energyConsumption = energyConsumption;
    }

    public boolean validateFiringDirection(Direction firingDirection) {
        for(Connector conn : connections){
            if(conn.getDirection() == firingDirection){
                switch(conn.getConnectorType()){
                    case NullConnectorType nct:
                        return true;
                    default:
                        return false;
                }
            }
        }
        return true;
    }


    public int getFirePower() {
        return firePower;
    }

    public int getEnergyConsumption() {
        return energyConsumption;
    }
    public void setEnergyConsumption(int energyConsumption) {
        this.energyConsumption = energyConsumption;
    }

    public abstract boolean fire();

    public abstract double getEffectivePower();

}


