package it.polimi.ingsw.network.rmi.messages;


import it.polimi.ingsw.Position;
import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.network.client.listeners.ShipBuildingListener;

public record ComponentPlacedMessage(int playerID, Component component, Position position, boolean success) implements UpdateMessage {
    
    @Override
    public void update(ShipBuildingListener listener, int localPlayerId) {
        if (playerID == localPlayerId) {
            //System.out.println("[MSG] ComponentPlacedMessage for player " + playerID + " and component " + component + " at position " + position);
            listener.displayRefresh();
        }
    }   
}