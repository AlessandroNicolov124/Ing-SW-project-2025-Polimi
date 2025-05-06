package it.polimi.ingsw.network.socket.messages;

import java.io.IOException;
import it.polimi.ingsw.network.socket.server.SocketClientHandler;
import it.polimi.ingsw.network.socket.client.SocketClient;

public class PlayerAddedMessage implements ClientMessage {
    private static final long serialVersionUID = 1L;
    private final int playerId;
    private final String username;

    public PlayerAddedMessage(int id, String u) {
        this.playerId = id; 
        this.username = u;
    }

    public int playerId() {
        return playerId;
    }
    
    public String username() {
        return username;
    }

    @Override            
    public void handleClient(SocketClient cli) {
        cli.setPlayerId(playerId);
        cli.showUpdate("Welcome " + username + " (id = " + playerId + ")");
    }
    
    // Nothing to do here because this message is sent to the client
    @Override
    public void handle(SocketClientHandler handler) throws IOException {

    }
}