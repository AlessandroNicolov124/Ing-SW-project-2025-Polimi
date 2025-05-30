package it.polimi.ingsw.network.rmi.messages;

import java.util.UUID;

import it.polimi.ingsw.network.client.listeners.GameListener;

public record PlayerJoinedMessage(UUID matchId, String playerName) implements UpdateMessage{
 
    @Override
    public void update(GameListener listener, int localPlayerId) {
        listener.playerJoinedMatch(matchId, playerName);
    }

}