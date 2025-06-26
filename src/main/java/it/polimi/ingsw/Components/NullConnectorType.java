package it.polimi.ingsw.Components;

/** NullConnectorType is a type of ConnectorType that represents a connector that does not connect to anything. */
public final class NullConnectorType extends ConnectorType {

    @Override
    public boolean isCompatibleWith(ConnectorType other) {
        return switch (other) {
            case NullConnectorType n -> true; //smooth side
            default -> false;
        };
    }

}
