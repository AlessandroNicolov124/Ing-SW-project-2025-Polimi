package it.polimi.ingsw.network.messages.lobby;

import java.util.UUID;

import it.polimi.ingsw.network.messages.RequestMessage;
import it.polimi.ingsw.network.socket.server.SocketClientHandler;

public class JoinMatchRequest implements RequestMessage {

    private final static long serialVersionUID = 1L;
    private final UUID matchId;
    private final int playerId;
    private final String username;

    public JoinMatchRequest(UUID matchId, int playerId, String username) {
        this.matchId = matchId;
        this.playerId = playerId;
        this.username = username;
    }

    @Override
    public void handleServer(SocketClientHandler handler) {
        try {
            handler.joinMatch(matchId, playerId, username);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
