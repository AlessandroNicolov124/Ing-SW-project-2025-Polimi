package it.polimi.ingsw.network.rmi.messages;

import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.network.client.listeners.GameListener;

public record PickComponentMessage(int playerId, Component component) implements UpdateMessage {
    
    @Override
    public void update(GameListener listener, int localPlayerId) {
        //System.out.println("[MSG] PickComponentMessage for player" + playerId + " and component " + component);
    }
    
    
}