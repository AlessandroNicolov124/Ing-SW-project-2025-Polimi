package it.polimi.ingsw.network.messages.loading;

import java.util.UUID;

import it.polimi.ingsw.network.client.listeners.GameListener;
import it.polimi.ingsw.network.messages.ResponseMessage;

public class LoadingStatusResponse implements ResponseMessage {

    private static final long serialVersionUID = 1L;

    private final UUID matchId;
    private final int playersReady;
    private final int totalPlayers;
    
    public LoadingStatusResponse(UUID matchId, int playersReady, int totalPlayers) {
        this.matchId = matchId;
        this.playersReady = playersReady;
        this.totalPlayers = totalPlayers;
    }

    public UUID getMatchId() {
        return matchId;
    }

    public int getPlayersReady() {
        return playersReady;
    }

    public int getTotalPlayers() {
        return totalPlayers;
    }

    @Override
    public void handleClient(GameListener listener) {
        listener.loadingStatus(matchId, playersReady, totalPlayers);
    }
    
}
