package it.polimi.ingsw.network.messages.loading;

import java.util.UUID;

import it.polimi.ingsw.network.client.listeners.GameListener;
import it.polimi.ingsw.network.messages.ResponseMessage;

public class PlayerReadyResponse implements ResponseMessage {

    private static final long serialVersionUID = 1L;
    private final UUID matchId;
    private final int readyPlayers;
    private final int totalPlayers;

    
    public PlayerReadyResponse(UUID matchId, int readyPlayers, int totalPlayers) {
        this.matchId = matchId;
        this.readyPlayers = readyPlayers;
        this.totalPlayers = totalPlayers;
    }

    public UUID getMatchId() {
        return matchId;
    }

    public int getReadyPlayers() {
        return readyPlayers;
    }

    public int getTotalPlayers() {
        return totalPlayers;
    }

    public void handleClient(GameListener listener) {
        listener.playerReady(matchId, readyPlayers, totalPlayers);
    }

}