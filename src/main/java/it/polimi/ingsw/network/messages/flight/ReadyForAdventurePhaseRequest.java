package it.polimi.ingsw.network.messages.flight;

import it.polimi.ingsw.network.messages.RequestMessage;
import it.polimi.ingsw.network.socket.server.SocketClientHandler;
import java.util.UUID;

public class ReadyForAdventurePhaseRequest implements RequestMessage {
    private static final long serialVersionUID = 1L;
    private final UUID matchId;
    private final int playerId;

    public ReadyForAdventurePhaseRequest(UUID matchId, int playerId) {
        this.matchId = matchId;
        this.playerId = playerId;
    }

    @Override
    public void handleServer(SocketClientHandler handler) {
        try {
            handler.readyForAdventurePhase(matchId, playerId);
        } catch (Exception e) {
            System.err.println("Error handling ready for adventure phase request: " + e.getMessage());
        }
    }

}