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

    public PlayersInMatchResponse(UUID matchId, List<String> playerNames, int hostId) {
        this.matchId = matchId;
        this.playerNames = playerNames;
        this.hostId = hostId;
    }

    public UUID getMatchId() {
        return matchId;
    }

    public List<String> getPlayerNames() {
        return playerNames;
    }

    @Override
    public void handleClient(GameListener listener) {
        listener.playersInMatch(matchId, playerNames, hostId);
    }
    
}
