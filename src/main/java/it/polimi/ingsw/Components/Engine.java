package it.polimi.ingsw.Components;

import it.polimi.ingsw.Direction;

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

    public int getEnergyConsumption() {
        return energyConsumption;
    }

    public boolean validateEngineDirection(Direction direction) {
        for(Connector conn : connections){
            if(conn.getDirection() == direction.getOpposite()){
                switch(conn.getConnectorType()){
                    case NullConnectorType nct:
                        return true;
                    default:
                        return false;
                }
            }
        }
        return false;
    }

}
