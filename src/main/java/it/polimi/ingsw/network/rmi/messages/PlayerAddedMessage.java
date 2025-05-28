package it.polimi.ingsw.network.rmi.messages;

import it.polimi.ingsw.network.client.listeners.GameListener;

public record PlayerAddedMessage(int playerId) implements UpdateMessage {

    @Override
    public void update(GameListener listener, int localPlayerId) {
        //System.out.println("[MSG] PlayerAddedMessage for player" + playerId);
    }
    
}
