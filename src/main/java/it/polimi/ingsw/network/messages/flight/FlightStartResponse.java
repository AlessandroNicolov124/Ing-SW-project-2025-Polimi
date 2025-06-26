package it.polimi.ingsw.network.messages.flight;

import it.polimi.ingsw.network.client.listeners.GameListener;
import it.polimi.ingsw.network.messages.ResponseMessage;
import java.util.UUID;

public class FlightStartResponse implements ResponseMessage {
    private static final long serialVersionUID = 1L;
    private final UUID matchId;
    private final boolean success;
    private final String message;

    public FlightStartResponse(UUID matchId, boolean success, String message) {
        this.matchId = matchId;
        this.success = success;
        this.message = message;
    }

    @Override
    public void handleClient(GameListener listener) {
        if (success) {
            listener.allPlayersReadyForFlight(matchId);
        } else {
            listener.allPlayersReadyForFlightFailed(message);
        }
    }
}