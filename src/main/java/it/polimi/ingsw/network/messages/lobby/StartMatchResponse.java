package it.polimi.ingsw.network.messages.lobby;

import java.util.UUID;

import it.polimi.ingsw.network.client.listeners.GameListener;
import it.polimi.ingsw.network.messages.ResponseMessage;

public class StartMatchResponse implements ResponseMessage {

    private static final long serialVersionUID = 1L;
    private final UUID matchId;
    private final String username;
    private final boolean success;
    private final String errorMessage;

    public StartMatchResponse(UUID matchId, String username, boolean success, String errorMessage) {
        this.matchId = matchId;
        this.username = username;
        this.success = success;
        this.errorMessage = errorMessage;
    }

    @Override
    public void handleClient(GameListener listener) {
        if (success) {
            listener.matchStarted(matchId, username);
        } else {
            listener.matchStartFailed(errorMessage);
        }
    }
    
}