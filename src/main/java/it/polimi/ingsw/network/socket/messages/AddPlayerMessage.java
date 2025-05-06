package it.polimi.ingsw.network.socket.messages;

import it.polimi.ingsw.network.socket.server.SocketClientHandler;

public class AddPlayerMessage implements ServerMessage {

    private static final long serialVersionUID = 1L;
    private final String username;

    public AddPlayerMessage(String username) { 
        this.username = username; 
    }

    public String username() { 
        return username; 
    }

    @Override
    public void handleServer(SocketClientHandler h) {
        int id = h.createPlayer(username);
        h.sendUpdate(new PlayerAddedMessage(id, username));
    }
}