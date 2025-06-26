package it.polimi.ingsw.network.messages.shipbuilding;

import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.network.client.listeners.GameListener;
import it.polimi.ingsw.network.messages.ResponseMessage;

public class PickComponentResponse implements ResponseMessage {

    private static final long serialVersionUID = 1L;
    private final int playerId;
    private final Component component;
    private final boolean success;
    private final String errorMessage;


    public PickComponentResponse(int playerId, Component component, boolean success, String errorMessage) {
        this.playerId = playerId;
        this.component = component;
        this.success = success;
        this.errorMessage = errorMessage;
    }

    @Override
    public void handleClient(GameListener listener) {
        listener.componentPicked(playerId, component, success, errorMessage);
    }
} 