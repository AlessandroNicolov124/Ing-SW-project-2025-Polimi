package it.polimi.ingsw.network.messages.shipbuilding;

import it.polimi.ingsw.Position;
import it.polimi.ingsw.network.client.listeners.GameListener;
import it.polimi.ingsw.network.messages.ResponseMessage;
import it.polimi.ingsw.network.messages.RotationType;

public class RotateComponentResponse implements ResponseMessage {

    private static final long serialVersionUID = 1L;
    private final int playerId;
    private final Position position;
    private final RotationType rotationType;
    private boolean isValid = false;

    public RotateComponentResponse(int playerId, Position position, RotationType rotationType, boolean isValid) {
        this.playerId = playerId;
        this.position = position;
        this.rotationType = rotationType;
        this.isValid = isValid;
    }

    public int getPlayerId() {
        return playerId;
    }

    public Position getPosition() {
        return position;
    }

    public RotationType isRotationType() {
        return rotationType;
    }

    public boolean isValid() {
        return isValid;
    }

    @Override
    public void handleClient(GameListener listener) {
        listener.componentRotated(playerId, position, rotationType, isValid);
    }
    
}
