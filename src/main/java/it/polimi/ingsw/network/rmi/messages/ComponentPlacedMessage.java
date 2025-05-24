package it.polimi.ingsw.network.rmi.messages;


import it.polimi.ingsw.Position;
import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.network.client.listeners.GameListener;

public record ComponentPlacedMessage(int playerID, Component component, Position position, boolean success) implements UpdateMessage {
    
    @Override
    public void update(GameListener listener, int localPlayerId) {
        if (playerID == localPlayerId) {
            if (success) {
                listener.componentPlaced(component, position);
            }
            listener.displayRefresh();
        }
    }   
}