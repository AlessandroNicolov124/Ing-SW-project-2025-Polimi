package it.polimi.ingsw.network.messages.flight;

import it.polimi.ingsw.network.client.listeners.GameListener;
import it.polimi.ingsw.network.messages.ResponseMessage;
import java.util.UUID;

public class FlightStartResponse implements ResponseMessage {
    private static final long serialVersionUID = 1L;
    private final UUID matchId;

    public FlightStartResponse(UUID matchId) {
        this.matchId = matchId;
    }

    public UUID getMatchId() {
        return matchId;
    }

    @Override
    public void handleClient(GameListener listener) {
        listener.allPlayersReadyForFlight(matchId);
    }
}