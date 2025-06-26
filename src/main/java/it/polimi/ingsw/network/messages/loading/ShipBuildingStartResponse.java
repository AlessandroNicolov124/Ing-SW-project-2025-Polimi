package it.polimi.ingsw.network.messages.loading;

import java.util.UUID;

import it.polimi.ingsw.network.client.listeners.GameListener;
import it.polimi.ingsw.network.messages.ResponseMessage;

public class ShipBuildingStartResponse implements ResponseMessage {

    private static final long serialVersionUID = 1L;

    private final UUID matchId;
    private final int readyPlayers;
    private final boolean success;
    private final String errorMessage;
    
    public ShipBuildingStartResponse(UUID matchId, int readyPlayers, boolean success, String errorMessage) {
        this.matchId = matchId;
        this.readyPlayers = readyPlayers;
        this.success = success;
        this.errorMessage = errorMessage;
    }

    @Override
    public void handleClient(GameListener listener) {
        if (success) {
            listener.shipBuildingStart(matchId, readyPlayers);
        } else {
            listener.shipBuildingStartFailed(errorMessage);
        }
    }
    
}
