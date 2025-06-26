package it.polimi.ingsw.network.messages.lobby;

import java.util.UUID;

import it.polimi.ingsw.network.messages.RequestMessage;
import it.polimi.ingsw.network.socket.server.SocketClientHandler;

public class LeaveMatchRequest implements RequestMessage {

    private static final long serialVersionUID = 1L;
    private final String playerName;
    private final UUID matchId;
    private final int playerId;

    public LeaveMatchRequest(String playerName, UUID matchId, int playerId) {
        this.playerName = playerName;
        this.matchId = matchId;
        this.playerId = playerId;
    }

    @Override
    public void handleServer(SocketClientHandler handler) {
        try {
            handler.leaveMatch(matchId, playerId, playerName);
        } catch (Exception e) {
            e.printStackTrace();

       }
    }

    
}