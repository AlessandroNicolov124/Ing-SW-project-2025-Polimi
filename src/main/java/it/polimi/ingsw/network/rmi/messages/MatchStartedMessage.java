package it.polimi.ingsw.network.rmi.messages;

import java.util.UUID;

import it.polimi.ingsw.network.client.listeners.GameListener;

public record MatchStartedMessage(UUID matchId, String message) implements UpdateMessage {

    @Override
    public void update(GameListener listener, int localPlayerId) {
        listener.matchStarted(matchId, message);
    }
}
