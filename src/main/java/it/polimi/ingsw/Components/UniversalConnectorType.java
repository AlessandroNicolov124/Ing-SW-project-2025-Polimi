package it.polimi.ingsw.Components;

public final class UniversalConnectorType extends ConnectorType {
    private static final long serialVersionUID = 1L;

    @Override
    public boolean isCompatibleWith(ConnectorType other) {
        return switch (other) {
            case NullConnectorType n -> false;
            default -> true;
        };
    }
    
}