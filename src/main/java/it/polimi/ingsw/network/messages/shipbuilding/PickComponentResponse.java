package it.polimi.ingsw.network.messages.shipbuilding;

import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.network.client.listeners.GameListener;
import it.polimi.ingsw.network.messages.ResponseMessage;

public class PickComponentResponse implements ResponseMessage {

    private static final long serialVersionUID = 1L;
    private final int playerId;
    private final Component component;
    private final boolean success;
    private final String message;

    public PickComponentResponse(int playerId, Component component, boolean success, String message) {
        this.playerId = playerId;
        this.component = component;
        this.success = success;
        this.message = message;
    }

    public int getPlayerId() {
        return playerId;
    }

    public Component getComponent() {
        return component;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public void handleClient(GameListener listener) {
        // Call the new componentPicked method for all listeners
        listener.componentPicked(playerId, component, success, message);
    }
} 