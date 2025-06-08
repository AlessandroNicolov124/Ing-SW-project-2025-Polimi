package it.polimi.ingsw.network.messages.shipbuilding;

import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.Position;
import it.polimi.ingsw.network.client.listeners.GameListener;
import it.polimi.ingsw.network.messages.ResponseMessage;

public class PlaceComponentResponse implements ResponseMessage {

    private static final long serialVersionUID = 1L;
    private final int playerId;
    private final Component component;
    private final Position position;
    private final boolean success;
    private final String message;

    public PlaceComponentResponse(int playerId, Component component, Position position, boolean success, String message) {
        this.playerId = playerId;
        this.component = component;
        this.position = position;
        this.success = success;
        this.message = message;
    }

    public int getPlayerId() {
        return playerId;
    }

    public Component getComponent() {
        return component;
    }

    public Position getPosition() {
        return position;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public void handleClient(GameListener listener) {
        // For RMI, responses are broadcast to all clients in the match
        // Each client should only process responses for their own player
        if (listener instanceof it.polimi.ingsw.view.tui.components.GalaxyTruckerTUI) {
            it.polimi.ingsw.view.tui.components.GalaxyTruckerTUI tui = 
                (it.polimi.ingsw.view.tui.components.GalaxyTruckerTUI) listener;
            
            // Check if this response is for the current player
            if (tui.getCurrentPlayer() != null && tui.getCurrentPlayer().getId() == playerId) {
                // Handle both success and failure cases for our player
                if (success) {
                    listener.componentPlaced(component, position);
                    // Trigger display refresh to show the updated ship
                    listener.displayRefresh();
                } else {
                    // Handle placement failure with user-friendly message
                    tui.showMessage("✗ Component placement failed: " + message);
                }
            }
            // If it's not for our player, ignore the response
        } else {
            // For non-TUI clients, handle as before
            if (success) {
                listener.componentPlaced(component, position);
                listener.displayRefresh();
            } else {
                System.err.println("Component placement failed: " + message);
            }
        }
    }
    
} 