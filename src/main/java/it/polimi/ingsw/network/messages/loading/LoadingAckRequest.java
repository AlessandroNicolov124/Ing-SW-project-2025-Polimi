package it.polimi.ingsw.network.messages.loading;

import java.util.UUID;

import it.polimi.ingsw.network.messages.RequestMessage;
import it.polimi.ingsw.network.socket.server.SocketClientHandler;

public class LoadingAckRequest implements RequestMessage {

    private static final long serialVersionUID = 1L;

    private final UUID matchId;

    public LoadingAckRequest(UUID matchId) {
        this.matchId = matchId;
    }

    public UUID getMatchId() {
        return matchId;
    }

    @Override
    public void handleServer(SocketClientHandler handler) {
        try {
            handler.loadingAck(matchId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
