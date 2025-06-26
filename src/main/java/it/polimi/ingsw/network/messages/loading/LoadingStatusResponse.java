package it.polimi.ingsw.network.messages.loading;

import java.util.UUID;

import it.polimi.ingsw.network.client.listeners.GameListener;
import it.polimi.ingsw.network.messages.ResponseMessage;

public class LoadingStatusResponse implements ResponseMessage {

    private static final long serialVersionUID = 1L;

    private final UUID matchId;
    private final int playersReady;
    private final int totalPlayers;
    private final boolean success;
    private final String errorMessage;
    
    public LoadingStatusResponse(UUID matchId, int playersReady, int totalPlayers, boolean success, String errorMessage) {
        this.matchId = matchId;
        this.playersReady = playersReady;
        this.totalPlayers = totalPlayers;
        this.success = success;
        this.errorMessage = errorMessage;
    }

    @Override
    public void handleClient(GameListener listener) {
        if (success) {
            listener.loadingStatus(matchId, playersReady, totalPlayers);
        } else {
            listener.loadingStatusFailed(errorMessage);
        }
    }
    
}
