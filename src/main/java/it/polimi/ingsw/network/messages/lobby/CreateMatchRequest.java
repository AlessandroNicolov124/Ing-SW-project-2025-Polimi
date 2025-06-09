package it.polimi.ingsw.network.messages.lobby;

import it.polimi.ingsw.network.messages.RequestMessage;
import it.polimi.ingsw.network.socket.server.SocketClientHandler;

public class CreateMatchRequest implements RequestMessage {

    private static final long serialVersionUID = 1L;
    private final String matchName;
    private final int playerId;
    private final String playerName;

    public CreateMatchRequest(String matchName, int playerId, String playerName) {
        this.matchName = matchName;
        this.playerId = playerId;
        this.playerName = playerName;
    }

    public String getMatchName() {
        return matchName;
    }

    public int getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    @Override
    public void handleServer(SocketClientHandler handler) {
        handler.createMatch(matchName, playerId, playerName);
    }
    
}
