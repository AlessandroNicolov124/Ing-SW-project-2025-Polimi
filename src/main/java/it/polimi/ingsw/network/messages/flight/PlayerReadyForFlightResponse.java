package it.polimi.ingsw.network.messages.flight;

import it.polimi.ingsw.network.client.listeners.GameListener;
import it.polimi.ingsw.network.messages.ResponseMessage;
import java.util.UUID;

public class PlayerReadyForFlightResponse implements ResponseMessage {
    private static final long serialVersionUID = 1L;
    private final UUID matchId;
    private final int playerId;
    private final String playerName;
    private final boolean shipValid;
    private final boolean success;
    private final String message;

    public PlayerReadyForFlightResponse(UUID matchId, int playerId, String playerName, boolean shipValid, boolean success, String message) {
        this.matchId = matchId;
        this.playerId = playerId;
        this.playerName = playerName;
        this.shipValid = shipValid;
        this.success = success;
        this.message = message;
    }

    @Override
    public void handleClient(GameListener listener) {
        if (success) {
            listener.playerReadyForFlight(matchId, playerId, playerName, shipValid);
        } else {
            listener.playerReadyForFlightFailed(message);
        }
    }
}