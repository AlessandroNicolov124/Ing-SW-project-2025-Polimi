package it.polimi.ingsw.network.messages.shipbuilding;

import it.polimi.ingsw.network.messages.RequestMessage;
import it.polimi.ingsw.network.socket.server.SocketClientHandler;

public class GetPlayerTokenListRequest implements RequestMessage {

    private static final long serialVersionUID = 1L;
    private final String playerName;

    public GetPlayerTokenListRequest(String playerName) {
        this.playerName = playerName;
    }


    @Override
    public void handleServer(SocketClientHandler handler) {
        try {
            handler.getPlayerTokenList(playerName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
}
