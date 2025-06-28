package it.polimi.ingsw.network.messages.shipbuilding;

import it.polimi.ingsw.network.messages.RequestMessage;
import it.polimi.ingsw.network.socket.server.SocketClientHandler;

public class GetPlayerShipRequest implements RequestMessage {
    
    private static final long serialVersionUID = 1L;
    private final String playerName;

    public GetPlayerShipRequest(String playerName) {
        this.playerName = playerName;
    }

    @Override
    public void handleServer(SocketClientHandler handler) {
        try {
            handler.getPlayerShip(playerName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
    
}
