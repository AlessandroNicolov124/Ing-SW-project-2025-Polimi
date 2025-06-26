package it.polimi.ingsw.network.messages.lobby;

import java.util.UUID;

import it.polimi.ingsw.network.client.listeners.GameListener;
import it.polimi.ingsw.network.messages.ResponseMessage;

public class UpdateHostResponse implements ResponseMessage {

    private static final long serialVersionUID = 1L;
    private final UUID matchId;
    private final String newHostName;
    private final int playerId;
    private final boolean success;
    private final String errorMessage;

    public UpdateHostResponse(UUID matchId, int playerId, String newHostName, boolean success, String errorMessage) {
        this.matchId = matchId;
        this.newHostName = newHostName;
        this.playerId = playerId;
        this.success = success;
        this.errorMessage = errorMessage;
    }

    @Override
    public void handleClient(GameListener listener) {
        if (success) {
            listener.updateHost(matchId, playerId, newHostName);
        } else {
            listener.updateHostFailed(errorMessage);
        }
    }
    
}
