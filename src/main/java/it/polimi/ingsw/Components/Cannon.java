package it.polimi.ingsw.Components;

import it.polimi.ingsw.*;

/**
 * Cannon is an abstract class representing a cannon component in the game.
 *
 */
public sealed abstract class Cannon extends Component permits SingleCannon, DoubleCannon {

    //la direzione di fuoco del cannone corrisponde alla direzione del componente
    protected int firePower;
    protected int energyConsumption;

    public Cannon(int power, int energyConsumption) {
        super();
        this.firePower = power;
        this.energyConsumption = energyConsumption;
    }

    /**
     * Check if the cannon can fire in the specified direction.
     * it is possible to fire if there is no connector in the specified direction or if the connector is a NullConnectorType.
     * @param firingDirection the direction in which the cannon is trying to fire
     * @return true if the cannon can fire, false otherwise
     **/
    public boolean validateFiringDirection(Direction firingDirection) {
        // Ottieni il connettore nella direzione specificata
        Connector connector = getConnectorByDirection(firingDirection);

        // Se non esiste un connettore in quella direzione, è possibile sparare
        if (connector == null) {
            return true;
        }
        return switch(connector.getConnectorType()){
            case NullConnectorType c -> true;
            default -> false;
        };
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


