package it.polimi.ingsw.network.messages.lobby;

import it.polimi.ingsw.network.messages.RequestMessage;
import it.polimi.ingsw.network.socket.server.SocketClientHandler;

public class AddPlayerRequest implements RequestMessage {

    private static final long serialVersionUID = 1L;
    private final String playerName; 


    public AddPlayerRequest(String playerName) {
        this.playerName = playerName;
    }

    public String getPlayerName() {
        return this.playerName;
    }

    @Override
    public void handleServer(SocketClientHandler handler) {
        try {
            handler.createPlayer(playerName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
