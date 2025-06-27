package it.polimi.ingsw.Components;

/**
 * DoubleConnectorType is a type of ConnectorType that represents a connector that can connect to another connector.
 * It is compatible with UniversalConnectorType and DoubleConnectorType.
 */
public final class DoubleConnectorType extends ConnectorType {

    @Override
    public boolean isCompatibleWith(ConnectorType other) {
        return switch (other) {
            case UniversalConnectorType u -> true;
            case DoubleConnectorType d -> true;
            default -> false;
        };
    }
    
}
