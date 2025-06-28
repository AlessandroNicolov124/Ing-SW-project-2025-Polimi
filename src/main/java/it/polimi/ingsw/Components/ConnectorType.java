package it.polimi.ingsw.Components;

import java.io.Serializable;

/**
 * Represents a type of connector of the component in the game.
 * This is an abstract class that defines the common behavior for different types of connectors.
 */
public sealed abstract class ConnectorType implements Serializable permits SingleConnectorType, DoubleConnectorType, UniversalConnectorType, NullConnectorType {

    /**
     * Checks if the connector type is compatible with another connector type.
     *
     * @param connectorType the other connector type to check compatibility with
     * @return true if compatible, false otherwise
     */
    public abstract boolean isCompatibleWith(ConnectorType connectorType);
        
}
