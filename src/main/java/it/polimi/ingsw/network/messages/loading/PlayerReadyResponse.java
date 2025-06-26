package it.polimi.ingsw.network.messages.loading;

import java.util.UUID;

import it.polimi.ingsw.network.client.listeners.GameListener;
import it.polimi.ingsw.network.messages.ResponseMessage;

public class PlayerReadyResponse implements ResponseMessage {

    private static final long serialVersionUID = 1L;
    private final UUID matchId;
    private final int readyPlayers;
    private final int totalPlayers;
    private final boolean success;
    private final String errorMessage;

    
    public PlayerReadyResponse(UUID matchId, int readyPlayers, int totalPlayers, boolean success, String errorMessage) {
        this.matchId = matchId;
        this.readyPlayers = readyPlayers;
        this.totalPlayers = totalPlayers;
        this.success = success;
        this.errorMessage = errorMessage;
    }

    public void handleClient(GameListener listener) {
        if (success) {
            listener.playerReady(matchId, readyPlayers, totalPlayers);
        } else {
            listener.playerReadyFailed(errorMessage);
        }
    }

}