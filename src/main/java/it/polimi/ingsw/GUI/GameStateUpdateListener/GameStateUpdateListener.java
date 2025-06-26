package it.polimi.ingsw.GUI.GameStateUpdateListener;

import it.polimi.ingsw.Position;
import it.polimi.ingsw.Components.Component;

import it.polimi.ingsw.network.messages.RotationType;

public interface GameStateUpdateListener {
    void gameStateUpdate();
    void gameComponentPicked(Component component);
    void gameComponentPlaced(Component component, Position position);
    void gameComponentRemoved(Position position);
    void gameComponentRotated(Position position, RotationType rotationType);
    void gameShipValidated(boolean isValid, String message);


    void gameComponentPickedFailed(String message);
    void gameComponentPlacedFailed(String message);
    void gameComponentRemovedFailed(String message);
    void gameComponentRotatedFailed(String message);
    void gameShipValidationFailed(String message);

    void gamePlayerReadyForFlightSuccess(String message);
    void gamePlayerReadyForFlightFailed(String message);
}