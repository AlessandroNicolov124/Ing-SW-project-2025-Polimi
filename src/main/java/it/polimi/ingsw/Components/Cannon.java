package it.polimi.ingsw.Components;

import it.polimi.ingsw.*;

public sealed abstract class Cannon extends Component permits SingleCannon, DoubleCannon {
    private static final long serialVersionUID = 1L;

    //Fire direction Matches component direction
    protected int firePower;
    protected int energyConsumption;

    public Cannon(int power, int energyConsumption) {
        super();
        this.firePower = power;
        this.energyConsumption = energyConsumption;
    }

    /**
     * Tests whether a cannon can fire in the specified direction.
     * A cannon can only fire in a direction where it has no connector
     * or where it has a NullConnectorType connector.
     *
     * @param firingDirection La direzione in cui si vuole sparare
     * @return true se il cannone può sparare nella direzione specificata, false altrimenti
     */
    public boolean validateFiringDirection(Direction firingDirection) {
        // Obtaining connector in a give direction
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

    @Override
    protected String calculateComponentString() {
        String s = this.getClass().getSimpleName().toLowerCase() + "_";
        s += getConnectionsString();
        return s;
    }

}


