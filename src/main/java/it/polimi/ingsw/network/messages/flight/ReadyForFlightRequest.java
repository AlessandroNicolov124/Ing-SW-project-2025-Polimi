package it.polimi.ingsw.network.messages.flight;

import java.util.UUID;

import it.polimi.ingsw.network.messages.RequestMessage;
import it.polimi.ingsw.network.socket.server.SocketClientHandler;

public class ReadyForFlightRequest implements RequestMessage {

    private static final long serialVersionUID = 1L;
    private final UUID matchId;
    private final int playerId;

    public ReadyForFlightRequest(UUID matchId, int playerId) {
        this.playerId = playerId;
        this.matchId = matchId;
    }

    public int getPlayerId() {
        return playerId;
    }

    public UUID getMatchId() {
        return matchId;
    }

    @Override
    public void handleServer(SocketClientHandler handler) {
        try {
            handler.readyForFlight(matchId, playerId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
