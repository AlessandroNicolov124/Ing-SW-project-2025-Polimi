package it.polimi.ingsw.Components;

public sealed abstract class ConnectorType permits SingleConnectorType, DoubleConnectorType, UniversalConnectorType, NullConnectorType {

    public abstract boolean isCompatibleWith(ConnectorType connectorType);
        
}
