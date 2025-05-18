package it.polimi.ingsw.network.rmi.messages;

import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.network.client.listeners.ShipBuildingListener;

public record PickComponentMessage(int playerId, Component component) implements UpdateMessage {
    
    @Override
    public void update(ShipBuildingListener listener, int localPlayerId) {
        //System.out.println("[MSG] PickComponentMessage for player" + playerId + " and component " + component);
    }
    
    
}
