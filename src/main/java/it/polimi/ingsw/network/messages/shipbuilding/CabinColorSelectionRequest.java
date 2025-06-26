package it.polimi.ingsw.network.messages.shipbuilding;

import it.polimi.ingsw.network.messages.RequestMessage;
import it.polimi.ingsw.network.socket.server.SocketClientHandler;

public class CabinColorSelectionRequest implements RequestMessage {

    private static final long serialVersionUID = 1L;
    private final int playerId;
    private final String color;

    public CabinColorSelectionRequest(int playerId, String color) {
        this.playerId = playerId;
        this.color = color;
    }

    @Override
    public void handleServer(SocketClientHandler handler) {
        try {
            handler.selectCabinColor(color, playerId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
