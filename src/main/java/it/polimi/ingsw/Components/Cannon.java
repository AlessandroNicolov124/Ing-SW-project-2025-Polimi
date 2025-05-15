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

    /**
     * Verifica se un cannone può sparare nella direzione specificata.
     * Un cannone può sparare solo in una direzione dove non ha connettori
     * o dove ha un connettore di tipo nullo (NullConnectorType).
     *
     * @param firingDirection La direzione in cui si vuole sparare
     * @return true se il cannone può sparare nella direzione specificata, false altrimenti
     */
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


