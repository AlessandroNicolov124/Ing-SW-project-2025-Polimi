package it.polimi.ingsw.network.messages.shipbuilding;

import it.polimi.ingsw.Position;
import it.polimi.ingsw.network.client.listeners.GameListener;
import it.polimi.ingsw.network.messages.ResponseMessage;

public class RemoveComponentResponse implements ResponseMessage {

    private static final long serialVersionUID = 1L;
    private final int playerId;
    private final Position position;
    private final boolean success;
    private final String errorMessage;

    public RemoveComponentResponse(int playerId, Position position, boolean success, String message) {
        this.playerId = playerId;
        this.position = position;
        this.success = success;
        this.errorMessage = message;
    }

    @Override
    public void handleClient(GameListener listener) {
        if (success) {
            listener.componentRemoved(position);
        } else {
            listener.componentRemovedFailed(errorMessage);
        }
    }
} 