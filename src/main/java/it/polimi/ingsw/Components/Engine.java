package it.polimi.ingsw.Components;

import it.polimi.ingsw.Direction;

import java.util.stream.Collectors;

/**
 * Represents an abstract engine component in the game.
 * This class serves as a base for specific types of engines, such as SingleEngine and DoubleEngine.
 */
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
      * Checks if an engine in the opposite direction to the specified one does not have a connector.
      * A cannon can fire only in a direction where it has no connectors
      * or where it has a connector of type NullConnectorType.
      *
      * @param componentDirection The direction in which the engine moves
      * @return true if the engine can fire in the direction opposite to the specified one, false otherwise
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
