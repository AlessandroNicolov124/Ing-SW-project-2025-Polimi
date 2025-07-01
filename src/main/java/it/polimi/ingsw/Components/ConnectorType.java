package it.polimi.ingsw.Components;

import java.io.Serializable;

public sealed abstract class ConnectorType implements Serializable permits SingleConnectorType, DoubleConnectorType, UniversalConnectorType, NullConnectorType {
    private static final long serialVersionUID = 1L;

    public abstract boolean isCompatibleWith(ConnectorType connectorType);
        
}
