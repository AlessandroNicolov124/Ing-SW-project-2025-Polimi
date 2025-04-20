package it.polimi.ingsw.Components;

public final class UniversalConnectorType extends ConnectorType {

    @Override
    public boolean isCompatibleWith(ConnectorType other) {
        return switch (other) {
            case NullConnectorType n -> false;
            default -> true;
        };
    }
    
}