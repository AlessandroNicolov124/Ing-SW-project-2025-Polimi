package it.polimi.ingsw.network.messages.shipbuilding;

import it.polimi.ingsw.network.messages.RequestMessage;
import it.polimi.ingsw.network.socket.server.SocketClientHandler;

public class ExpiringTimeRequest implements RequestMessage {
    private final String playerName;
    private static final long serialVersionUID = 1L;

    public ExpiringTimeRequest(String playerName) {
        this.playerName = playerName;
    }

    @Override
    public void handleServer(SocketClientHandler handler) {
        try {
            handler.sendTimeExpiredNotification(playerName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}