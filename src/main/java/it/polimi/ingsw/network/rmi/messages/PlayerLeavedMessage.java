package it.polimi.ingsw.network.rmi.messages;

import it.polimi.ingsw.network.client.listeners.GameListener;

public record PlayerLeavedMessage(String playerName) implements UpdateMessage {

    @Override
    public void update(GameListener listener, int localPlayerId) {
        listener.playerLeftMatch(playerName);
    }
    
}
