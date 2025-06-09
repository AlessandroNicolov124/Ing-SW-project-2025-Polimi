package it.polimi.ingsw.network.messages.lobby;

import java.util.UUID;

import it.polimi.ingsw.network.client.listeners.GameListener;
import it.polimi.ingsw.network.messages.ResponseMessage;

public class JoinMatchResponse implements ResponseMessage {

    private static final long serialVersionUID = 1L;
    private final UUID matchId;
    private final String username;
    
    public JoinMatchResponse(UUID matchId, String username) {
        this.matchId = matchId;
        this.username = username;
    }

    public UUID getMatchId() {
        return matchId;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public void handleClient(GameListener listener) {
        listener.playerJoinedMatch(matchId, username);
    }
    
}
