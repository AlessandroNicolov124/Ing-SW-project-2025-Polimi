package it.polimi.ingsw.Components;

public final class NullConnectorType extends ConnectorType {

    @Override
    public boolean isCompatibleWith(ConnectorType other) {
        return switch (other) {
            case NullConnectorType n -> true; //smooth side
            default -> false;
        };
    }

}
