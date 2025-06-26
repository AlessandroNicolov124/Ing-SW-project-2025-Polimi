package it.polimi.ingsw.network.messages.shipbuilding;

import it.polimi.ingsw.Position;
import it.polimi.ingsw.network.messages.RequestMessage;
import it.polimi.ingsw.network.messages.RotationType;
import it.polimi.ingsw.network.socket.server.SocketClientHandler;

public class RotateComponentRequest implements RequestMessage {

    private static final long serialVersionUID = 1L;
    private final int playerId;
    private final Position position;
    private final RotationType rotationType;

    public RotateComponentRequest(int playerId, Position position, RotationType rotationType) {
        this.playerId = playerId;
        this.position = position;
        this.rotationType = rotationType;
    }

    @Override
    public void handleServer(SocketClientHandler handler) {
        try {
            handler.rotateComponent(playerId, position, rotationType);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
