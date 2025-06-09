package it.polimi.ingsw.network.messages.shipbuilding;

import it.polimi.ingsw.Position;
import it.polimi.ingsw.network.messages.RequestMessage;
import it.polimi.ingsw.network.socket.server.SocketClientHandler;

public class RemoveComponentRequest implements RequestMessage {

    private static final long serialVersionUID = 1L;
    private final int playerId;
    private final Position position;

    public RemoveComponentRequest(int playerId, Position position) {
        this.playerId = playerId;
        this.position = position;
    }

    public int getPlayerId() {
        return playerId;
    }

    public Position getPosition() {
        return position;
    }

    @Override
    public void handleServer(SocketClientHandler handler) {
        try {
            handler.removeComponent(playerId, position);
        } catch (Exception e) {
            System.err.println("Error handling removeComponent request: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 