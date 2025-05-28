package it.polimi.ingsw.Components;

import java.io.Serializable;

public sealed abstract class ConnectorType implements Serializable permits SingleConnectorType, DoubleConnectorType, UniversalConnectorType, NullConnectorType {

    public abstract boolean isCompatibleWith(ConnectorType connectorType);
        
}
