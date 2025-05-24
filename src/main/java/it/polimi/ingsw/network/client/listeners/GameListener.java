package it.polimi.ingsw.network.client.listeners;


import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.network.socket.messages.RotationType;
import it.polimi.ingsw.Position;

public interface GameListener {
    
    void displayRefresh();
    void componentPlaced(Component component, Position position);
    void componentRemoved(Position position);
    void componentRotated(Position position, RotationType rotationType);
    void shipValidated(boolean isValid, String message);

}