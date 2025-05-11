package it.polimi.ingsw.Components;

import it.polimi.ingsw.Direction;

public class Connector {
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

    public boolean isCompatibleWith(Connector other) {
        if (other == null){
            System.out.println("   other connector is null");
            return false;
        }
        return this.connectorType.isCompatibleWith(other.connectorType);
    }
}
