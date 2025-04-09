package it.polimi.ingsw.Components;

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
