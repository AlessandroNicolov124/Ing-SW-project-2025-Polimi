package it.polimi.ingsw.network.messages.lobby;

import java.util.UUID;

import it.polimi.ingsw.network.client.listeners.GameListener;
import it.polimi.ingsw.network.messages.ResponseMessage;

public class LeaveMatchResponse implements ResponseMessage {

    private static final long serialVersionUID = 1L;
    private final UUID matchId;
    private final String playerName;
    private final int playerId;
    private final boolean success;
    private final String errorMessage;

    public LeaveMatchResponse(UUID matchId, int playerId, String playerName, Boolean success, String errorMessage) {
        this.matchId = matchId;
        this.playerName = playerName;
        this.playerId = playerId;
        this.success = success;
        this.errorMessage = errorMessage;
    }

    @Override
    public void handleClient(GameListener listener) {
        listener.playerLeftMatch(matchId, playerId, playerName);
    }
    
}
