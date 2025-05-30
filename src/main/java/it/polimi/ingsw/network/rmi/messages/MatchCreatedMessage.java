package it.polimi.ingsw.network.rmi.messages;

import java.util.UUID;

import it.polimi.ingsw.network.client.listeners.GameListener;

public record MatchCreatedMessage(UUID matchId, String matchName, String hostName) implements UpdateMessage{

    @Override
    public void update(GameListener listener, int localPlayerId) {
        listener.matchCreated(matchId, matchName, hostName);
    }
    
}
