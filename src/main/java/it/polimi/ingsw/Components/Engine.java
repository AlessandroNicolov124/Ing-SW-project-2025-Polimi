package it.polimi.ingsw.Components;

import it.polimi.ingsw.Direction;

import java.util.stream.Collectors;

public abstract sealed class Engine extends Component permits SingleEngine, DoubleEngine {
    protected int power;
    protected int energyConsumption;

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

    /**
     * Verifica se un motore nella direzione opposta a quella specificata non ha un connettore
     * Un cannone può sparare solo in una direzione dove non ha connettori
     * o dove ha un connettore di tipo nullo (NullConnectorType).
     *
     * @param componentDirection La direzione verso cui il motore si muove
     * @return true se il motore può sparare nella direzione opposta a quella specificata, false altrimenti
     */
    public boolean validateEngineDirection(Direction componentDirection) {
        // Ottieni il connettore nella direzione specificata
        Connector connector = getConnectorByDirection(componentDirection.getOpposite());

        // Se non esiste un connettore in quella direzione, è possibile sparare
        if (connector == null) {
            return true;
        }
        return switch(connector.getConnectorType()){
            case NullConnectorType c -> true;
            default -> false;
        };
    }

    public abstract int getEffectivePower();


}
