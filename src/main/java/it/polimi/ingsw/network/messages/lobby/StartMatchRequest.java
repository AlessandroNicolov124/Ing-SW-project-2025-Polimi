package it.polimi.ingsw.network.messages.lobby;

import java.util.UUID;

import it.polimi.ingsw.network.messages.RequestMessage;
import it.polimi.ingsw.network.socket.server.SocketClientHandler;

public class StartMatchRequest implements RequestMessage {

    private static final long serialVersionUID = 1L;

    private final UUID matchId;
    private final int playerId;
    private final String username;

    public StartMatchRequest(UUID matchId, int playerId, String username) {
        this.matchId = matchId;
        this.playerId = playerId;
        this.username = username;
    }

    public UUID getMatchId() {
        return matchId;
    }

    public int getPlayerId() {
        return playerId;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public void handleServer(SocketClientHandler handler) {
        try {
            handler.startMatch(matchId, playerId, username);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}