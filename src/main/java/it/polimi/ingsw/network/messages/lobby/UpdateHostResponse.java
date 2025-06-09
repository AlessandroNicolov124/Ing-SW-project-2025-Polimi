package it.polimi.ingsw.network.messages.lobby;

import java.util.UUID;

import it.polimi.ingsw.network.client.listeners.GameListener;
import it.polimi.ingsw.network.messages.ResponseMessage;

public class UpdateHostResponse implements ResponseMessage {

    private static final long serialVersionUID = 1L;
    private final UUID matchId;
    private final String newHostName;
    private final int playerId;

    public UpdateHostResponse(UUID matchId, int playerId, String newHostName) {
        this.matchId = matchId;
        this.newHostName = newHostName;
        this.playerId = playerId;
    }

    public UUID getMatchId() {
        return matchId;
    }

    public int getPlayerId() {
        return playerId;
    }

    public String getNewHostName() {
        return newHostName;
    }

    @Override
    public void handleClient(GameListener listener) {
        listener.updateHost(matchId, playerId, newHostName);
    }
    
}
