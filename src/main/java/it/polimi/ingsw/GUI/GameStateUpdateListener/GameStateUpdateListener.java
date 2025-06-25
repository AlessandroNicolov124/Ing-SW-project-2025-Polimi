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

}