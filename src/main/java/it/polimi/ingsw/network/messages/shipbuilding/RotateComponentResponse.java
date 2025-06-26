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
    private final boolean success;
    private final String errorMessage;

    public RotateComponentResponse(int playerId, Position position, RotationType rotationType, boolean isValid, boolean success, String errorMessage) {
        this.playerId = playerId;
        this.position = position;
        this.rotationType = rotationType;
        this.isValid = isValid;
        this.success = success;
        this.errorMessage = errorMessage;
    }

    @Override
    public void handleClient(GameListener listener) {
        if (success) {
            listener.componentRotated(playerId, position, rotationType, isValid);
        } else {
            listener.componentRotatedFailed(errorMessage);
        }
    }
    
}
