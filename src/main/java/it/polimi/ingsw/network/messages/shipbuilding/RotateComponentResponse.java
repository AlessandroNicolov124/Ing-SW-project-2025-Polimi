package it.polimi.ingsw.network.messages.shipbuilding;

import it.polimi.ingsw.Position;
import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.network.client.listeners.GameListener;
import it.polimi.ingsw.network.messages.ResponseMessage;
import it.polimi.ingsw.network.messages.RotationType;

public class RotateComponentResponse implements ResponseMessage {
    private static final long serialVersionUID = 1L;
    private final int playerId;
    private final Position position;
    private final RotationType rotationType;
    private final boolean isValid;
    private final boolean success;
    private final String message;
    //private final Component rotatedComponent;

    public RotateComponentResponse(int playerId, Position position, RotationType rotationType, 
                                 boolean isValid, boolean success, String message) {
        this.playerId = playerId;
        this.position = position;
        this.rotationType = rotationType;
        this.isValid = isValid;
        this.success = success;
        this.message = message;
        //this.rotatedComponent = rotatedComponent;
    }

    @Override
    public void handleClient(GameListener listener) {
        if (success) {
            listener.componentRotated(playerId, position, rotationType, isValid);
        } else {
            listener.componentRotatedFailed(message);
        }
    }
}