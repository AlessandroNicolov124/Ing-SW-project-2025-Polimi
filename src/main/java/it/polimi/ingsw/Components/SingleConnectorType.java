package it.polimi.ingsw.Components;

public final class SingleConnectorType extends ConnectorType {

    @Override
    public boolean isCompatibleWith(ConnectorType other) {
        return switch (other) {
            case UniversalConnectorType u -> true;
            case SingleConnectorType s -> true;
            default -> false;
        };
    }
    
}