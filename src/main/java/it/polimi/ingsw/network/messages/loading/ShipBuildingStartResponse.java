package it.polimi.ingsw.network.messages.loading;

import java.util.UUID;

import it.polimi.ingsw.network.client.listeners.GameListener;
import it.polimi.ingsw.network.messages.ResponseMessage;

public class ShipBuildingStartResponse implements ResponseMessage {

    private static final long serialVersionUID = 1L;

    private final UUID matchId;
    private final int readyPlayers;
    
    public ShipBuildingStartResponse(UUID matchId, int readyPlayers) {
        this.matchId = matchId;
        this.readyPlayers = readyPlayers;
    }

    public UUID getMatchId() {
        return matchId;
    }

    public int getReadyPlayers() {
        return readyPlayers;
    }

    @Override
    public void handleClient(GameListener listener) {
        listener.shipBuildingStart(matchId, readyPlayers);
    }
    
}
