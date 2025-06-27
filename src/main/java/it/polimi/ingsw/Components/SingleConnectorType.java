package it.polimi.ingsw.Components;

/**
 * Represents a single connector type in the game.
 * This class extends ConnectorType and defines compatibility rules for single connectors.
 * It is compatible with other single connectors and universal connectors.
 */
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