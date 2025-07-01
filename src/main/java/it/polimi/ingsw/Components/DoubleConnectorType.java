package it.polimi.ingsw.Components;

public final class DoubleConnectorType extends ConnectorType {
    private static final long serialVersionUID = 1L;

    @Override
    public boolean isCompatibleWith(ConnectorType other) {
        return switch (other) {
            case UniversalConnectorType u -> true;
            case DoubleConnectorType d -> true;
            default -> false;
        };
    }
    
}
