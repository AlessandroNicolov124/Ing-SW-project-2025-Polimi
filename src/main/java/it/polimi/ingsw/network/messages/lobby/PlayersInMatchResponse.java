package it.polimi.ingsw.network.messages.lobby;

import java.util.List;
import java.util.UUID;

import it.polimi.ingsw.network.client.listeners.GameListener;
import it.polimi.ingsw.network.messages.ResponseMessage;

public class PlayersInMatchResponse implements ResponseMessage {
    
    private static final long serialVersionUID = 1L;
    private final UUID matchId;
    private final List<String> playerNames;
    private final int hostId;
    private final boolean success;
    private final String errorMessage;

    public PlayersInMatchResponse(UUID matchId, List<String> playerNames, int hostId, boolean success, String errorMessage) {
        this.matchId = matchId;
        this.playerNames = playerNames;
        this.hostId = hostId;
        this.success = success;
        this.errorMessage = errorMessage;
    }

    @Override
    public void handleClient(GameListener listener) {
        if (success) {
            listener.playersInMatch(matchId, playerNames, hostId);
        } else {
            listener.playersInMatchFailed(errorMessage);
        }
    }
    
}
