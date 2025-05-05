package it.polimi.ingsw.network.socket.messages;

import java.io.IOException;

import it.polimi.ingsw.network.socket.server.SocketClientHandler;

public class AddPlayerMessage implements GenericMessage {
    private static final long serialVersionUID = 1L;
    private final String username;

    public AddPlayerMessage(String username) { 
        this.username = username; 
    }

    public String username() { 
        return username; 
    }

    @Override
    public void handle(SocketClientHandler h) throws IOException {
        int id = h.createPlayer(username);
        h.sendMessage(new PlayerAddedMessage(id, username));
        //h.broadcast(username + " joined the game!");
    }
}