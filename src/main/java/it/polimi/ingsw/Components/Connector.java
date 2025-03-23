package it.polimi.ingsw.Components;

import it.polimi.ingsw.Direction;

// Classe Connector
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

    //Verifica se questo connettore è compatibile con un altro
    public boolean isCompatibleWith(Connector other) {
        if (other == null) {
            return false;
        }

        return connectorType == ConnectorType.UNIVERSAL ||
                other.connectorType == ConnectorType.UNIVERSAL ||
                connectorType == other.connectorType;
    }
}
