package it.polimi.ingsw.network.messages.loading;

import java.util.UUID;

import it.polimi.ingsw.network.messages.RequestMessage;
import it.polimi.ingsw.network.socket.server.SocketClientHandler;

public class PlayerReadyRequest implements RequestMessage {
 
    private static final long serialVersionUID = 1L;

    private final UUID matchId;
    private final int playerId;

    public PlayerReadyRequest(UUID matchId, int playerId) {
        this.matchId = matchId;
        this.playerId = playerId;
    }

    public UUID getMatchId() {
        return matchId;
    }

    public int getPlayerId() {
        return playerId;
    }

    @Override
    public void handleServer(SocketClientHandler handler) {
        try {
            handler.playerReady(matchId, playerId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
