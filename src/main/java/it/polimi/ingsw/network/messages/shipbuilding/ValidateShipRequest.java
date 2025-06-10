package it.polimi.ingsw.network.messages.shipbuilding;

import it.polimi.ingsw.network.messages.RequestMessage;
import it.polimi.ingsw.network.socket.server.SocketClientHandler;

public class ValidateShipRequest implements RequestMessage {

    private static final long serialVersionUID = 1L;
    private final int playerId;

    public ValidateShipRequest(int playerId) {
        this.playerId = playerId;
    }

    public int getPlayerId() {
        return playerId;
    }

    @Override
    public void handleServer(SocketClientHandler handler) {
        try {
            handler.validateShip();
        } catch (Exception e) {
            System.err.println("Error handling validateShip request: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 