package it.polimi.ingsw.network.messages.shipbuilding;

import it.polimi.ingsw.network.messages.RequestMessage;
import it.polimi.ingsw.network.socket.server.SocketClientHandler;

public class PickComponentRequest implements RequestMessage {

    private static final long serialVersionUID = 1L;
    private final int playerId;

    public PickComponentRequest(int playerId) {
        this.playerId = playerId;
    }

    public int getPlayerId() {
        return playerId;
    }

    @Override
    public void handleServer(SocketClientHandler handler) {
        try {
            handler.pickComponentCard();
        } catch (Exception e) {
            System.err.println("Error handling pickComponent request: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 