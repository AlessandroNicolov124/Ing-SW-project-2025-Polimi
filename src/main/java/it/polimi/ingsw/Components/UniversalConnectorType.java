package it.polimi.ingsw.Components;

/** UniversalConnectorType represents a connector type that is compatible with all other types of connectors except NullConnectorType.
 * This class is used to represent a universal connector in the game.
 */
public final class UniversalConnectorType extends ConnectorType {

    @Override
    public boolean isCompatibleWith(ConnectorType other) {
        return switch (other) {
            case NullConnectorType n -> false;
            default -> true;
        };
    }
    
}