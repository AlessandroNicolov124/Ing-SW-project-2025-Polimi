package it.polimi.ingsw.Components;

import it.polimi.ingsw.Direction;
import java.io.Serializable;

/**
 * This class represents a connector that can be used to connect components in a spaceship.
 * It contains the direction of the connector and its type, and provides methods to check compatibility with other connectors.
 */
public class Connector implements Serializable {
    private Direction direction;
    private ConnectorType connectorType;

    public Connector(Direction direction, ConnectorType connectorType) {
        this.direction = direction;
        this.connectorType = connectorType;
    }

    public ConnectorType getConnectorType() {
        return connectorType;
    }

    public void setConnectorType(ConnectorType connectorType) {
        this.connectorType = connectorType;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    /**
     * check if this connector is compatible with another connector.
     * @param other the other connector to check compatibility with
     */
    public boolean isCompatibleWith(Connector other) {
        if (other == null){
            System.out.println("   other connector is null");
            return false;
        }
        return this.connectorType.isCompatibleWith(other.connectorType);
    }
}
